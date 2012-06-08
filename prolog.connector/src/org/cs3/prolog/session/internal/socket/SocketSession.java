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
 */
package org.cs3.prolog.session.internal.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cs3.prolog.common.Util;
import org.cs3.prolog.cterm.CTermUtil;
import org.cs3.prolog.pif.PrologException;
import org.cs3.prolog.pif.PrologInterface;
import org.cs3.prolog.pif.PrologInterfaceException;
import org.cs3.prolog.pif.internal.AbstractPrologInterface;
import org.cs3.prolog.pif.internal.socket.SocketClient;
import org.cs3.prolog.pif.internal.socket.SocketCommunicationConstants;
import org.cs3.prolog.session.PrologSession;

/**
 */
public class SocketSession implements PrologSession {
	private static final String RESULT_LIST = "ResultList";
	private SocketClient client;
	private boolean queryActive;
	private AbstractPrologInterface pif;
	private int flags;
	
	public SocketSession(SocketClient client, AbstractPrologInterface pif,int flags) {
		this.client = client;
		this.pif = pif;
		this.flags=flags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cs3.pl.prolog.PrologSession#dispose()
	 */
	@Override
	public void dispose() {
		if (isDisposed()) {
			return;
		}
		try {
			client.close();
		} catch (IOException e) {
			pif.error(e);
		} finally {
			client = null;
		}
	}

//	@Override
//	public List<Map<String, Object>> queryAll(String query) throws PrologException,
//		PrologInterfaceException {
//		CTermUtil.checkFlags(flags);
//		if (isDisposed()) {
//			throw new IllegalStateException("Session is disposed!");
//		}
//		if (query.length() == 0) {
//			return generateEmptyResults();
//		}
//		Vector<Map<String, Object>> results;
//		try {
//			configureProtocol(flags);
//			client.readUntil(SocketCommunicationConstants.GIVE_COMMAND);
//			client.writeln(SocketCommunicationConstants.QUERY_ALL);
//			client.readUntil(SocketCommunicationConstants.GIVE_TERM);
//			normalizeQuery(query);
//			results = readResults();
//		} catch (IOException e) {
//			throw pif.error(e);
//		} 
//		return results;
//	}
//	
//	private Vector<Map<String, Object>> readResults() throws IOException {
//	Vector<Map<String, Object>> results = new Vector<Map<String, Object>>();
//	Map<String, Object> result = read_solution(flags);
//	while (result != null) {
//		results.add(result);
//		result = read_solution(flags);
//	}
//	return results;
//}
//	
//	private List<Map<String, Object>> generateEmptyResults() {
//	List<Map<String, Object>> l = new Vector<Map<String, Object>>();
//	l.add(generateAnEmtpyResult());
//	return l;
//}
	
	
	@Override
	public List<Map<String, Object>> queryAll(String query) throws PrologInterfaceException {
		int oldflags = flags;
		flags = flags | PrologInterface.PROCESS_LISTS;
		
		if (query.endsWith(".")) {
			query = query.substring(0, query.length()-1);
		}
		List<String> vars = getVariableNames(query);
		String newQuery = createFindallQuery(query, vars);
		Map<String, Object> result = queryOnce(newQuery);
		flags=oldflags;
		return transformResults(result, vars);
	}

	private List<Map<String, Object>> transformResults(Map<String, Object> result,  List<String> vars) {
		List<Map<String, Object>> newResult = new ArrayList<Map<String,Object>>();
		
		List<List<Object>> listOfLists = (List<List<Object>>) result.get(RESULT_LIST);
		for (List<Object> list : listOfLists) {
			HashMap<String, Object> newMap = new HashMap<String, Object>();
			for (int i=0; i<vars.size(); i++) {
				newMap.put(vars.get(i), list.get(i));
			}
			newResult.add(newMap);
		}
		
		return newResult;
	}

	private String createFindallQuery(String query, List<String> vars) {
		StringBuffer buf = new StringBuffer();
		buf.append("findall([");
		boolean addComma = false;
		for (String v : vars) {
			if (addComma) {
				buf.append(", ");
			} else {
				addComma = true;
			}
			buf.append(v);
		}
		buf.append("], (");
		buf.append(query);
		buf.append("), " + RESULT_LIST + ")");
		return buf.toString();
	}

	private List<String> getVariableNames(String query) {
		try {
			String getVarQuery = "get_var_names(" + Util.quoteAtom(query) + ",Vars)";
			Map<String, Object> result = queryOnce(getVarQuery);
			String varString = result.get("Vars").toString();
			if (varString.isEmpty()) {
				return new ArrayList<String>();
			} else {
				return Arrays.asList(Util.unquoteAtom(varString).split(","));
			}
		} catch (PrologInterfaceException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public Map<String, Object> queryOnce(String query) throws PrologException,
	PrologInterfaceException {
		CTermUtil.checkFlags(flags);
		if (isDisposed()) {
			throw new IllegalStateException("Session is disposed!");
		}
		Map<String, Object> solution;
		if (query.length() == 0) {
			solution = generateAnEmtpyResult();
		} else {
			try {
				configureProtocol(flags);
				client.readUntil(SocketCommunicationConstants.GIVE_COMMAND);
				client.writeln(SocketCommunicationConstants.QUERY);
				client.readUntil(SocketCommunicationConstants.GIVE_TERM);
				normalizeQuery(query);
				solution = read_solution(flags);
				tryFinishReading();
			} catch (IOException e) {
				throw pif.error(e);
			}
		}
		return solution;
	}

	private void tryFinishReading() throws PrologInterfaceException {
		try {
			finishReading();
		} catch (IOException e) {
			throw pif.error(e);
		}
	}


	private void normalizeQuery(String query) throws IOException {
		query = query.trim();
		if (query.endsWith(".")) {
			client.writeln(query);
		} else {
			client.writeln(query + ".");
		}
	}

	private Map<String, Object> generateAnEmtpyResult() {
		return new HashMap<String, Object>();
	}
	
	private Map<String, Object> read_solution(int flags) throws IOException {
		HashMap<String, Object> result = new HashMap<String, Object>();
		while (true) {			
			String varname = (String) readValue(PrologInterface.UNQUOTE_ATOMS);
			if (varname == null) {
				return handleSpecialResults(result);
			} else {
				Object value = readVariableValue(flags, varname);
				result.put(varname, value);
			}
		}
	}

	private Object readVariableValue(int flags, String varname)
			throws IOException {
		Object value = readValue(flags);
		if (value == null) {
			throw new PrologException(
					"could not read value for variable " + varname);
		}
		return value;
	}

	private HashMap<String, Object> handleSpecialResults(HashMap<String, Object> result)
			throws IOException {
		String line = client.readln();
		if (line == null) {
			throw new IOException("There was no solution to read.");
		}
		if (line.startsWith(SocketCommunicationConstants.ERROR)) {
			throwPrologErrorToJavaVM(line);
		}
		if (SocketCommunicationConstants.END_OF_SOLUTION.equals(line)) {
			return result;
		}
		return null;
	}

	private void throwPrologErrorToJavaVM(String line) {
		int errorLength = SocketCommunicationConstants.ERROR.length();
		String errorSubstring = line.substring(errorLength);
		throw new PrologException(errorSubstring);
	}

	private Object readValue(int flags) throws IOException {
		return client.readValue(flags);
	}
	
	private void finishReading() throws IOException, PrologInterfaceException {
		while (true) {
			String line = client.readln();
			if (line == null) {
				throw pif.error(new IllegalStateException(
						"There is nothing to read"));
			}
			if (SocketCommunicationConstants.MORE.equals(line)) {
				client.writeln(SocketCommunicationConstants.NO);
			}
			if (SocketCommunicationConstants.OK.equals(line)) {
				return;
			}
			if (line.startsWith(SocketCommunicationConstants.ERROR)) {
				throwPrologErrorToJavaVM(line);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cs3.pl.prolog.PrologSession#isDisposed()
	 */
	@Override
	public boolean isDisposed() {
		return client == null;
	}

	private void configureProtocol(int flags) {
		/*
		 * the only thing that needs to be configured on the server side, is
		 * whether or not lists are processed.
		 */
		boolean processLists = (flags & PrologInterface.PROCESS_LISTS) > 0;
		setProtocolOption("interprete_lists", Boolean.toString(processLists));
	}

	public SocketClient getClient() {
		return client;
	}

	private void setProtocolOption(String id, String value) {
		if (queryActive) {
			throw new RuntimeException(
					"Cannot set protocol option while query is active.");
		}
		try {
			client.readUntil(SocketCommunicationConstants.GIVE_COMMAND);
			client.writeln(SocketCommunicationConstants.SET_OPTION);
			client.readUntil(SocketCommunicationConstants.GIVE_SYMBOL);
			client.writeln(id);
			client.readUntil(SocketCommunicationConstants.GIVE_TERM);
			client.writeln(value);
			client.readUntil(SocketCommunicationConstants.OK);
		} catch (IOException e) {
			throw new RuntimeException("IO Error while setting protocol option");
		} 
	}
}
