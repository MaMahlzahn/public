/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others)
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

/*
 */
package org.cs3.pl.parser;


import junit.framework.TestCase;


public class StringLineBreakInfoProviderTest extends TestCase {
    public void test1()throws Throwable{
        String input = "Elephants forgot, force-fed on stale chalk,\n" +
        		"Ate the floors of their cages.\n" +
        		"Strongmen lost their hair, paybox collapsed and \n" +
        		"Lions sharpen their teeth.";
        int[] linebreaks=new int[]{0,43,74};
        StringLineBreakInfoProvider provider = new StringLineBreakInfoProvider (input,"\n");
        for(int i=0;i<linebreaks.length;i++){
            int offsetAtLine = provider.getOffsetAtLine(i);
            assertEquals(i==0? 0 : linebreaks[i]+1,offsetAtLine);
        }
    }
}


