
package org.konte.lang;

import org.konte.parse.ParseException;
import java.util.ArrayList;


/**<p>Splits a konte script to tokens.
 * {@link org.konte.model.Parser} will create a model from the tokens.
 *<p>Tokenizer stores token strings and line numbers for later reference.
 * {@link Tokenizer.TokenizerString}
 * @author pvto
 */
public class Tokenizer {

    private int offset;
    private int tokenStart;
    private int lineNr;
    private String token;
    private String b;

    private Tokenizer(String rawtext)
    {
        b = rawtext;
        lineNr = 1;
    }

    
    private void parseMultilineComment()
    {
        int orig = offset;
        int dropRest = 0;
        for (;;) {
            if (offset > orig + 2 && 
                    b.charAt(offset - 1) == '*' && b.charAt(offset) == '/')
            {
                offset++;
                dropRest = 2;
                break;
            }
            else
            {
                checkEndline();
            }
            if (b.length() <= ++offset) {   // missing */ ... normally error but allowed in editor line context
                break;
            }
        }
        token = b.substring(orig, offset - dropRest); // pick */ in the next round
        offset -= dropRest;
        return;
    }

    private void parseCommentLine()
    {
        token = null;
        if (offset >= b.length())
            return;
        for(;;) {
            if (checkEndline() || ++offset >= b.length())
            {
                token = b.substring(tokenStart, offset);
                return;
            }
        }
    }
    private void parseStringLiteral() throws ParseException
    {
        int orig = offset - 1;
        for(;;) {
            if (offset >= b.length())
            {
                token = b.substring(orig, b.length());
                return;
            }
            char c = b.charAt(offset++);
            if (c == '"')
            {
                token = b.substring(orig, offset);
                return;
            }
            else if (c == '\n') 
            {
                lineNr++;
            }
        }
    }

    private boolean isCommentChars()
    {
        char c = b.charAt(offset);
        if (c == '/')
        {
            if (b.length() > offset + 1)
            {
                if (b.charAt(offset + 1) == '/')
                {
                    tokenStart = offset;
                    parseCommentLine();
                    return true;
                }
                else if (b.charAt(offset + 1) == '*') 
                {
                    tokenStart = offset;
                    parseMultilineComment();
                    return true;
                }
            }
        }
        return false;
    }
    
    private void readNext() throws ParseException 
    {

        char curchar = b.charAt(offset);
        while(curchar == ' ' || curchar == '\t' || curchar == '\r' || checkEndline() || curchar < ' ' || curchar == 160)
        {
            if (++offset >= b.length()) {
                token = null;
                return;
            }
            curchar = b.charAt(offset);
        }

        tokenStart = offset;
        
        if (curchar == '"')
        {
            offset++;
            parseStringLiteral();
            return;
        }
        else if (isCommentChars()) 
        {
            return;
        }


        int orig = offset;
        for (;;) {
            curchar = b.charAt(offset);

            if (checkEndline())
            {
                int boundary = offset;
                if (b.charAt(offset - 1) == '\r')
                    boundary = offset - 1;
                token = b.substring(orig, boundary);
                return;
            }

            if (offset > orig)
            {
                if (curchar == '\t' || curchar == ' ' || curchar < ' ' || curchar == 160)
                {
                    token = b.substring(orig, offset);
                    return;
                }
                else if (Language.isControlChar(curchar)) 
                {
                    String oper = b.substring(orig, offset + 1);
                    if (Language.tokenByName(oper) != null) {
                       offset++;
                    }
                    token = b.substring(orig, offset);
                    return;
                } else if (Language.isControlChar(b.charAt(offset - 1)) &&
                        Language.isNameChar(curchar))
                {
                    token = b.substring(orig, offset);
                    return;
                }
            }

            if (b.length() <= ++offset) {
                break;
            }
        }
        token = b.substring(orig, offset);
        return;
    }

    private boolean checkEndline()
    {
        char curchar = b.charAt(offset);
        if (curchar == '\n')
        {
            lineNr++;
            return true;
        }
        return false;
    }

    
    public static ArrayList<TokenizerString> retrieveTokenStrings(String b) throws ParseException
    {
        Tokenizer position = new Tokenizer(b);
        ArrayList<TokenizerString> tokenStrings = new ArrayList<TokenizerString>();

        while (position.offset < b.length())
        {
            int tokenLineNr = position.lineNr;
            position.readNext();
            int caret = position.tokenStart;
            String retrievedToken = position.token;
            if (retrievedToken == null /*|| retrievedToken.length() == 0*/) {
                // skip
            }
            else
            {
                tokenStrings.add(new TokenizerString(retrievedToken,tokenLineNr,caret));
            }
        }
        return tokenStrings;
    }
    
    
    
    
    public static class TokenizerString {
        private String string;
        private int lineNr;
        private int caretPos;

        public TokenizerString(String string, int lineNr, int caretPos) {
            this.string = string;
            this.lineNr = lineNr;
            this.caretPos = caretPos;
        }

        public int getLineNr() {
            return lineNr;
        }

        public String getString() {
            return string;
        }

        public void setString(String s) {
            this.string = s;
        }

        public int getCaretPos() {
            return caretPos;
        }

        public String toString() { return string; }
    }
}

