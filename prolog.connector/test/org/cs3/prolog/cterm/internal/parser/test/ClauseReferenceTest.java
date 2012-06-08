package org.cs3.prolog.cterm.internal.parser.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.cs3.prolog.cterm.internal.parser.ASTAtom;
import org.cs3.prolog.cterm.internal.parser.CanonicalTermParser;
import org.cs3.prolog.cterm.internal.parser.Node;

public class ClauseReferenceTest extends TestCase {
    CanonicalTermParser termParser;
    InputStream inputStream;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
//		termParser = new CanonicalTermParser(inputStream);
	}
	
	public void testClauseReference() throws Exception{
		termParser=new CanonicalTermParser(mockStream("<clause>(02D193FC)"));
		termParser.Term();
		Node n =termParser.getASTRoot();
		
		
		ASTAtom expectedAtom = (ASTAtom)n;
		assertEquals("<clause>(02D193FC)",expectedAtom.getString());
	}
	
	
	private InputStream mockStream(String inputText){
		try {
			return new ByteArrayInputStream(inputText.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
