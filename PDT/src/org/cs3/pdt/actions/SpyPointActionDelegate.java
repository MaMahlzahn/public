package org.cs3.pdt.actions;

import java.io.IOException;
import java.util.Hashtable;
import java.util.ResourceBundle;

import org.cs3.pdt.PDTPlugin;
import org.cs3.pdt.editors.PLEditor;
import org.cs3.pl.common.Debug;
import org.cs3.pl.metadata.PrologElementData;
import org.cs3.pl.prolog.PrologSession;
import org.cs3.pl.prolog.SessionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * @see IWorkbenchWindowActionDelegate
 */
public class SpyPointActionDelegate extends TextEditorAction {
    /**
     *  
     */
    public SpyPointActionDelegate(ITextEditor editor) {
        super(ResourceBundle.getBundle("actions"),SpyPointActionDelegate.class.getName(),editor); //$NON-NLS-1$
    }

    /**
     * @see IWorkbenchWindowActionDelegate#run
     */

    Hashtable spypred = new Hashtable();

    public void run() {
        PDTPlugin plugin = PDTPlugin.getDefault();
        plugin.getDisplay().asyncExec(new Runnable() {
            public void run() {
                PDTPlugin plugin = PDTPlugin.getDefault();

                PLEditor editor = (PLEditor) plugin.getActiveEditor();
                PrologSession session = null;
                try {
                    session = plugin.getPrologInterface().getSession();
                } catch (IOException e) {
                    Debug.report(e);
                    return;
                }
                String pred;

                PrologElementData data;
                try {
                    data = editor.getSelectedPrologElement();
                } catch (BadLocationException e2) {
                    Debug.report(e2);
                    return;
                }
                if (data == null) {
                    MessageDialog
                            .openInformation(editor.getEditorSite().getShell(),
                                    "PDT Plugin", 
                                    "Cannot locate a predicate at the specified location."); 
                    return;

                }
                if (data.isModule()) {
                    MessageDialog.openInformation(editor.getEditorSite()
                            .getShell(), "PDT Plugin", 
                            "Cannot spy on a module " +": "+ data.getSignature()  //$NON-NLS-2$
                                    + "."); //$NON-NLS-1$
                    return;

                }
                pred = data.getSignature();

                if (spypred.get(pred) != null) {
                    try {
                        session.query("nospy(" + pred + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                    } catch (SessionException e1) {
                        Debug.report(e1);
                        return;
                    }
                    spypred.remove(pred);
                } else {
                    try {
                        session.query("spy(" + pred + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                    } catch (SessionException e1) {
                        Debug.report(e1);
                        return;
                    }
                    spypred.put(pred, pred);
                }

            }
        });
    }

    /**
     * @param editorPart
     */
    private void updateOutline(PLEditor editor, String filename) {
        // trigger inputChanged...
        editor.getOutlinePage().getTreeViewer().setInput(
                editor.getEditorInput());
        //		editor.getOutlinePage().getContentProvider().inputChanged(editor.getOutlinePage().getTreeViewer(),null,filename);
    }

    /**
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {

    }

    /**
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *           org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        // TODO Auto-generated method stub

    }
}