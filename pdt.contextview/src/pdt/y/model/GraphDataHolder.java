/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

package pdt.y.model;

import y.base.DataMap;
import y.base.Edge;
import y.base.Node;
import y.util.Maps;

public class GraphDataHolder {

	private static final String MODULE = "module";
	private static final String FILE = "file";
	private static final String FILE_NODE = "file_node";
	private static final String PREDICATE = "predicate";
	private static final String CALL = "call";
	private static final String LOADING = "loading";
	private static final String FILE_ENTRY_POINT = "entry_point";

	// Addition data:
	private DataMap nodeMap = Maps.createHashedDataMap();
	private DataMap moduleMap = Maps.createHashedDataMap();
	private DataMap fileNameMap = Maps.createHashedDataMap();
	private DataMap kindMap = Maps.createHashedDataMap();
	private DataMap functorMap = Maps.createHashedDataMap();
	private DataMap arityMap = Maps.createHashedDataMap();
	private DataMap callFrequencyMap = Maps.createHashedDataMap();
	private DataMap dynamicMap = Maps.createHashedDataMap();
	private DataMap transparentMap = Maps.createHashedDataMap();
	private DataMap metaPredMap = Maps.createHashedDataMap();
	private DataMap multifileMap = Maps.createHashedDataMap();
	private DataMap exportedMap = Maps.createHashedDataMap();
	private DataMap unusedLocal = Maps.createHashedDataMap();
	private DataMap fileTypeMap = Maps.createHashedDataMap();


	// Getter and Setter
	public DataMap getNodeMap() {
		return nodeMap;
	}

	public DataMap getModuleMap() {
		return moduleMap;
	}

	public DataMap getFileNameMap() {
		return fileNameMap;
	}

	public DataMap getKindMap() {
		return kindMap;
	}

	public DataMap getFunctorMap() {
		return functorMap;
	}

	public DataMap getArityMap() {
		return arityMap;
	}

	public DataMap getCallFrequencyMap() {
		return callFrequencyMap;
	}

	public DataMap getDynamicMap() {
		return dynamicMap;
	}

	public DataMap getTransparentMap() {
		return transparentMap;
	}

	public DataMap getMetaPredMap() {
		return metaPredMap;
	}

	public DataMap getMultifileMap() {
		return multifileMap;
	}


	public DataMap getExportedMap() {
		return exportedMap;
	}

	public DataMap getUnusedLocalMap() {
		return unusedLocal;
	}
	
	public DataMap getFileTypeMap() {
		return fileTypeMap;
	}

	public boolean isPredicate(Node node) {
		DataMap kindMap = getKindMap();
		String kind = kindMap.get(node).toString();
		return kind.equals(PREDICATE);
	}

	public boolean isModule(Node node) {
		DataMap kindMap = getKindMap();
		String kind = kindMap.get(node).toString();
		return kind.equals(MODULE);
	}

	public boolean isFile(Node node) {
		DataMap kindMap = getKindMap();
		String kind = kindMap.get(node).toString();
		return kind.equals(FILE);
	}
	
	public boolean isFileNode(Node node) {
		DataMap kindMap = getKindMap();
		String kind = kindMap.get(node).toString();
		return kind.equals(FILE_NODE);
	}

	public boolean isCallEdge(Edge edge) {
		DataMap kindMap = getKindMap();
		String kind = kindMap.get(edge).toString();
		return kind.equals(CALL);
	}

	public boolean isLoadingEdge(Edge edge) {
		DataMap kindMap = getKindMap();
		String kind = kindMap.get(edge).toString();
		return kind.equals(LOADING);
	}

	public boolean isDynamicNode(Node node) {
		Object returnNode = dynamicMap.get(node);
		if(returnNode == null)
			return false;
		return (Boolean)returnNode;
	}

	public boolean isTransparentNode(Node node) {
		Object returnNode = transparentMap.get(node);
		if(returnNode == null)
			return false;
		return (Boolean)returnNode;
	}

	public boolean isMetaPred(Node node) {
		Object returnNode = metaPredMap.get(node);
		if(returnNode == null)
			return false;
		return (Boolean)returnNode;
	}

	public boolean isMultifile(Node node) {
		Object returnNode = multifileMap.get(node);
		if(returnNode == null)
			return false;
		return (Boolean)returnNode;
	}

	public boolean isExported(Node node) {
		Object returnNode = exportedMap.get(node);
		if(returnNode == null)
			return false;
		return (Boolean)returnNode;
	}

	public boolean isUnusedLocal(Node node) {
		Object returnNode = unusedLocal.get(node);
		if(returnNode == null)
			return false;
		return (Boolean)returnNode;
	}

	public boolean isEntryPointFile(Node node) {
		DataMap typeMap = getFileTypeMap();
		String type = typeMap.get(node).toString();
		return type.equals(FILE_ENTRY_POINT);
	}

	public String getLabelTextForNode(Node node) {
		String labelText;
		if (isModule(node)) {
			labelText = getModuleName(node);
		} else if (isFile(node))  {
			labelText = getFileName(node);
		} else if (isFileNode(node)) {
			labelText = getFileNodeText(node);
		} else if (isPredicate(node))  {
			labelText = getPredicateText(node);
		} else {
			labelText = getNodeText(node);
		}
		return labelText;
	}

	//	public int getIdForNode(Node node) {
	//		return (Integer) nodeMap.get(node);
	//	}

	private String getModuleName(Node node) {
		return moduleMap.get(node).toString();
	}
	
	private String getFileNodeText(Node node) {
		return functorMap.get(node).toString();
	}

	private String getPredicateText(Node node) {
		return functorMap.get(node) + " / " + arityMap.get(node);
	}

	private String getFileName(Node node) {
		return fileNameMap.get(node).toString();
	}
	public String getNodeText(Node node) {
		return nodeMap.get(node).toString();
	}

	public int getFrequency(Edge edge) {
		return callFrequencyMap.getInt(edge);
	}

}


