/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Tobias Rho, Lukas Degener, Andreas Becker, Fabian Noth
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package org.cs3.prolog.common;

// Guenter Kniesel, 23.9.2010: Adapted to changes in SWI-Prolog 5.9.9: 
// Reordered so that the values in the newer releases are the defaults. 
public class PDTConstants {
	
	// with graphic environment: First value is default!
	public static final String WINDOWS_EXECUTABLES = "swipl-win,plwin";           // plwin -> swipl-win since 5.9.9

	// without graphic environment: First value is default!
	public static final String WINDOWS_COMMAND_LINE_EXECUTABLES = "swipl,plcon";  // plcon -> swipl since 5.9.9

	public static final String STACK_COMMMAND_LINE_ARGUMENTS = "-L4m -G4m -T4m";
	
	// With or without graphic environment: First value is default!
	public static final String UNIX_COMMAND_LINE_EXECUTABLES = "swipl,xpce";      // xpce deleted since 5.9.9

}

/*
From: Jan Wielemaker <J.Wielemaker@cs.vu.nl>
To: swi-prolog@iai.uni-bonn.de
Date: Wed, 24 Feb 2010 11:38:34 +0100 
Subject: [SWIPL] Ann: SWI-Prolog 5.9.9

Finally. SWI-Prolog 5.9.9 has been released.  
There are some incompatible changes, notably using 
the same executable name on all platforms. 

	 * MODIFIED: Renaming executables for better system interoperability:
	   	pl   --> swipl
	   	plld --> swipl-ld
	   	plrc --> swipl-rc
		xpce --> <deleted>
		libpl.dll --> swipl.dll
	  Windows:
		plwin.exe --> swipl-win.exe
		plcon.exe --> swipl.exe
		
	 * MODIFIED: The default stack-limit has been changed to 128Mb per
	   stack on 32-bit and 256Mb on 64-bit hardware. This probably 
	   results in fewer complaints about running out-of-stack.
*/


