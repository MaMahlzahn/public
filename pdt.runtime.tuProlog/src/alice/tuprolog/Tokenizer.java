/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;
import	java.io.*;

/**
 *  This class defines the tokenizer for prolog terms
 *
 *
 *
 */
class Tokenizer implements Serializable {

    public static final int    TYPEMASK    = 0x00FF;
    public static final int    ATTRMASK    = 0xFF00;
    public static final int    LPAR        = 0x0001;
    public static final int    RPAR        = 0x0002;
    public static final int    LBRA        = 0x0003;
    public static final int    RBRA        = 0x0004;
    public static final int    BAR         = 0x0005;
    public static final int    INTEGER     = 0x0006;
    public static final int    FLOAT       = 0x0007;
    public static final int    ATOM        = 0x0008;
    public static final int    VARIABLE    = 0x0009;
    public static final int    SQ_SEQUENCE = 0x000A;
    public static final int    DQ_SEQUENCE = 0x000B;
    public static final int    COMMA       = 0x000C;
    public static final int    END         = 0x000D;
    public static final int    LBRA2       = 0x000E;
    public static final int    RBRA2       = 0x000F;
    public static final int    FUNCTOR     = 0x0100;
    public static final int    OPERATOR    = 0x0200;
    public static final int    EOF         = 0x1000;
    public static final int    ERROR       = 0x2000;
    static public final String SYMCHAR     = "\\�$&?^@#.,:;=<>+-*/";

    /** string input stream source of text to be parsed */
    private alice.util.StringInputStream   inputStream;

    /** where to store unread characters */
    private alice.util.LinkedList     charList;

    /** where to store unread tokens */
    private alice.util.LinkedList     tokenList;

    /** current token type */
    private int         type;

    /** current token text */
    private String      seq;

    /** last token type */
    int lastTokenType;

    /**
     * creating a tokenizer for the source stream
     *
     */
    public Tokenizer(alice.util.StringInputStream is) {
        inputStream    = is;
        charList = new alice.util.LinkedList();
        // token list to be consumed
        tokenList = new alice.util.LinkedList();
    }

    /**
     * reads next available token
     */
    public Token readToken() {
        if(tokenList.isEmptyList()) {
            nextToken();
            lastTokenType=type;
            return(new Token(seq,type));
        } else {
            Token token = (Token)tokenList.head;
            tokenList = tokenList.tail;
            lastTokenType=token.getType();
            return(token);
        }
    }

    /**
     * puts back token to be read again
     */
    public void unreadToken(Token token) {
        tokenList = new alice.util.LinkedList(token,tokenList);
    }

    /**
     * gets current character index on the stream
     */
    public int getCurrentPos(){
        return inputStream.getCurrentPos();
    }

    /**
     * gets current line index on the stream
     */
    public int getCurrentLine(){
        return inputStream.getCurrentLine();
    }

    /**
     * resets parsing process.
     * <p>
     * it's ready to parse
     * from the beginning of the source
     */
    public void reset() throws Exception {
        charList = new alice.util.LinkedList();
        tokenList = new alice.util.LinkedList();
        inputStream.reset();
    }

    /** is the string a prolog atom? */
    public static boolean isAtom(String s) {
        if(s.length() == 0){
            return(false);
        }
        if(!isLLt((int)s.charAt(0))){
            return(false);
        }
        for(int c = 1;c < s.length();c++){
            if(!isLtr((int)s.charAt(c))){
                return(false);
            }
        }
        return(true);
    }

    //

    private static boolean isTer(int c) {
        return(c==-1 || c=='\n' || c=='\r');
    }

    private static boolean isWht(int c) {
        return(c==' ' || c=='\t' || isTer(c));
    }

    private static boolean isCmt(int c) {
        return(c=='%');
    }

    private static boolean isDgt(int c) {
        return(c>='0' && c<='9');
    }

    private static boolean isLLt(int c) {
        return(c=='!' || c>='a' && c<='z');
    }

    private static boolean isULt(int c) {
        return(c=='_' || c>='A' && c<='Z');
    }

    private static boolean isLtr(int c) {
        return(isLLt(c) || isULt(c) || isDgt(c));
    }

    private static boolean isExp(int c) {
        return(c=='e' || c=='E');
    }

    private static boolean isSym(int c) {
        return(SYMCHAR.indexOf(String.valueOf((char)c))!=-1);
    }

    private boolean isNum(int c) {
        if(isDgt(c)) {
            seq += String.valueOf((char)c);
            while(isDgt(c = readChar())){
                seq += String.valueOf((char)c);
            }
            unreadChar(c);
            return(true);
        }
        return(false);
    }

    private int readChar() {
        if(charList.isEmptyList()){
            try {
                int value=inputStream.read();
                return(value);

            } catch(Exception e) {
                return(-1);
            }
        } else {
            int ch = ((Integer)charList.head).intValue();
            charList = charList.tail;
            return(ch);
        }
    }

    private void unreadChar(int ch) {
        charList = new alice.util.LinkedList(new Integer(ch),charList);
    }

    private int skip() {
        int ch = 0;
        while(true) {
            while(isWht(ch = readChar())){
                if(ch == -1)
                    return(ch);
            }
            if(isCmt(ch)){
                while(!isTer(ch = readChar()));
            } else
                // Ricci 020824
                // check for comment block
                if (ch=='/'){
                    int next_ch=readChar();
                    if (next_ch=='*'){
                        // ok, it's a comment block
                        do {
                            // skip until * and /
                            next_ch = readChar();
                            if (next_ch=='*'){
                                next_ch = readChar();
                                if (next_ch == '/'){
                                    break;
                                }
                            }
                        } while (next_ch!=-1);
                    } else {
                        unreadChar(next_ch);
                        break;
                    }
                }  else {
                    // no comment/comment block
                    break;
                }
        }
        return(ch);
    }

    private int nextToken() {
        int ch = skip();
        type   = ERROR;
        seq    = "";

        if(ch == -1) {
            return(type = EOF);
        } else if(ch == '(') {
            seq = "(";
            return(type = LPAR);
        } else if(ch == ')') {
            seq = ")";
            return(type = RPAR);
        } else if(ch == '{') {
            seq = "{";
            return(type = LBRA2);
        } else if(ch == '}') {
            seq = "}";
            return(type = RBRA2);
        } else if(ch == '[') {
            seq = "[";
            return(type = LBRA);
        } else if(ch == ']') {
            seq = "]";
            return(type = RBRA);
        } else if(ch == '|') {
            seq = "|";
            return(type = BAR);
        } else if(ch == ','){
            seq=",";
            return(type = OPERATOR);
        } else if(ch == '-'){
            ch = readChar();

            if (isDgt(ch)){
                //
                //if (lastTokenType==FLOAT || lastTokenType==INTEGER || lastTokenType==VARIABLE || isSym(ch)){
                //
                if (lastTokenType==FLOAT || lastTokenType==INTEGER || lastTokenType==VARIABLE || isSym(ch) 
                    || lastTokenType==RPAR || lastTokenType==RBRA || lastTokenType==RBRA2 ){

                    unreadChar(ch);
                    ch = '-';
                } else {
                    seq = "-";
                }
            } else {
                unreadChar(ch);
                ch = '-';
            }
        } 
        //-----
	    else if(ch == '+'){
			ch = readChar();
			if (isDgt(ch)){
				if (lastTokenType==FLOAT || lastTokenType==INTEGER || lastTokenType==VARIABLE || isSym(ch)
					|| lastTokenType==RPAR || lastTokenType==RBRA || lastTokenType==RBRA2 ){

					unreadChar(ch);
					ch = '+';
				} else {
					//seq = "+";
				}
			} else {
				unreadChar(ch);
				ch = '+';
			}
		} 
        //------
        
        
        if(isNum(ch)) {
            if((ch = readChar()) != '.') {
                unreadChar(ch);
                return(type = INTEGER);
            }
            if(!isDgt(ch = readChar())) {
                unreadChar(ch);
                unreadChar('.');
                return(type = INTEGER);
            }
            seq += ".";
            isNum(ch);
            if(!isExp(ch = readChar())) {
                unreadChar(ch);
                return(type = FLOAT);
            }
            seq += "E";
            if((ch = readChar()) == '-') {
                seq += "-";
                ch = readChar();
            }
            if(!isNum(ch)){
                return(type = ERROR);
            }
            return(type = FLOAT);
        } else if(ch == '\'') {
            ch = readChar();
            while (ch!=-1 /*!isTer(ch)*/){
                 if (ch == -1){
                	 	unreadChar(-1);
                	 	break;
                 }
            		if (ch == '\''){
                    ch = readChar();
                    if (ch == '\''){
                        seq += "\'";
                    } else {
                        unreadChar(ch);
                        break;
                    }
                } else if (ch == '\\'){
                        // ricci 020824 - managing the line break
                    ch = readChar();
                    if (ch==-1){
                        unreadChar(-1);
                        break;
					} else if (ch=='t'){ 
						seq += '\t';
					} else if (ch=='r'){ 
						seq += '\r';
                    } else if (ch=='n'){
                    		seq += '\n';
                    } else if (!isTer(ch)){
                        	seq += '\\';
                        	unreadChar(ch);
                    } else {
                        ch = skip();
                        unreadChar(ch);
                    }
                } else {
                    seq += String.valueOf((char)ch);
                }
                ch = readChar();
            }
            // ricci - 020726 ---------------------------------
            ch = readChar();
            unreadChar(ch);
            type = SQ_SEQUENCE;
            if (ch == '('){
                type = type | FUNCTOR;
            }
            return(type);
        } else if(ch == '\"') {
            // ricci - 020726 ---------------------------------
            //while((ch = readChar()) != '\"' && !isTer(ch)){
            //    seq += String.valueOf((char)ch);
            //}
            ch = readChar();
            while (ch!=-1){
            	   if (ch==-1){
            		   unreadChar(-1);
            		   break;
            	   }
                if (ch == '\"'){
                    ch = readChar();
                    if (ch == '\"'){
                        seq += "\"";
                    } else {
                        unreadChar(ch);
                        break;
                    }
				} else if (ch == '\\'){
						// ricci 020824 - managing the line break
					ch = readChar();
					if (ch==-1){
						unreadChar(-1);
						break;
					} else if (ch=='t'){ 
						seq += '\t';
					} else if (ch=='r'){ 
						seq += '\r';
					} else if (ch=='n'){
						seq += '\n';
					} else if (!isTer(ch)){
						seq += '\\';
						unreadChar(ch);
					} else {
						ch = skip();
						unreadChar(ch);
					}
				}  else {
					seq += String.valueOf((char)ch);
				}
                ch = readChar();
            }
            // ricci - 020726 ---------------------------------
            ch = readChar();
            unreadChar(ch);
            type = SQ_SEQUENCE;
            if(ch == '('){
                type = type | FUNCTOR;
            }
            return(type);
        }
        else if(isLLt(ch)) {
            seq = String.valueOf((char)ch);
            while(isLtr(ch = readChar())){
                seq += String.valueOf((char)ch);
            }
            unreadChar(ch);
            type = ATOM;
            if(ch == '('){
                type = type | FUNCTOR;
            } else if(ch == ' '){
                type = type | OPERATOR;
            }
            return(type);
        }
        else if(isULt(ch)) {
            seq = String.valueOf((char)ch);
            while(isLtr(ch = readChar())){
                seq += String.valueOf((char)ch);
            }
            unreadChar(ch);
            return(type = VARIABLE);
        }
        else if(isSym(ch)) {
            seq = String.valueOf((char)ch);
            while(isSym(ch = readChar())){
                seq += String.valueOf((char)ch);
            }
            unreadChar(ch);
            if(seq.equals(".")) {
                while(isWht(ch = readChar())){
                    if(isTer(ch)){
                        return(type = END);
                    }
                }
                unreadChar(ch);
                if(isCmt(ch)){
                    return(type = END);
                }
            }
            return(type = OPERATOR);
        }
        seq = String.valueOf((char)ch);
        return(type);
    }
}