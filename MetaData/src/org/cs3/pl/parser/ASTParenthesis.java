/* Generated By:JJTree: Do not edit this line. ASTParenthesis.java */

package org.cs3.pl.parser;

public class ASTParenthesis extends SimpleNode implements ASTTerm {
  public ASTParenthesis(int id) {
    super(id);
  }

  public ASTParenthesis(PrologParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PrologParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
