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


package org.cs3.pl.prolog;

import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.cs3.pdt.runtime.PrologRuntime;
import org.cs3.pdt.runtime.PrologRuntimePlugin;
import org.cs3.pl.cterm.CCompound;
import org.cs3.pl.cterm.CInteger;
import org.cs3.pl.cterm.CTerm;

public class PLUtilTest extends TestCase {
	private PrologSession session;

	protected void setUp() throws Exception {
		super.setUp();
		PrologRuntimePlugin plugin = PrologRuntimePlugin.getDefault();
		PrologInterface pif = plugin.getPrologInterface("test");
		this.session = pif.getSession(PrologInterface.CTERMS);
		PLUtil.configureFileSearchPath(plugin.getLibraryManager(), session, new String[]{PrologRuntime.LIB_COMMON});
		session.queryOnce("use_module(library('/org/cs3/pdt/util/pdt_util_rbtree'))");
		

	}
	
	public void testIterator() throws Throwable{
		Map map = session.queryOnce("pdt_rbtree_new(T0)," +
				"pdt_rbtree_insert(T0,a,0,T1)," +
				"pdt_rbtree_insert(T1,y,1,T2)," +
				"pdt_rbtree_insert(T2,x,2,T3)," +
				"pdt_rbtree_insert(T3,b,3,T4)," +
				"pdt_rbtree_insert(T4,z,4,T5)," +
				"pdt_rbtree_insert(T5,c,5,T6)");
		CTerm t=(CTerm) map.get("T6");
		Iterator it =PLUtil.rbtreeIterateNodes(t);
		StringBuffer sb =new StringBuffer();
		while(it.hasNext()){
			CCompound c = (CCompound) it.next();
			sb.append(c.getArgument(1).getFunctorValue());
			sb.append("->");
			sb.append(c.getArgument(2).getFunctorValue());
			if(it.hasNext()){
				sb.append(", ");
			}
		}
		assertEquals("a->0, b->3, c->5, x->2, y->1, z->4", sb.toString());
	}
	
	
	public void testLookup() throws Throwable{
		Map map = session.queryOnce("pdt_rbtree_new(T0)," +
				"pdt_rbtree_insert(T0,a,0,T1)," +
				"pdt_rbtree_insert(T1,y,1,T2)," +
				"pdt_rbtree_insert(T2,x,2,T3)," +
				"pdt_rbtree_insert(T3,b,3,T4)," +
				"pdt_rbtree_insert(T4,z,4,T5)," +
				"pdt_rbtree_insert(T5,c,5,T6)");
		CTerm t=(CTerm) map.get("T6");
		assertEquals(0, ((CInteger)PLUtil.rbtreeLookup(t, "a")).getIntValue());
		assertEquals(1, ((CInteger)PLUtil.rbtreeLookup(t, "y")).getIntValue());
		assertEquals(2, ((CInteger)PLUtil.rbtreeLookup(t, "x")).getIntValue());
		assertEquals(3, ((CInteger)PLUtil.rbtreeLookup(t, "b")).getIntValue());
		assertEquals(4, ((CInteger)PLUtil.rbtreeLookup(t, "z")).getIntValue());
		assertEquals(5, ((CInteger)PLUtil.rbtreeLookup(t, "c")).getIntValue());
		assertNull(PLUtil.rbtreeLookup(t, "bb"));
	}
}
