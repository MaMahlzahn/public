package org.cs3.jtransformer.internal.actions;

import java.util.Map;

import org.cs3.jtransformer.JTransformer;
import org.cs3.jtransformer.JTransformerPlugin;
import org.cs3.jtransformer.internal.natures.JTransformerProjectNature;
import org.cs3.jtransformer.util.JTUtils;
import org.cs3.pdt.ui.util.UIUtils;
import org.cs3.pl.common.Debug;
import org.cs3.pl.prolog.PrologInterfaceException;
import org.cs3.pl.prolog.PrologSession;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;


public class OpenSelectionInEditorAction extends ConsoleSelectionAction{

	/**
	 * @throws CoreException
	 * @throws PrologInterfaceException 
	 */
	static PrologSession getPrologSession() throws CoreException, PrologInterfaceException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		JTransformerProjectNature nature = null;
		for (int i = 0; i < projects.length; i++) {
			if(projects[i].isAccessible() && projects[i].hasNature(JTransformer.NATURE_ID)){
				nature = JTransformerPlugin.getNature(projects[i]);
				break;
			}
		}
		if(nature == null)
			return null;
		return nature.getPrologInterface().getSession();
	}
	
	public void run(IAction action) {
		PrologSession session;
		try {
			session = getPrologSession();
			Map result = session.queryOnce("sourceLocation(" + getPefId()
					+ ", File, Start, Length)");
			if (result == null) {
				result = session.queryOnce("tree(" + getPefId()
						+ ", _,Kind)");
				if (result == null) {
					UIUtils.displayErrorDialog(JTUtils.getShell(true), "JTransformer", 
						"'" + getPefId()
								+ "' is not a tree element.");
				} else {
					String kind = (String)result.get("Kind");
					result = session.queryOnce("enclClass(" + getPefId()
							+ ", EnclClass), fullQualifiedName(EnclClass,FQN)");
					if(result == null) {
						UIUtils.displayErrorDialog(JTUtils.getShell(true), "JTransformer", 
								"Could not find the source location for the tree '" + getPefId() + 
								"' of type " + kind + ".\n");
					} else {
						UIUtils.displayErrorDialog(JTUtils.getShell(true), "JTransformer", 
								"Could not find the source location for the tree '" + getPefId() + 
								"'.\nIts enclosing class '" + result.get("FQN") + "' is an external byte code class.");
						
					}

				}
				
			}
			else {
				String filename = result.get("File").toString();
				int start = Integer.parseInt(result.get("Start").toString());
				int length = Integer.parseInt(result.get("Length").toString());
				JTUtils.selectInEditor(start, length, filename);
			}
		} catch (CoreException e) {
			Debug.report(e);
		} catch (PrologInterfaceException e)
		{
			Debug.report(e);
		}
	}


}
