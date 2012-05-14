package org.cs3.pdt.internal.structureElements;


public class PredicateOccuranceElement implements PrologTreeElement{
	private String label;
	private int line;
	private String type;
	private PrologTreeElement parent;
	private String file;
	
	public PredicateOccuranceElement(String label, String file, int line, String type, PrologTreeElement parent) {
		this.label = label;
		this.line = line;
		this.type = type;
		this.file = file;
		this.parent = parent;
	}
	
	public int getLine() {
		return line;
	}
	
	public String getType() {
		return type;
	}
	
	public PrologTreeElement getParent() {
		return parent;
	}
	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return new Object[0];
	}

	@Override
	public String getLabel() {
		return label;
	}
	
	public String getFile() {
		return file;
	}

}
