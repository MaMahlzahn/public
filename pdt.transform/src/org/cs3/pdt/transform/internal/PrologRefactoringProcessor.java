/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package org.cs3.pdt.transform.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cs3.pdt.runtime.PrologRuntimePlugin;
import org.cs3.pl.common.Option;
import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.PrologInterfaceException;
import org.cs3.pl.prolog.PrologSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

public class PrologRefactoringProcessor extends RefactoringProcessor {

	private PrologRefactoringInfo info;
	PrologRefactoringProcessorChangeCreator changeCreator;

	public PrologRefactoringProcessor(PrologRefactoringInfo info) {
		this.info = info;
		this.changeCreator = new PrologRefactoringProcessorChangeCreator(info);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		PrologSession s = null;
		Option[] options = info.getOptions();
		Set<String> knownParameters = new HashSet<String>();
		for (int i = 0; i < options.length; i++) {
			knownParameters.add(options[i].getId());
		}
		try {
			s = info.getPrologInterace().getSession(PrologInterface.NONE);
			PrologRuntimePlugin.getDefault();
			info.configure(
					PrologRuntimePlugin.getLibraryManager(), s);
			String selectionTerm = info.getSelectionTerm();
			String parameterTerm = info.getParameterTerm();
			String identifier = info.getRefactoringId();
			Map<String, Object> m = s.queryOnce("pdt_interprete_selection('"
					+ identifier + "'," + selectionTerm + "," + parameterTerm
					+ ")");
			if (m == null) {
				result
						.addFatalError("Refactoring cannot be applied to the current selection.");
			}
			for (Entry<String, Object> entry : m.entrySet()) {
				if (knownParameters.contains(entry.getKey())) {
					info.setPreferenceValue(entry.getKey(), (String) entry.getValue());
				}
			}

		} catch (PrologInterfaceException e) {
			result.addFatalError("Problems with Prolog Connection");
		} finally {
			if (s != null) {
				s.dispose();
			}
		}

		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {

		RefactoringStatus result = new RefactoringStatus();
		PrologSession s = null;

		try {
			s = info.getPrologInterace().getSession(PrologInterface.NONE);

			String parameterTerm = info.getParameterTerm();
			String identifier = info.getRefactoringId();
			Map<String, Object> m = s.queryOnce("trace,pdt_perform_transformation('"
					+ identifier + "'," + parameterTerm + ")");
			if (m == null) {
				result.addFatalError("Transformation failed.");
			}
			List<Map<String, Object>> l = s
					.queryAll("pdt_transformation_problem(Id,Severity,Message)");
			for (Map<String, Object> m2 : l) {

				String severity = (String) m2.get("Severity");
				String message = (String) m2.get("Message");
				if ("info".equals(severity)) {
					result.addInfo(message);
				} else if ("warning".equals(severity)) {
					result.addWarning(message);
				} else if ("error".equals(severity)) {
					result.addError(message);
				} else if ("fatal".equals(severity)) {
					result.addFatalError(message);
				}
			}

		} catch (PrologInterfaceException e) {
			result.addFatalError("Problems with Prolog Connection");
		} finally {
			if (s != null) {
				s.dispose();
			}
		}

		return result;
	}


	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
				return changeCreator.createChange();
			}


	@Override
	public Object[] getElements() {
		return new String[] { "Some Element" };
	}

	@Override
	public String getIdentifier() {

		return getClass().getName();
	}

	@Override
	public String getProcessorName() {
		return info.getName();
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return true;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
			SharableParticipants sharedParticipants) throws CoreException {
		return new RefactoringParticipant[0];
	}

}


