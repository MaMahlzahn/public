/* Generated By:JJTree: Do not edit this line. ASTNil.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.cs3.pl.cterm.internal.parser;

public
class ASTNil extends ASTNode {
  public ASTNil(int id) {
    super(id);
  }

  public ASTNil(CanonicalTermParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CanonicalTermParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5938375baefaab74632e535c2535d06a (do not edit this line) */
