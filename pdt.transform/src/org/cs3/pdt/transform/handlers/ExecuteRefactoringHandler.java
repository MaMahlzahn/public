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

package org.cs3.pdt.transform.handlers;

import org.cs3.pdt.transform.PDTTransformationsPlugin;
import org.cs3.pdt.transform.PrologRefactoringDescriptor;
import org.cs3.pdt.transform.internal.DeclarativePrologRefactoringInfo;
import org.cs3.pdt.transform.internal.PrologRefactoring;
import org.cs3.pdt.transform.internal.PrologRefactoringInfo;
import org.cs3.pdt.transform.internal.PrologRefactoringProcessor;
import org.cs3.pdt.transform.internal.wizards.PrologRefactoringWizard;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExecuteRefactoringHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		
	}

	@Override
	public void dispose() {

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection=HandlerUtil.getCurrentSelectionChecked(event);
		IWorkbenchPart activePart = HandlerUtil.getActivePartChecked(event);
		Shell shell = HandlerUtil.getActiveShellChecked(event);
		String refactoringId= event.getParameter("pdt.transform.executeRefactoring.refactoring");
		PrologRefactoringDescriptor desc;
		PrologRefactoringInfo info;
		try {
			desc = PDTTransformationsPlugin.getDefault().getPrologRefactoringDescriptor(refactoringId);
			if(desc==null){
				throw new RuntimeException();
			}
			
			info = new DeclarativePrologRefactoringInfo(desc,selection,activePart);
		} catch (Exception e) {
			throw new ExecutionException("failed to obtain Refactoring Descriptor for id "+refactoringId,e);
		}

		
		PrologRefactoringProcessor processor = new PrologRefactoringProcessor(info);
		PrologRefactoring refac = new PrologRefactoring(processor);
		PrologRefactoringWizard wizard = new PrologRefactoringWizard(refac,info);
		RefactoringWizardOpenOperation op 
	      = new RefactoringWizardOpenOperation( wizard );
	    try {
	      String titleForFailedChecks = ""; //$NON-NLS-1$
	      op.run( shell, titleForFailedChecks );
	    } catch( final InterruptedException irex ) {
	      // operation was canceled
	    }
		
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		
	}

}


