package org.konte.parse;

import org.junit.Test;
import org.konte.lang.Tokenizer;
import org.konte.model.Model;

public class ParserTest {
    
    public ParserTest() {
    }

    @Test
    public void testParseExpr() throws ParseException, IllegalArgumentException, IllegalAccessException {
        String s =
"cube {\n" +
"    featurez{x 1/3 y -1/3 z -1+2+3}\n" +
"}"
;
        Model m = new Parser().parse( 
            Tokenizer.retrieveTokenStrings(new StringBuilder(s))
        );
        m.initForGenerate();
    }
    
}
