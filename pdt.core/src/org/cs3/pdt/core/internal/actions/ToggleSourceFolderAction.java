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

package org.cs3.pdt.core.internal.actions;

import java.util.Iterator;
import java.util.Set;

import org.cs3.pdt.core.IPrologProject;
import org.cs3.pdt.core.PDTCore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ToggleSourceFolderAction implements IObjectActionDelegate {

	private IContainer container;

	private IProject project;

	private IPrologProject plProject;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		;
	}

	@Override
	public void run(IAction action) {
		Set<IContainer> entries;
		try {
			entries = plProject.getExistingSourcePathEntries();

			if(action.isChecked()){
				entries.add(container);
			}else{
				entries.remove(container);
			}
			
			
			String sep = System.getProperty("path.separator");
			StringBuffer sb = new StringBuffer();
			for (Iterator<IContainer> it = entries.iterator(); it.hasNext();) {
				IContainer entry = it.next();

				
				IPath path = entry.getProjectRelativePath();
				String string = path.toPortableString();

				//make sure the project root itself is "correctly" represented.
				if("".equals(string)){
					string="/"; 
				}
				sb.append(string);
				if (it.hasNext()) {
					sb.append(sep);
				}
			}
			
			plProject.setPreferenceValue(PDTCore.PROP_SOURCE_PATH, sb.toString());
			updateAction(action);
		} catch (CoreException e) {
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		container = null;
		try {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (obj instanceof IContainer) {
					container = (IContainer) obj;
				} else if (obj instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) obj;
					IResource r = (IResource) a.getAdapter(IResource.class);
					if (r != null
							&& (IResource.PROJECT == r.getType() || IResource.FOLDER == r
									.getType())) {
						container = (IContainer) r;
					}
				}
			}
			if (container != null) {
				project = container.getProject();
			} else {
				project = null;
			}
			if (project != null&&project.isOpen()) {

				if (project.hasNature(PDTCore.NATURE_ID)) {
					plProject = (IPrologProject) project
							.getNature(PDTCore.NATURE_ID);

				} else {
					plProject = null;
				}

			} else {
				plProject = null;
			}
			updateAction(action);
		} catch (CoreException e) {
		}
	}

	private void updateAction(IAction action) throws CoreException {
		if (plProject == null) {
			action.setEnabled(false);
			action.setChecked(false);
		} else {
			action.setEnabled(true);
			action.setChecked(plProject.getExistingSourcePathEntries()
					.contains(container));
		}

	}

}


