package org.cs3.pdt.internal.contentassistant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cs3.pdt.internal.editors.PLPartitionScanner;
import org.cs3.prolog.PrologInterfaceException;
import org.cs3.prolog.common.Util;
import org.cs3.prolog.common.logging.Debug;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public abstract class PrologContentAssistProcessor {

	public PrologContentAssistProcessor() {
		super();
	}

	protected abstract IFile getFile() throws CoreException;
	
	private class Prefix {
		int begin;
		int length;
		String prefix;
				
		Prefix(IDocument document, int begin, String prefix) {
			this.begin=begin;
			this.prefix=prefix;
			this.length=prefix.length();
		}
	}
		
	protected abstract void addPredicateProposals(IDocument document, int begin,
			int len, String prefix, List<ComparableCompletionProposal> proposals, String module)
			throws PrologInterfaceException, CoreException;

	protected abstract void addVariableProposals(IDocument document, int begin,
			int len, String prefix, List<ComparableCompletionProposal> proposals) throws BadLocationException, PrologInterfaceException, CoreException;

	private Prefix calculatePrefix(IDocument document, int offset)
			throws BadLocationException {
				int begin=offset;
				int length=0;
				boolean isPredChar = Util.isNonQualifiedPredicateNameChar(document.getChar(begin));
				
				while (isPredChar){
					length++;
					int test = begin-1;
					if(test >=0){
						isPredChar = Util.isNonQualifiedPredicateNameChar(document.getChar(test));
						if(!isPredChar){
							break;
						}
					} else {
						break;
					}
					begin=test;
				}
				String pre = document.get(begin, length);
				
				Prefix prefix = new Prefix(document,begin,pre);
				return prefix;
			}

	private String retrievePrefixedModule(int documentOffset, IDocument document, int begin)
			throws BadLocationException {
				if (begin>0 && document.getChar(begin - 1) == ':') {
					int moduleBegin = begin - 2;
					while (Util.isNonQualifiedPredicateNameChar(document
							.getChar(moduleBegin))
							&& moduleBegin > 0)
						moduleBegin--;
					String moduleName = document.get(moduleBegin + 1, documentOffset - moduleBegin);
					if(!Util.isVarPrefix(moduleName)){
						return moduleName;
					}
				}
				return null;
			}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
	
		try {
			IDocument document = viewer.getDocument();
	
			documentOffset = documentOffset == 0 ? documentOffset
					: documentOffset - 1;
	
			Prefix pre = calculatePrefix(document,documentOffset);
	
			String module = retrievePrefixedModule(documentOffset - pre.length - 1,
					document, pre.begin);
			
			List<ComparableCompletionProposal> proposals = 
					new ArrayList<ComparableCompletionProposal>();
			if (module == null || module.equals("")) {
				if (pre.prefix.equals("")) {
					return null;
				}
				addVariableProposals(document, pre.begin, pre.length, pre.prefix, proposals);
			}
			addPredicateProposals(document, pre.begin, pre.length, pre.prefix, proposals,
					module);
	
			if (proposals.size() == 0)
				return null;
			Collections.sort(proposals);
			return proposals
					.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			Debug.report(e);
//			UIUtils.logAndDisplayError(PDTPlugin.getDefault()
//					.getErrorMessageProvider(), viewer.getTextWidget()
//					.getShell(), PDT.ERR_COMPLETION_BAD_LOCATION,
//					PDT.CX_COMPLETION, e);
			return null;
		} catch (PrologInterfaceException e) {
			Debug.report(e);
//			UIUtils.logAndDisplayError(PDTPlugin.getDefault()
//					.getErrorMessageProvider(), viewer.getTextWidget()
//					.getShell(), PDT.ERR_PIF, PDT.CX_COMPLETION, e);
			return null;
		} catch (CoreException e) {
			Debug.report(e);
//			UIUtils.logAndDisplayError(PDTPlugin.getDefault()
//					.getErrorMessageProvider(), viewer.getTextWidget()
//					.getShell(), PDT.ERR_CORE_EXCEPTION, PDT.CX_COMPLETION, e);
			return null;
		} finally {
	
		}
	}

	protected boolean isComment(ITypedRegion region) {
		return region.getType().equals(PLPartitionScanner.PL_COMMENT)
				|| region.getType().equals(PLPartitionScanner.PL_MULTI_COMMENT);
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
	
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
	
		return new char[0];
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return new char[0];
	}

	public IContextInformationValidator getContextInformationValidator() {
		class Validator implements IContextInformationValidator {
	
			@Override
			public boolean isContextInformationValid(int position) {
				return true;
			}
	
			@Override
			public void install(IContextInformation info, ITextViewer viewer,
					int documentPosition) {
				;
	
			}
		}
		return new Validator();
	}

	public String getErrorMessage() {
		return "Error Message?";
	}

}