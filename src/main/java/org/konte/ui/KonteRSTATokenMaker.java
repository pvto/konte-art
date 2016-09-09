package org.konte.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.konte.lang.Language;
import org.konte.lang.Tokenizer;
import org.konte.lang.Tokenizer.TokenizerString;
import org.konte.lang.Tokens;
import org.konte.model.Untransformable;
import org.konte.parse.ParseException;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class KonteRSTATokenMaker extends AbstractTokenMaker {

    public static class MyLinkedNode {
        public char[] key;
        public int value;
        public MyLinkedNode next;
        
        public MyLinkedNode(char[] key, int value)
        {
            this.key = key;
            this.value = value;
        }
    }
    public static boolean deepEquals(char[] a, char[] segment, int bOffset, int bStripLength)
    {
        if (a.length != bStripLength) return false;
        for(int i = 0; i < a.length; i++)
            if (a[i] != segment[i+bOffset]) return false;
        return true;
    }
    
    public static class MyNameTypeMap
    {
        private MyLinkedNode[] hashTable = new MyLinkedNode[1<<12];
        public void put(String name, int type)
        {
            char[] chars = name.toCharArray();
            int hash = getHash(chars, 0, chars.length);
            if (hashTable[hash] != null)
            {
                MyLinkedNode prev = hashTable[hash];
                
                for(;;)
                {
                    if (deepEquals(prev.key, chars, 0, chars.length))
                    {
                        prev.value = type;
                        return;
                    }
                    if (prev.next == null)
                    {
                        break;
                    }
                    prev = prev.next;
                } 
                prev.next = new MyLinkedNode(chars, type);
                return;
            }
            hashTable[hash] = new MyLinkedNode(chars, type);
        }
        
        public int get(char[] seg, int offset, int end)
        {
            int hash = getHash(seg, offset, end - offset + 1);
            MyLinkedNode node = hashTable[hash];
            while(node != null)
            {
                if (deepEquals(node.key, seg, offset, end - offset + 1))
                {
                    return node.value;
                }
                node = node.next;
            }
            return -1;
        }
        
        public int getHash(char[] chars, int offset, int length)
        {
            return chars[offset] * 17 + chars[offset + length - 1] % hashTable.length;
        }
    }
    
    private MyNameTypeMap myMap;
    
    @Override
    public TokenMap getWordsToHighlight()
    {
        if (myMap == null) myMap = new MyNameTypeMap();
        
        TokenMap tokenMap = new TokenMap();

        for(Tokens.Token ktok : Language.tokens)
        {
            int type = -1;
            if (Tokens.Function.class.isAssignableFrom(ktok.getClass()))
            {
                type = Token.FUNCTION;
            }
            else if (Tokens.InnerToken.class.isAssignableFrom(ktok.getClass()))
            {
                type = Token.RESERVED_WORD;
            }
            else if (ktok == Language.comment_start || ktok == Language.comment_end)
            {
                type = Token.COMMENT_MULTILINE;
            }
            else if (ktok == Language.comment)
            {
                type = Token.COMMENT_EOL;
            }
            else if (ktok == Language.hyphen) 
            {
                type = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            else if (Tokens.Context.class.isAssignableFrom(ktok.getClass()))
            {
                type = Token.SEPARATOR;
            }
            else if (Untransformable.class.isAssignableFrom(ktok.getClass()))
            {
                type = Token.RESERVED_WORD_2;
            }
            else if (Tokens.Operator.class.isAssignableFrom(ktok.getClass())
                    || Tokens.Comparator.class.isAssignableFrom(ktok.getClass()))
            {
                type = Token.OPERATOR;
            }
            else {
                type = Token.RESERVED_WORD;
            }
            tokenMap.put(ktok.name, type);
            myMap.put(ktok.name, type);
            for(String alias : ktok.aliases)
            {
                tokenMap.put(alias, type);
                myMap.put(alias, type);
            }
        }
        return tokenMap;
    }

    @Override
    public void addToken(Segment segment, int start, int end, int tokenType, int docStartOffset)
    {
        System.out.println("addToken " + start + "(" + docStartOffset + ")" + "-" + end);
        // This assumes all keywords, etc. were parsed as "identifiers."
        if (tokenType==Token.IDENTIFIER)
        {
            int value = myMap.get(segment.array, start, end);
            System.out.println(value);
            if (value != -1)
            {
                tokenType = value;
            }
        }
        super.addToken(segment, start, end, tokenType, docStartOffset);
    }

    
    @Override
    public Token getTokenList(Segment text, int startTokenType, int docStartOffset)
    {
        resetTokenList();
        int offset = text.offset;
        int newStartOffset = docStartOffset - offset;
        int currentTokenType = startTokenType;
        int nextStart = -1;
        int nextEnd = -1;
        int prevEnd = -1;
        char[] chars = text.array;
        char[] seg = Arrays.copyOfRange(chars, text.offset, offset + text.count);
        String sseg = new String(seg);
        
        
        try
        {
            ArrayList<TokenizerString> tokens = Tokenizer.retrieveTokenStrings(sseg);
            TokenizerString prev = null;

            out: for(TokenizerString next : tokens)
            {
                nextStart = offset + next.getCaretPos();
                nextEnd = nextStart + next.getString().length() - 1;
                System.out.println(String.format("gtl o=%d nso=%d ctt=%d ns=%d ne=%d pe=%d %s",
                        offset, newStartOffset, currentTokenType, nextStart, nextEnd, prevEnd, sseg));
                Tokens.Token konte = Language.tokenByName(next.getString());
                switch(currentTokenType)
                {
                    case Token.COMMENT_MULTILINE:
                        if (konte != Language.comment_end)
                            continue;
                        addToken(text, prevEnd + 1, nextEnd, currentTokenType, newStartOffset + prevEnd + 1);
                        currentTokenType = Token.NULL;
                        break;
                    case Token.COMMENT_EOL:
                        addToken(text, prevEnd + 1, offset + chars.length - 1, currentTokenType, newStartOffset + prevEnd + 1);
                        currentTokenType = Token.NULL;
                        break out;
                    case Token.LITERAL_STRING_DOUBLE_QUOTE:
                        if (konte != Language.hyphen)
                            continue;
                        addToken(text, prevEnd, nextEnd, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + prevEnd);
                        currentTokenType = Token.NULL;
                        break;
                    default:
                        if (prevEnd + 1 < nextStart)
                        {
                            addToken(text, prevEnd + 1, nextStart - 1, Token.WHITESPACE, newStartOffset + prevEnd + 1);
                        }

                        int type = Token.IDENTIFIER;
                        if (konte == Language.comment_start)
                        {
                            type = Token.COMMENT_MULTILINE;
                        }
                        else if (konte == Language.comment)
                        {
                            type = Token.COMMENT_EOL;
                        }
                        else if (konte == Language.hyphen)
                        {
                            type = Token.LITERAL_STRING_DOUBLE_QUOTE;
                        }
                        else
                        {
                            addToken(text, nextStart, nextEnd, type, newStartOffset + nextStart);
                        }
                        prev = next;
                        prevEnd = nextEnd;
                        break;
                }

            }
            switch (currentTokenType) {

               // Remember what token type to begin the next line with.
               case Token.LITERAL_STRING_DOUBLE_QUOTE:
               case Token.COMMENT_MULTILINE:
                   int add = currentTokenType == Token.LITERAL_STRING_DOUBLE_QUOTE ? 0 : -1;
                  addToken(text, Math.max(0, prevEnd + add), nextEnd, currentTokenType, newStartOffset + prevEnd + add);
                  break;

               default:
                  addNullToken();

            }
        }
        catch(ParseException pe)
        {
            throw new RuntimeException(pe);
        }
        return firstToken;
    }

    
    public static class TextEditorDemo extends JFrame {

       public TextEditorDemo() {

          JPanel cp = new JPanel(new BorderLayout());

          AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
          atmf.putMapping("text/c3dg", "org.konte.ui.KonteRSTATokenMaker");

          RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
          textArea.setSyntaxEditingStyle("text/c3dg");
          textArea.setCodeFoldingEnabled(true);
          RTextScrollPane sp = new RTextScrollPane(textArea);
          cp.add(sp);

          setContentPane(cp);
          setTitle("Text Editor Demo");
          setDefaultCloseOperation(EXIT_ON_CLOSE);
          pack();
          setLocationRelativeTo(null);

       }

    }

    public static void main(String[] args) {
       SwingUtilities.invokeLater(new Runnable() {
          public void run() {
             new TextEditorDemo().setVisible(true);
          }
       });
    }

}
