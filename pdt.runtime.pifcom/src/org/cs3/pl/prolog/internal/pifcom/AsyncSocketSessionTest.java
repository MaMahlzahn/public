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

package org.cs3.pl.prolog.internal.pifcom;

import java.util.Iterator;
import java.util.Vector;

import junit.framework.TestCase;

import org.cs3.pl.common.Debug;
import org.cs3.pl.common.Util;
import org.cs3.pl.prolog.AsyncPrologSession;
import org.cs3.pl.prolog.AsyncPrologSessionEvent;
import org.cs3.pl.prolog.AsyncPrologSessionListener;
import org.cs3.pl.prolog.PrologException;
import org.cs3.pl.prolog.PrologInterface2;
import org.cs3.pl.prolog.PrologInterfaceException;
import org.cs3.pl.prolog.PrologInterfaceFactory;
import org.cs3.pl.prolog.PrologSession;

public class AsyncSocketSessionTest extends TestCase {

	private PrologInterface2 pif;

	private Recorder rec;

	private AsyncPrologSession session;

	protected void setUp() throws Exception {
		Debug.setDebugLevel(Debug.LEVEL_DEBUG);
		PrologInterfaceFactory factory = Factory.newInstance();
		pif = (PrologInterface2) factory.create();
		//pif.setOption(SocketPrologInterface.EXECUTABLE, "konsole -e xpce");
		pif.setOption(SocketPrologInterface.HIDE_PLWIN, "false");
		pif.start();
		rec = new Recorder();
		session = pif.getAsyncSession();
		session.addBatchListener(rec);
	}

	protected void tearDown() throws Exception {
		pif.stop();
	}

	class Record {
		String method;

		AsyncPrologSessionEvent event;

		public Record(String method, AsyncPrologSessionEvent event) {
			this.method = method;
			this.event = event;
		}
	}

	class Recorder implements AsyncPrologSessionListener {
		public void clear() {
			records.clear();
		}

		public synchronized Record last() {
			return (Record) records.lastElement();
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for (Iterator it = records.iterator(); it.hasNext();) {
				Record r = (Record) it.next();
				if (!first) {
					sb.append(", ");
				}
				sb.append(r.method);
				sb.append('(');
				if (r.event.ticket instanceof String) {
					sb.append(r.event.ticket == null ? "null" : r.event.ticket
							.toString());
				} else {
					sb.append(r.event.ticket == null ? "null" : "dummy");
				}
				sb.append(',');
				sb.append(r.event.message == null ? "null" : Util
						.hideStreamHandles(r.event.message, "$stream(_)"));
				sb.append(',');
				sb.append(r.event.bindings == null ? "null" : "("
						+ Util.prettyPrint(r.event.bindings) + ")");
				sb.append(')');

				first = false;
			}
			return sb.toString();
		}

		Vector records = new Vector();

		public synchronized void joinComplete(AsyncPrologSessionEvent e) {
			records.add(new Record("joinComplete", e));
			notifyAll();
		}

		public synchronized void abortComplete(AsyncPrologSessionEvent e) {
			records.add(new Record("abortComplete", e));
			notifyAll();
		}

		public synchronized void goalSucceeded(AsyncPrologSessionEvent e) {
			records.add(new Record("goalSucceeded", e));
			notifyAll();
		}

		public synchronized void goalFailed(AsyncPrologSessionEvent e) {
			records.add(new Record("goalFailed", e));
			notifyAll();
		}

		public synchronized void goalRaisedException(AsyncPrologSessionEvent e) {
			records.add(new Record("goalRaisedException", e));
			notifyAll();
		}

		public synchronized void goalHasSolution(AsyncPrologSessionEvent e) {
			records.add(new Record("goalHasSolution", e));
			notifyAll();
		}

		public synchronized void goalSkipped(AsyncPrologSessionEvent e) {
			records.add(new Record("goalSkipped", e));
			notifyAll();
		}

		public synchronized void goalCut(AsyncPrologSessionEvent e) {
			records.add(new Record("goalCut", e));
			notifyAll();
		}

		public synchronized void batchComplete(AsyncPrologSessionEvent e) {
			records.add(new Record("batchComplete", e));
			notifyAll();
		}

	}

	public void test_queryOnce_sequence01() throws Throwable {
		// PrologSession session=pif.getSession();
		session.queryOnce("1", "member(A,[a,b,c])");
		session.queryOnce("2", "member(a,[a,b,c])");
		session.queryOnce("3", "member(a,[a,b,c)");
		session.queryOnce("4", "member(aA,[a,b,c])");
		session.dispose();
		assertEquals(
				"goalHasSolution(1,null,(A-->a)), "
						+ "goalSucceeded(1,null,null), "
						+ "goalHasSolution(2,null,()), "
						+ "goalSucceeded(2,null,null), "
						+ "goalRaisedException(3,error(syntax_error(cannot_start_term), stream($stream(_), 9, 0, 116)),null), "
						+ "goalFailed(4,null,null), "
						+ "batchComplete(null,null,null)", rec.toString());
	}

	public void test_queryAll_sequence01() throws Throwable {
		// PrologSession session=pif.getSession();
		session.queryAll("1", "member(A,[a,b,c])");
		session.queryAll("2", "member(a,[a,b,c])");
		session.queryAll("3", "member(a,[a,b,c)");
		session.queryAll("4", "member(aA,[a,b,c])");
		session.dispose();
		assertEquals(
				"goalHasSolution(1,null,(A-->a)), "
						+ "goalHasSolution(1,null,(A-->b)), "
						+ "goalHasSolution(1,null,(A-->c)), "
						+ "goalSucceeded(1,null,null), "
						+ "goalHasSolution(2,null,()), "
						+ "goalSucceeded(2,null,null), "
						+

						"goalRaisedException(3,error(syntax_error(cannot_start_term), stream($stream(_), 9, 0, 113)),null), "
						+ "goalFailed(4,null,null), "
						+ "batchComplete(null,null,null)", rec.toString());
	}

	public void test_longAtom() throws Throwable {
		// session.queryOnce("0", "guitracer");
		// session.queryOnce("0", "trace");
		// session.join();
		// rec.clear();
		StringBuffer sb = new StringBuffer();
		sb.append("atom(");
		for (int i = 0; i < 600000; i++) {
			sb.append('a');
		}
		sb.append(")");

		session.queryOnce("1", sb.toString());
		session.join();
		assertEquals(
				"goalHasSolution(1,null,()), goalSucceeded(1,null,null), joinComplete(dummy,null,null)",
				rec.toString());
	}

	public void test_pending_during_queryall() throws Throwable {
		String alias = session.getProcessorThreadAlias();
		String ticket = "queryall";
		session.queryAll(ticket, "repeat,writeln(waiting),thread_get_message(test(M)),writeln(got(M)),(M==stop,!;true)");
		PrologSession syncSession = pif.getSession();
		rec.clear();
		synchronized (rec) {
			syncSession.queryOnce("writeln('i am here'),thread_send_message('" + alias
					+ "',test(1))");
			rec.wait();
		}
		boolean pending1 = session.isPending(ticket);
		synchronized (rec) {
			syncSession.queryOnce("thread_send_message('" + alias
					+ "',test(stop))");
			rec.wait();
		}
		session.join();
		boolean pending2 = session.isPending(ticket);
		assertEquals("not pending during queryall",true,pending1);
		assertEquals("pending after queryall",false,pending2);
	}
	
	/*
	 * have one query with a blocking io call.
	 * queue a second one, which serves as a dummy.
	 * abort the batch. 
	 * the processor cannot recieve the async abort request while
	 * the first query blocks.
	 * unlock the first query.
	 * the processor should now recieve the async abort request, it should
	 * skip the second goal.
	 */
	public void test_abort01() throws Throwable {
		session.queryOnce("1", "thread_self(Alias)");
		session.join();
		Record r = (Record) rec.records.get(0);
		final String alias = (String) r.event.bindings.get("Alias");
		//session.queryOnce("debug", "guitracer");
		//session.queryOnce("debug", "spy(handle_batch_command)");
		session.queryAll("2", "repeat,thread_get_message(test(M))");
		final PrologSession syncSession = pif.getSession();

		synchronized (rec) {
			syncSession.queryOnce("thread_send_message('" + alias
					+ "',test(1))");
			rec.wait();
		}

		rec.clear();
		assertFalse(session.isIdle());
		session.queryOnce("3", "should_be_skipped");
		final Object lock = new Object();

		synchronized (lock) {
			Thread thread = new Thread() {
				public void run() {

					// we need to make sure that test(2) (see below) is send
					// AFTER
					// the abort call, otherwise, abort will lock up forever.
					Debug.debug("enter 3");
					synchronized (lock) {
					Debug.debug("enter 5");						
						try {
							
							
							Debug.debug("enter 6");
							syncSession.queryOnce("thread_send_message('" + alias
									+ "',test(2))");
						} catch (PrologException e) {
							Debug.report(e);
							
						} catch (PrologInterfaceException e) {
							Debug.report(e);
						} 
					}
						
				}
			};
			Debug.debug("enter 0");
			thread.start();
			Debug.debug("enter 1");
			session.abort(lock);
			Debug.debug("enter 7");	
		}
			// session.queryOnce("toggle uitracer","guitracer");
			// session.queryOnce("start tracer","trace");


		
		
		session.dispose();
		assertEquals("goalHasSolution(2,null,(M-->2)), "
				+ "goalCut(2,null,null), " + "goalSkipped(3,null,null), "
				+ "abortComplete(dummy,null,null), "
				+ "batchComplete(null,null,null)", rec.toString());

	}

	public void test_abort02() throws Exception{
		session.abort();
		session.dispose();

	}
	
	public void test_setProtocolOption() throws Exception{
		session.setPreferenceValue("socketsession.canonical", "true");

	}
	
	public void test_manyAsyncSessions() throws Throwable{
		int N= 50;
		AsyncPrologSession[] sessions = new AsyncPrologSession[N];
		for(int i=0;i<N;i++){
			sessions[i]=pif.getAsyncSession();
		}
		for(int i=0;i<N;i++){
			sessions[i].dispose();
		}
	}
}