/* Generated By:JJTree: Do not edit this line. PrologParserVisitor.java */

package org.cs3.pl.parser;

public interface PrologParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTCompilationUnit node, Object data);
  public Object visit(ASTFunctor node, Object data);
  public Object visit(ASTFunctorVariableModule node, Object data);
  public Object visit(ASTVariable node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTNamedCall node, Object data);
  public Object visit(ASTCall node, Object data);
  public Object visit(ASTPredicateSignature node, Object data);
  public Object visit(ASTClause node, Object data);
  public Object visit(ASTHead node, Object data);
  public Object visit(ASTPredicateArgs node, Object data);
  public Object visit(ASTCompound node, Object data);
  public Object visit(ASTCut node, Object data);
  public Object visit(ASTSequence node, Object data);
  public Object visit(ASTList node, Object data);
  public Object visit(ASTBraces node, Object data);
  public Object visit(ASTParenthesis node, Object data);
  public Object visit(ASTRestTokens node, Object data);
  public Object visit(ASTIntAtom node, Object data);
  public Object visit(ASTFloatAtom node, Object data);
  public Object visit(ASTStringAtom node, Object data);
  public Object visit(ASTCharAtom node, Object data);
  public Object visit(ASTBody node, Object data);
  public Object visit(ASTSeparator node, Object data);
  public Object visit(ASTBinaryOp node, Object data);
}
