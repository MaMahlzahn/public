/* Generated By:JJTree: Do not edit this line. ASTDividedAtom.java */

package org.cs3.pl.parser;

public class ASTDividedAtom extends SimpleNode implements ASTTerm {
  public ASTDividedAtom(int id) {
    super(id);
  }

  public ASTDividedAtom(PrologParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PrologParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
