/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others) 
 * E-mail: degenerl@cs.uni-bonn.de
 * WWW: http://roots.iai.uni-bonn.de/research/pdt 
 * Copyright (C): 2004-2006, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * In addition, you may at your option use, modify and redistribute any
 * part of this program under the terms of the GNU Lesser General Public
 * License (LGPL), version 2.1 or, at your option, any later version of the
 * same license, as long as
 * 
 * 1) The program part in question does not depend, either directly or
 *   indirectly, on parts of the Eclipse framework and
 *   
 * 2) the program part in question does not include files that contain or
 *   are derived from third-party work and are therefor covered by special
 *   license agreements.
 *   
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *   
 * ad 1: A program part is said to "depend, either directly or indirectly,
 *   on parts of the Eclipse framework", if it cannot be compiled or cannot
 *   be run without the help or presence of some part of the Eclipse
 *   framework. All java classes in packages containing the "pdt" package
 *   fragment in their name fall into this category.
 *   
 * ad 2: "Third-party code" means any code that was originaly written as
 *   part of a project other than the PDT. Files that contain or are based on
 *   such code contain a notice telling you so, and telling you the
 *   particular conditions under which they may be used, modified and/or
 *   distributed.
 ****************************************************************************/

/*
 * Created on 31.01.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.cs3.pdt.internal.views.lightweightOutline;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.cs3.pdt.PDT;
import org.cs3.pdt.PDTPlugin;
import org.cs3.pdt.PDTUtils;
import org.cs3.pdt.internal.editors.PLEditor;
import org.cs3.pdt.internal.queries.PDTOutlineQuery;
import org.cs3.pdt.internal.structureElements.OutlineModuleElement;
import org.cs3.pdt.internal.structureElements.OutlinePredicate;
import org.cs3.pdt.internal.structureElements.PredicateOccuranceElement;
import org.cs3.pdt.internal.views.HideDirectivesFilter;
import org.cs3.pdt.internal.views.HidePrivatePredicatesFilter;
import org.cs3.pdt.internal.views.HideSubtermsFilter;
import org.cs3.pdt.internal.views.PrologOutlineComparer;
import org.cs3.pdt.internal.views.PrologOutlineFilter;
import org.cs3.pdt.internal.views.ToggleSortAction;
import org.cs3.pl.common.FileUtils;
import org.cs3.pl.common.Util;
import org.cs3.pl.metadata.SourceLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;



public class NonNaturePrologOutline extends ContentOutlinePage {
	private static final int EXPANDING_LEVEL = 2;
	public static final String MENU_ID = "org.cs3.pdt.outline.menu";
	private ITreeContentProvider contentProvider;
	private PrologSourceFileModel model;
	private PLEditor editor;
	private ILabelProvider labelProvider;
	private PrologOutlineFilter[] filters;
	private Menu contextMenu;
//	private StringMatcher matcher;
	
	public NonNaturePrologOutline(PLEditor editor) {
		this.editor = editor;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		
		contentProvider = new OutlineContentProvider();
		viewer.setContentProvider(contentProvider);
		
//		labelProvider = new OutlineLabelProvider();
		
		labelProvider = new DecoratingLabelProvider(new OutlineLabelProvider(), 
				PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
		viewer.setLabelProvider(labelProvider);

		viewer.setComparer(new PrologOutlineComparer());

		viewer.addSelectionChangedListener(this);
		
		
		model = new PrologSourceFileModel(new HashMap<String,OutlineModuleElement>());
		
		viewer.setInput(model);
		
		viewer.setAutoExpandLevel(EXPANDING_LEVEL);
		
		initFilters();

		IActionBars actionBars = getSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
//		Action action = new LexicalSortingAction(viewer);
//		toolBarManager.add(action);
		Action action = new ToggleSortAction(getTreeViewer());
		toolBarManager.add(action);
//		action = new FilterActionMenu(this);
//		toolBarManager.add(action);
		
		hookContextMenu(parent);
		setInput(editor.getEditorInput());
	}


	private void fillContextMenu(IMenuManager manager) {		
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void hookContextMenu(Composite parent) {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				NonNaturePrologOutline.this.fillContextMenu(manager);
			}
		});
		TreeViewer viewer = getTreeViewer();
		getSite().registerContextMenu(MENU_ID,menuMgr, viewer);
		contextMenu = menuMgr.createContextMenu(parent);
		viewer.getControl().setMenu(contextMenu);
	}

	
	@Override
	public TreeViewer getTreeViewer() {
		return super.getTreeViewer();
	}

	public void setInput(Object information) {
		String fileName = editor.getPrologFileName();
		
		Map<String,OutlineModuleElement> modules;
		TreeViewer treeViewer = getTreeViewer();
		if (fileName != "") {
			try {			
				modules = PDTOutlineQuery.getProgramElementsForFile(fileName);
				model.update(modules);
				
				treeViewer.setInput(model);
				treeViewer.setAutoExpandLevel(EXPANDING_LEVEL);

			} catch(Exception e) {
				
			}
		}
		
		if (treeViewer != null) {
			treeViewer.refresh();
		}
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		super.selectionChanged(event);
		Object elem = getFirstSelectedElement(event);
		OutlinePredicate predicate=null;
		String selectedFile = "";
		int line;
		if ((elem != null) && (elem instanceof OutlinePredicate)) { 
			predicate = (OutlinePredicate)elem;
			line = predicate.getLine();
			selectedFile = predicate.getFileName();
		} else if ((elem != null) && (elem instanceof PredicateOccuranceElement)) {
			PredicateOccuranceElement occurance = (PredicateOccuranceElement)elem;
			line = occurance.getLine();
			selectedFile = occurance.getFile();
			predicate = (OutlinePredicate)occurance.getParent();
		} else {
			return;
		}
		
		String editorFileName = editor.getPrologFileName();
		if (selectedFile.equals(editorFileName)) {
			editor.gotoLine(line);
		} else {
			IFile file;
			try {
				file = FileUtils.findFileForLocation(selectedFile);
				SourceLocation loc = createLocation(predicate, line, file);
				PDTUtils.showSourceLocation(loc);
			} catch (IOException e) {
			}
		}
	}

	private Object getFirstSelectedElement(final SelectionChangedEvent event) {
		if(event.getSelection().isEmpty()){
			return null;
		}
		if(!(event.getSelection() instanceof IStructuredSelection)){
			return null;
		}
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object elem = selection.getFirstElement();

		return elem;
	}

	private SourceLocation createLocation(OutlinePredicate predicate,
			int line, IFile file) {
		SourceLocation loc = new SourceLocation(file.getRawLocation().toPortableString(), false);
		loc.isWorkspacePath = file.isAccessible();
		loc.setLine(line);
		loc.setPredicateName(predicate.getFunctor());
		loc.setArity(predicate.getArity());
		return loc;
	}

	public PrologOutlineFilter[] getAvailableFilters() {
		if (filters == null) {
			filters = new PrologOutlineFilter[] {
					new HideDirectivesFilter("hide_directives",
							"Hide Directives"),
					new HidePrivatePredicatesFilter("hide_private_predicates",
							"Hide Private Predicates"),
					new HideSubtermsFilter("hide_subterms", "Hide Subterms") };
		}
		return filters;
	}

	protected void initFilters() {
		String value = PDTPlugin.getDefault().getPreferenceValue(
				PDT.PREF_OUTLINE_FILTERS, "");
		HashSet<String> enabledIds = new HashSet<String>();
		Util.split(value, ",", enabledIds);
		PrologOutlineFilter[] filters = getAvailableFilters();
		for (int i = 0; i < filters.length; i++) {
			PrologOutlineFilter filter = filters[i];
			if (enabledIds.contains(filter.getId())) {
				getTreeViewer().addFilter(filter);
			}
		}
	}
	

	@Override
	public void dispose() {
		super.dispose();
		contentProvider.dispose();
		model.dispose();
	}
}