package org.cs3.pdt.internal.editors;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.cs3.pdt.quickfix.PDTMarker;
import org.cs3.prolog.common.FileUtils;
import org.cs3.prolog.common.Util;
import org.cs3.prolog.common.logging.Debug;
import org.cs3.prolog.pif.PrologException;
import org.cs3.prolog.pif.PrologInterface;
import org.cs3.prolog.pif.PrologInterfaceException;
import org.cs3.prolog.session.PrologSession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class PLMarkerUtils {

	private static int mapSeverity(String severity) {
		if ("error".equals(severity)) {
			return IMarker.SEVERITY_ERROR;
		}
		if ("warning".equals(severity)) {
			return IMarker.SEVERITY_WARNING;
		}
		if ("info".equals(severity)) {
			return IMarker.SEVERITY_INFO;
		}

		throw new IllegalArgumentException("cannot map severity constant: "
				+ severity);
	}

	public static void addMarkers(PrologInterface pif, IProgressMonitor monitor) {
		monitor.beginTask("Update markers", 2);
		PrologSession session =null;
		try {
			session = pif.getSession();
			Map<String, IFile> reloadedFiles = new HashMap<String, IFile>();
			monitor.subTask("add markers for errors and warnings");
			addMarkersForErrorsAndWarnings(session, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), reloadedFiles);
			monitor.subTask("Update Prolog Smells Detectors");
			addMarkersForSmellDetectors(session, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), reloadedFiles);
//			session.queryOnce("deactivate_warning_and_error_tracing");
		} catch (PrologException e) {
			// this may be a reload_timeout_reached exception
			// so at least we deactivate the tracing, because
			// otherwise error markers will still be visible after removing the error
//			try {
//				session.queryOnce("deactivate_warning_and_error_tracing");
//			} catch (Exception e1) {
//				Debug.report(e1);
//			}
		} catch (Exception e) {
			Debug.report(e);
		} finally {
			if (session != null) {
				session.dispose();
			}
			monitor.done();
		}
	}
	
	private static void addMarkersForErrorsAndWarnings(PrologSession session, SubProgressMonitor monitor, Map<String, IFile> reloadedFiles) throws PrologInterfaceException, CoreException {
		List<Map<String, Object>> reloadedFilesQueryResuot = session.queryAll("pdtplugin:pdt_reloaded_file(File)");
		List<Map<String, Object>> msgs = session.queryAll("pdtplugin:errors_and_warnings(Kind,Line,Length,Message,File)");
		monitor.beginTask("add markers for errors and warnings", reloadedFilesQueryResuot.size() + msgs.size());
		
		HashSet<IFile> clearedFiles = new HashSet<IFile>();
		for (Map<String, Object> reloadedFile : reloadedFilesQueryResuot) {
			String fileName = reloadedFile.get("File").toString();
			IFile file = null;
			try {
				file = FileUtils.findFileForLocation(fileName);
			} catch (IOException e1) {
				monitor.worked(1);
				continue;
			} catch (IllegalArgumentException e2){
				monitor.worked(1);
				continue;
			}
			if (file == null || !file.exists()){
				monitor.worked(1);
				continue;
			}
			file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			clearedFiles.add(file);
			reloadedFiles.put(fileName, file);
			monitor.worked(1);
		}
		
		for (Map<String, Object> msg : msgs) {
			int severity=0;
			try {
				severity = mapSeverity(((String)msg.get("Kind")));
			} catch(IllegalArgumentException e){
				monitor.worked(1);
				continue;
			}
			
			String fileName = msg.get("File").toString();
			IFile file = reloadedFiles.get(fileName);
			if (file == null) {
				try {
					file = FileUtils.findFileForLocation(fileName);
				} catch (IOException e1) {
					monitor.worked(1);
					continue;
				} catch (IllegalArgumentException e2){
					monitor.worked(1);
					continue;
				}
				if (file == null || !file.exists()){
					monitor.worked(1);
					continue;
				}
				file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				clearedFiles.add(file);
				reloadedFiles.put(fileName, file);
			}
			
			IMarker marker = file.createMarker(IMarker.PROBLEM);

			marker.setAttribute(IMarker.SEVERITY, severity);

			String msgText = msg.get("Message").toString();
			int line = Integer.parseInt(msg.get("Line").toString());
			if (severity == IMarker.SEVERITY_ERROR && msgText.startsWith("Exported procedure ") && msgText.endsWith(" is not defined\n")){
				line = 1;
			}
			
			MarkerUtilities.setLineNumber(marker, line);
			
			marker.setAttribute(IMarker.MESSAGE, msgText);
			monitor.worked(1);
		}
		monitor.done();
	}
	
	private static void addMarkersForSmellDetectors(PrologSession session, IProgressMonitor monitor, Map<String, IFile> reloadedFiles) throws PrologInterfaceException, CoreException {
		monitor.beginTask("Update Prolog Smells Detectors", reloadedFiles.size());

		for (String fileName : reloadedFiles.keySet()) {
			String query = "smell_marker_pdt(Name, Description, QuickfixDescription, QuickfixAction, '" + fileName + "', Start, Length)";
			List<Map<String, Object>> msgsSmells = session.queryAll(query);
			
			if(msgsSmells!=null) {
				IFile file = reloadedFiles.get(fileName);
				IDocument doc = Util.getDocument(file);
				for (Map<String, Object> msg : msgsSmells) {
					IMarker marker = file.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(PDTMarker.SMELL_NAME, msg.get("Name").toString());
					marker.setAttribute(PDTMarker.QUICKFIX_DESCRIPTION, msg.get("QuickfixDescription").toString());
					
					String msgText = (String)msg.get("Description");
					int startPl = Integer.parseInt(msg.get("Start").toString());
					int start = Util.logicalToPhysicalOffset(doc,startPl);
					int length = Integer.parseInt(msg.get("Length").toString());
					
					MarkerUtilities.setCharStart(marker, start);
					MarkerUtilities.setCharEnd(marker, start+length);
					
					marker.setAttribute(PDTMarker.QUICKFIX_ACTION, msg.get("QuickfixAction".toString()));
					marker.setAttribute(IMarker.MESSAGE, msgText);
				}
			}
			monitor.worked(1);
		}
		monitor.done();
	}


}
