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

package org.cs3.pdt.console.internal.views;

import java.io.File;

import org.cs3.pdt.console.PDTConsole;
import org.cs3.pdt.console.PrologConsolePlugin;
import org.cs3.pdt.ui.util.UIUtils;
import org.cs3.pl.common.Debug;
import org.cs3.pl.console.CompletionResult;
import org.cs3.pl.console.ConsoleCompletionProvider;
import org.cs3.pl.console.ConsoleHistory;
import org.cs3.pl.console.ConsoleModel;
import org.cs3.pl.console.ConsoleModelEvent;
import org.cs3.pl.console.ConsoleModelListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ConsoleViewer extends Viewer implements ConsoleModelListener {
	private static class _TextSelection implements ITextSelection {

		private int offset;
		private int length;
		private int startLine;
		private int endLine;

		private String text;

		public _TextSelection(StyledText control) {
			this.text = control.getSelectionText();
			this.offset = control.getSelectionRange().x;
			this.length = control.getSelectionRange().y;
			this.startLine = control.getLineAtOffset(offset);
			this.endLine = control.getLineAtOffset(offset + length);

		}

		@Override
		public int getOffset() {

			return offset;
		}

		@Override
		public int getLength() {
			return length;
		}

		@Override
		public int getStartLine() {
			return startLine;
		}

		@Override
		public int getEndLine() {
			return endLine;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public boolean isEmpty() {
			return text.length() == 0;
		}

	}

	StyledText control;

	private ConsoleModel model;
	private ConsoleCompletionProvider completionProvider;
	private ConsoleHistory history;
	private boolean thatWasMe;
	private int startOfInput = 0;
	private boolean enterSendsSemicolon;

	final String PLACEHOLDER_LGT_WARNING = "%         WARNING!";
	final String PLACEHOLDER_LGT_UNDEFINED = "called but never defined";
	
	final String PLACEHOLDER_WARNING = "WARNING";
	final String PLACEHOLDER_ERROR = "ERROR";
	final String PLACEHOLDER_DEBUG = "DEBUG";
	final String PLACEHOLDER_INFO = "INFO";
	final String PLACEHOLDER_SPACETAB = "  ";
	final String PLACEHOLDER_THREESTARS = "***";

	private Color LastOutputColor = null;
	private boolean LineFeedOccured = true;
	private Color COLOR_ERROR;
	private Color COLOR_WARNING;
	private Color COLOR_INFO;
	private Color COLOR_DEBUG;

	private KeyListener keyListener = new KeyListener() {
		@Override
		public void keyPressed(KeyEvent e) {
			ui_keyPressed(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			;
		}
	};
	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!thatWasMe) {
				ui_inputModified(ui_getLineBuffer());
			}
		}
	};
	private VerifyKeyListener verifyKeyListener = new VerifyKeyListener() {
		@Override
		public void verifyKey(VerifyEvent event) {
			ui_keyStrokeIntercepted(event);
		}
	};
	private VerifyListener verifyListener = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
			ui_inputModificationIntercepted(e);
		}
	};
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			initPreferences();

		}
	};

	private Boolean coloringEnabled;

	public ConsoleViewer(Composite parent, int styles) {
		createControl(parent, styles);
		initPreferences();

		IPreferenceStore store = PrologConsolePlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(propertyChangeListener);

	}

	private void initPreferences() {

		if (!control.isDisposed()) {
			Display display = control.getDisplay();
			IPreferenceStore store = PrologConsolePlugin.getDefault().getPreferenceStore();

			// Font
			FontData fd = PreferenceConverter.getFontData(store, PDTConsole.PREF_CONSOLE_FONT);
			control.setFont(new Font(display, fd));

			// Coloring
			RGB color_err = PreferenceConverter.getColor(store, PDTConsole.PREF_CONSOLE_COLOR_ERROR);
			RGB color_warn = PreferenceConverter.getColor(store, PDTConsole.PREF_CONSOLE_COLOR_WARNING);
			RGB color_info = PreferenceConverter.getColor(store, PDTConsole.PREF_CONSOLE_COLOR_INFO);
			RGB color_dbg = PreferenceConverter.getColor(store, PDTConsole.PREF_CONSOLE_COLOR_DEBUG);

			COLOR_ERROR = new Color(display, color_err);
			COLOR_WARNING = new Color(display, color_warn);
			COLOR_INFO = new Color(display, color_info);
			COLOR_DEBUG = new Color(display, color_dbg);
			
			coloringEnabled = store.getBoolean(PDTConsole.PREF_CONSOLE_SHOW_COLORS); 

		}
	}

	private void createControl(Composite parent, int styles) {
		control = new StyledText(parent, styles);
		control.addVerifyKeyListener(verifyKeyListener);
		control.addKeyListener(keyListener);
		control.addVerifyListener(verifyListener);
		control.addModifyListener(modifyListener);

		control.setTabs(4);
		control.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				e.doit = false;
			}
		});
		control.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				;

			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				fireSelectionChanged(new SelectionChangedEvent(ConsoleViewer.this, getSelection()));
			}

		});

		control.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (control.getSelectionCount() == 0) {
					try {
						int offset = control.getOffsetAtLocation(new Point(e.x, e.y));
						control.setSelection(offset);
					} catch (IllegalArgumentException ere) {
						// TODO: what do we do when mouse position is behind the
						// end
						// of a line?
						;
					}
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				;

			}

		});
		control.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// It is up to the application to determine when and how a link should be activated.
				// In this snippet links are activated on mouse down when the control key is held down 
				try {
					int offset = control.getOffsetAtLocation(new Point (e.x, e.y));
					StyleRange style = control.getStyleRangeAtOffset(offset);
					if (style != null && style.underline && style.underlineStyle == SWT.UNDERLINE_LINK) {
						StyleRange selectedStyle = findStyleRangeForOffset(control.getStyleRanges(), offset);
						if (selectedStyle == null) {
							return;
						}
						String link = control.getTextRange(selectedStyle.start, selectedStyle.length);
						String[] fileAndLine = link.split("pl:");
						if (fileAndLine.length >= 2) {
							if (new File(fileAndLine[0] + "pl").exists()) {
								try {
									int line = Integer.parseInt(fileAndLine[1]) - 1;
									try {
										IEditorPart editor = UIUtils.openInEditor(fileAndLine[0] + "pl");
										if (!(editor instanceof AbstractTextEditor)) {
											return;
										}
										
										AbstractTextEditor textEditor = (AbstractTextEditor) editor;
										IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
										IRegion region = document.getLineInformation(line);
										ISelection selection = new TextSelection(document,region.getOffset(),region.getLength());
										textEditor.getEditorSite().getSelectionProvider().setSelection(selection);
									} catch(Exception ex){
									}
								} catch (NumberFormatException ex) {
								}
							}
						}
					}
				} catch (IllegalArgumentException ex) {
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
			
			private StyleRange findStyleRangeForOffset(StyleRange[] styles, int offset) {
				if (styles == null) {
					return null;
				}
				for (StyleRange style: styles) {
					if (style.start <= offset && offset <= style.start + style.length) {
						return style;
					}
				}
				return null;
			}
		});
		control.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				;
			}

		});
		

		control.setEnabled(model != null && model.isConnected());
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public Object getInput() {
		return model;
	}

	@Override
	public ISelection getSelection() {
		return new _TextSelection(control);
	}

	@Override
	public void refresh() {
		; // something to do? dunno.
	}

	@Override
	public void setInput(Object input) {
		ConsoleModel m = null;
		if (input instanceof ConsoleModel) {
			m = (ConsoleModel) input;
		}
		setModel(m);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		ITextSelection s = null;
		if (selection instanceof ITextSelection) {
			s = (ITextSelection) selection;
		}
		if (s == null) {
			return;
		}
		control.setSelectionRange(s.getOffset(), s.getLength());
		if (reveal) {
			control.showSelection();
		}

	}

	/**
	 * @param completionProvider
	 *            The completionProvider to set.
	 */
	public void setCompletionProvider(ConsoleCompletionProvider completionProvider) {
		this.completionProvider = completionProvider;
	}

	/**
	 * @param history
	 *            The history to set.
	 */
	public void setHistory(ConsoleHistory history) {
		this.history = history;
		if (history != null) {
			history.setConsoleModel(model);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cs3.pl.views.ConsoleController#setModel(org.cs3.pl.views.ConsoleModel
	 * )
	 */
	public void setModel(ConsoleModel consoleModel) {
		if (model == consoleModel) {
			return;
		}
		if (model != null) {
			model.removeConsoleListener(this);
		}
		this.model = consoleModel;
		if (model != null) {
			model.addConsoleListener(this);
			ui_setSingleCharMode(model.isSingleCharMode());
			ui_setEnabled(model.isConnected());
		} else {
			ui_setEnabled(false);
		}

		if (history != null) {
			history.setConsoleModel(model);
		}
	}

	public static final class SavedState {
		private int startOfInput;
		private String contents;
		private ConsoleHistory history;
		private ConsoleModel model;
		public ConsoleCompletionProvider completionProvider;
		public int caretPosition;
		private StyleRange[] styleRanges;

	}

	public SavedState saveState() {
		if (control.isDisposed()) {
			return null;
		}
		SavedState s = new SavedState();
		s.startOfInput = startOfInput;
		s.contents = control.getText();
		s.styleRanges = control.getStyleRanges();
		s.history = history;
		s.model = model;
		s.completionProvider = completionProvider;
		s.caretPosition = control.getCaretOffset();
		return s;
	}

	public void loadState(SavedState s) {
		thatWasMe = true;

		startOfInput = s.startOfInput;
		control.setText(s.contents);
		control.setStyleRanges(s.styleRanges);
		control.setCaretOffset(s.caretPosition);
		setHistory(s.history);
		setModel(s.model);
		setCompletionProvider(s.completionProvider);
		control.showSelection();
		thatWasMe = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.cs3.pl.views.ConsoleModelListener#onCommit(org.cs3.pl.views.
	 * ConsoleModelEvent)
	 */
	@Override
	public void onCommit(final ConsoleModelEvent e) {
		Display display = control.getDisplay();
		if (Display.getCurrent() != display) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					onCommit(e);
				}
			});
		} else {
			ui_setLineBuffer("");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cs3.pl.views.ConsoleModelListener#onEditBufferChanged(org.cs3.pl.
	 * views.ConsoleModelEvent)
	 */
	@Override
	public void onEditBufferChanged(final ConsoleModelEvent e) {
		if (control == null) {
			Debug.warning("no UI, dropping EditBufferChange: " + e.getNewLineState());
			return;
		}
		Display display = control.getDisplay();
		if (display == null) {
			Debug.warning("UI seems to be unavailable. dropping EditBufferChange: " + e.getNewLineState());
			return;
		}
		if (Display.getCurrent() != display) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					onEditBufferChanged(e);
				}
			});
		} else if (!thatWasMe) {
			String text = e.getNewLineState();
			ui_setLineBuffer(text == null ? "" : text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.cs3.pl.views.ConsoleModelListener#onModeChange(org.cs3.pl.views.
	 * ConsoleModelEvent)
	 */
	@Override
	public void onModeChange(final ConsoleModelEvent e) {
		Debug.debug("mode changed: " + model.isSingleCharMode());
		Display display = control.getDisplay();
		if (Display.getCurrent() != display) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					onModeChange(e);
				}
			});
		} else {
			try {
				ui_setSingleCharMode(model.isSingleCharMode());
			} catch (Throwable t) {
				Debug.report(t);
				throw new RuntimeException(t);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.cs3.pl.views.ConsoleModelListener#onOutput(org.cs3.pl.views.
	 * ConsoleModelEvent)
	 */
	@Override
	public void onOutput(final ConsoleModelEvent e) {
		if (control == null || control.isDisposed()) {
			return;
		}
		Display display = control.getDisplay();
		if (Display.getCurrent() != display) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					onOutput(e);
				}
			});
		} else {
			ui_appendOutput(e.getOutput());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cs3.pl.console.ConsoleModelListener#afterConnect(org.cs3.pl.console
	 * .ConsoleModelEvent)
	 */
	@Override
	public void afterConnect(final ConsoleModelEvent e) {
		Display display = control.getDisplay();
		if (Display.getCurrent() != display) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					afterConnect(e);
				}
			});
		} else {
			ui_setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cs3.pl.console.ConsoleModelListener#beforeDisconnect(org.cs3.pl.console
	 * .ConsoleModelEvent)
	 */
	@Override
	public void beforeDisconnect(final ConsoleModelEvent e) {
		if (control.isDisposed()) {
			return;
		}
		Display display = control.getDisplay();
		if (Display.getCurrent() != display) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					beforeDisconnect(e);
				}
			});
		} else {
			ui_setEnabled(false);
		}
	}

	private void completionAvailable(CompletionResult r) {

		if (!model.getLineBuffer().equals(r.getOriginalLineContent())) {
			Debug.debug("completion discarded.");
			return;
		}
		if (control.getCaretOffset() - startOfInput != r.getOriginalCaretPosition()) {
			Debug.debug("completion discarded.");
			return;
		}

		String[] options = r.getOptions();
		Debug.debug("found " + options.length + " completions");
		model.setLineBuffer(r.getNewLineContent());
		control.setCaretOffset(r.getNewCaretPosition() + startOfInput);

		if (options.length > 1) {
			StringBuffer buf = new StringBuffer();
			buf.append("\n");
			for (int i = 0; i < options.length; i++) {
				buf.append(options[i]);
				buf.append("\t\t\t\t\t\t\t\t");
			}
			buf.append("\n");
			ui_appendOutput(buf.toString());
		}

	}

	private void doCompletion() {
		if (completionProvider == null) {
			return;
		}

		final int caretPosition = control.getCaretOffset() - startOfInput;
		Runnable work = new Runnable() {
			@Override
			public void run() {

				final CompletionResult r = completionProvider.doCompletion(model.getLineBuffer(), caretPosition);
				final Runnable notify = new Runnable() {
					@Override
					public void run() {
						completionAvailable(r);
					}
				};
				control.getDisplay().asyncExec(notify);
			}
		};

		new Thread(work, "Console Completion Worker").start();

	}

	private void setColorRangeInControl(int start, int end, Color col) {
		StyleRange range = new StyleRange(start, end, col, control.getBackground());
		control.setStyleRange(range);
	}

	private boolean lineStartsWith(String line, String start) {
		if (line.startsWith(start, 0)) {
			return true;
		}
		if (line.startsWith(" " + start, 0)) {
			return true;
		}
		if (Boolean.valueOf(PrologConsolePlugin.getDefault().getPreferenceValue(PDTConsole.PREF_CONSOLE_COLORS_THREESTARS, "true")).booleanValue()) {
			if (line.startsWith(PLACEHOLDER_THREESTARS + start, 0)) {
				return true;
			}
			if (line.startsWith(PLACEHOLDER_THREESTARS + " " + start, 0)) {
				return true;
			}
			if (line.startsWith(" " + PLACEHOLDER_THREESTARS + start, 0)) {
				return true;
			}
			if (line.startsWith(" " + PLACEHOLDER_THREESTARS + " " + start, 0)) {
				return true;
			}

		}
		return false;
	}

	private void ui_appendOutput(String output) {
		thatWasMe = true;
		int p = 0;
		try {
			p = control.getCaretOffset() - startOfInput;

			control.replaceTextRange(startOfInput, 0, output);

			String[] Rows = output.split("\n");

			
			if (coloringEnabled) {
				int CharCount = 0;

				String row;

				// for (String row : Rows) {
				for (int i = 0; i < Rows.length; i++) {
					row = Rows[i];
					// Get the Color-Information
					String UpperCaseRow = row.toUpperCase();
					if (lineStartsWith(UpperCaseRow, PLACEHOLDER_LGT_WARNING) &&
						row.contains(PLACEHOLDER_LGT_UNDEFINED)) {
						LastOutputColor = COLOR_DEBUG;
					} else if (lineStartsWith(UpperCaseRow, PLACEHOLDER_WARNING) ||
						lineStartsWith(UpperCaseRow, PLACEHOLDER_LGT_WARNING)) {
						LastOutputColor = COLOR_WARNING;
					} else if (lineStartsWith(UpperCaseRow, PLACEHOLDER_ERROR)) {
						LastOutputColor = COLOR_ERROR;
					} else if (lineStartsWith(UpperCaseRow, PLACEHOLDER_DEBUG)) {
						LastOutputColor = COLOR_DEBUG;
					} else if (lineStartsWith(UpperCaseRow, PLACEHOLDER_INFO)) {
						LastOutputColor = COLOR_INFO;
					} else if (lineStartsWith(UpperCaseRow, PLACEHOLDER_THREESTARS)) {
						LastOutputColor = COLOR_INFO;
					} else if (!UpperCaseRow.startsWith("\u0009")
					// tabChar oder zwei leerzeichen
							&& !UpperCaseRow.startsWith(PLACEHOLDER_SPACETAB, 0) && LineFeedOccured) {
						// No Color Setting, take default color
						LastOutputColor = null;
					}
					// SET the Color-Information
					if (LastOutputColor != null) {
						setColorRangeInControl(startOfInput + CharCount, row.length(), LastOutputColor);
						
						Position location = getLocation(row);
						if (location != null) {
							int start = startOfInput + CharCount + location.offset;
							StyleRange range = new StyleRange(start, location.length, LastOutputColor, control.getBackground());
							range.underline = true;
							range.underlineStyle = SWT.UNDERLINE_LINK;
							control.setStyleRange(range);
						}
					}

					CharCount += row.length() + 1;

				}
				// is needed for long lines which are pushed in multiple parts
				if (output.endsWith("\n")) {
					LineFeedOccured = true;
				} else {
					LineFeedOccured = false;
				}
			}

			startOfInput += output.length();

			control.setCaretOffset(startOfInput + p);
			control.showSelection();
		} catch (Throwable e) {
			Debug.report(e);
			throw new RuntimeException(e);
		}
		thatWasMe = false;

	}

	private void ui_setLineBuffer(String string) {
		thatWasMe = true;
		int len = control.getContent().getCharCount() - startOfInput;
		try {
			control.replaceTextRange(startOfInput, len, string);
		} catch (Throwable t) {
			Debug.report(t);
			throw new RuntimeException(t);
		}
		control.setCaretOffset(control.getCharCount());
		control.showSelection();
		thatWasMe = false;
	}

	private void ui_setSingleCharMode(boolean b) {
		Display display = control.getDisplay();
		if (b) {
			control.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		} else {
			control.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		}
	}

	private void ui_setEnabled(boolean b) {
		control.setEnabled(b);
		Display display = control.getDisplay();
		if (b) {
			control.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		} else {
			control.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
		}
	}

	protected void ui_inputModificationIntercepted(VerifyEvent e) {
		if (thatWasMe) {
			return;
		}
		if (model == null) {
			e.doit = false;
		} else if (e.start < startOfInput) {
			// allow insertion (put the new text at the very end of the buffer)
			// but do not allow modification of text before startOfInput.
			e.doit = false;
			if (e.end == e.start) {// insertion
				control.replaceTextRange(control.getCharCount(), 0, e.text);
			}
		} else {
			e.doit = true;
		}

	}

	protected void ui_keyStrokeIntercepted(VerifyEvent event) {
		int keyMask = event.stateMask;
		try {
			if (thatWasMe) {
				return;
			}
			int keyCode = event.keyCode;
			int keyChar = event.character;

			if ((keyCode & SWT.MODIFIER_MASK) == 0 && control.getCaretOffset() < startOfInput) {

				control.setCaretOffset(control.getCharCount());
			}
			if (model == null) {
				event.doit = false;
				return;
			}
			if (model.isSingleCharMode()) {
				event.doit = false;

			} else {
				int off = getCaretOffset();
				switch (keyCode) {
				case SWT.HOME:
					event.doit = true;
					break;
				case SWT.KEYPAD_7:
					if (isNumLock(keyMask, keyCode, keyChar)) {
						break;
					}
					event.doit = true;
					break;
				case SWT.ARROW_LEFT:
					event.doit = off > startOfInput;
					break;
				case SWT.KEYPAD_4:
					if (isNumLock(keyMask, keyCode, keyChar)) {
						break;
					}
					event.doit = off > startOfInput;
					break;
				case SWT.CR:
				case SWT.KEYPAD_CR:
					event.doit = false;
					break;

				case SWT.ARROW_UP:
					event.doit = false;
					break;
				case SWT.KEYPAD_8:
					if (isNumLock(keyMask, keyCode, keyChar)) {
						break;
					}
					event.doit = false;
					break;

				case SWT.ARROW_DOWN:
					event.doit = false;
					break;
				case SWT.KEYPAD_2:
					if (isNumLock(keyMask, keyCode, keyChar)) {
						break;
					}
					event.doit = false;
					break;

				case ' ':
					if ((keyMask & SWT.CTRL) != 0) {
						doCompletion();
						event.doit = false;
					}
					break;
				case SWT.TAB:
					doCompletion();
					event.doit = false;
					break;
				default:
					event.doit = true;
					break;
				}
			}
		} catch (Exception e) {
			Debug.report(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * due to a known problem with swt, it is currently not possible to simply
	 * poll the numlock state.
	 * 
	 * this method tries to work around this limitation by not actualy looking
	 * at the num lock state, but testing, wether the pressed key is one of the
	 * keypad numbers and wether the resulting character is a digit.
	 */
	private boolean isNumLock(int keyMask, int keyCode, int keyChar) {

		if (keyCode >= SWT.KEYPAD_0 && keyCode <= SWT.KEYPAD_9) {
			return keyChar >= 0 && Character.isDigit((char) keyChar);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cs3.pl.views.ConsoleController#inputModified(java.lang.String)
	 */
	public void ui_inputModified(String newInput) {
		if (!thatWasMe) {
			thatWasMe = true;
			model.setLineBuffer(newInput);
			thatWasMe = false;
		}
	}

	protected String ui_getLineBuffer() {
		int charCount = control.getContent().getCharCount();
		return control.getContent().getTextRange(startOfInput, charCount - startOfInput);
	}

	protected void ui_keyPressed(KeyEvent e) {
		char keyChar = e.character;
		int keyCode = e.keyCode;

		int keyMask = e.stateMask;
		if (thatWasMe) {
			return;
		}

		if (model == null) {
			return;
		}
		if (model.isSingleCharMode() && keyChar > 0) {
			Debug.debug("keyChar: '" + keyChar + "'");
			if (enterSendsSemicolon && (keyCode == SWT.CR || keyCode == SWT.KEYPAD_CR)) {
				model.putSingleChar(';');
			} else {
				model.putSingleChar(keyChar);
			}
		} else {
			switch (keyCode) {
			case SWT.HOME:
			case SWT.KEYPAD_7:
				if (isNumLock(keyMask, keyCode, keyChar)) {
					break;
				}
				if ((keyMask & SWT.SHIFT) != 0) {

					Point range = control.getSelectionRange();
					int to = range.x + range.y;
					int from = startOfInput;
					control.setCaretOffset(startOfInput);
					control.setSelectionRange(to, from - to);
				} else {
					control.setCaretOffset(startOfInput);
				}
				break;

			case SWT.CR:
			case SWT.KEYPAD_CR:
				model.commitLineBuffer();
				break;
			case SWT.ARROW_UP:
				Debug.debug("UP");
				history.previous();
				break;
			case SWT.KEYPAD_8:
				if (isNumLock(keyMask, keyCode, keyChar)) {
					break;
				}
				Debug.debug("UP");
				history.previous();
				break;
			case SWT.ARROW_DOWN:
				history.next();
				break;
			case SWT.KEYPAD_2:
				if (isNumLock(keyMask, keyCode, keyChar)) {
					break;
				}
				history.next();
				break;
			case SWT.TAB:
				break;
			default:
				break;
			}
		}

	}

	public ConsoleCompletionProvider getCompletionProvider() {
		return completionProvider;
	}

	public ConsoleHistory getHistory() {
		return history;
	}

	public ConsoleModel getModel() {
		return model;
	}

	public int getLineCount() {
		return control.getLineCount();
	}

	public void clearOutput() {
		thatWasMe = true;
		int c = control.getCaretOffset() - startOfInput;
		control.getContent().replaceTextRange(0, startOfInput, "");
		startOfInput = 0;
		control.setCaretOffset(c);
		thatWasMe = false;

	}

	public int getOffsetAtLine(int line) {
		return control.getOffsetAtLine(line);
	}

	public int getLineAtOffset(int offset) {
		return control.getLineAtOffset(offset);
	}

	public String getText() {
		return control.getText();
	}

	public String getTextRange(int offset, int length) {
		return control.getTextRange(offset, length);
	}

	public int getCaretOffset() {
		return control.getCaretOffset();
	}

	public void setCaretOffset(int offset) {
		control.setCaretOffset(offset);
	}

	public void cut() {
		control.cut();

	}

	public void copy() {
		control.copy();

	}

	public void paste() {
		control.paste();

	}

	public void selectAll() {
		control.selectAll();

	}

	public void setEnterSendsSemicolon(boolean useEnter) {
		this.enterSendsSemicolon = useEnter;

	}

	public boolean getEnterSendsSemicolon() {
		return enterSendsSemicolon;
	}

	public int getStartOfInput() {
		return startOfInput;
	}

	public void setEnabled(final boolean b) {
		if (Display.getCurrent() == control.getDisplay()) {
			ui_setEnabled(b);
		} else {
			control.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					ui_setEnabled(b);
				}

			});
		}

	}
	
	private Position getLocation(String line) {
		try {
			int end = line.indexOf(".pl:");
			if (end == -1) {
				return null;
			} else {
				end += 4;
			}
			int start = getReferencedFilename(line);
			while(line.charAt(end) >= '0' && line.charAt(end) <= '9')
				end ++;

//			targetLine =  Integer.parseInt(line.substring(start,end))-1;  
//			filename = getReferencedFilename(line);
			return new Position(start, end - start);
		} catch(Exception e) {
			return null;
		}
	}
	
	private int getReferencedFilename(String line) {
		int start = 0;
		while(notPartOfFileName(line.charAt(start)))
			start++;
		while(notStartOfFileName(line,start))
			start++;
//		int end = line.indexOf(".pl:") + 3;
//		return line.substring(start, end);
		return start;
		
	}
	
	private boolean notStartOfFileName(String line, int offset) {
		if(line.charAt(offset) == '/')
			return false;
		if(line.length() > offset+2 && 
				line.charAt(offset + 1) == ':' &&
				line.charAt(offset + 2) == '/')
			return false;
		return true;
	}
	
	private boolean notPartOfFileName(char c) {
		switch(c) {
			case ' ':
			case '(':
			case '\t':
			case '\n':
			case '\r':
				return true;
			default:
				return false;
		}
	}
	
}
