package org.cs3.pdt.internal.structureElements;

import java.util.HashMap;
import java.util.Map;

public class OutlineModuleElement implements PrologTreeElement{
	private String name;
	private String kind;
	private Map<String, OutlinePredicate> predicates= new HashMap<String,OutlinePredicate>();
	
	public OutlineModuleElement(String name, String kindOfEntity) {
		this.name = name;
	}
	
	public boolean hasPredicate(String key) {
		return predicates.containsKey(key);
	}
	
	public OutlinePredicate getPredicate(String key) {
		return predicates.get(key);
	}
	
	public void addChild(String key, OutlinePredicate predicate) {
		predicates.put(key, predicate);
	}
	
	public boolean hasChildren() {
		return !(predicates.isEmpty());
	}
	
	public String getKind() {
		return kind;
	}
	
	public void dispose() {
		predicates.clear();
	}

	@Override
	public Object[] getChildren() {
		return predicates.values().toArray();
	}

	@Override
	public String getLabel() {
		return name;
	}
}
