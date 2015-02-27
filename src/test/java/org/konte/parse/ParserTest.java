package org.konte.parse;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.konte.lang.Tokenizer;
import org.konte.model.ConditionalStructure;
import org.konte.model.Model;

public class ParserTest {
    
    public ParserTest() {
    }

    @Test
    public void testParseExpr() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s =
"cube {\n" +
"    SQUARE{x 1/3 y -1/3 z -1+2+3 col0 (col0+1)}\n" +
"}"
;
        Model m = new Parser().parse( 
            Tokenizer.retrieveTokenStrings(new StringBuilder(s))
        );
        m.initForGenerate();
    }

    @Test(expected = ParseException.class)
    public void testParseInnerExprArg() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s = "throws { SQUARE{col0 col0+1} }";
        new Parser().parse(Tokenizer.retrieveTokenStrings(new StringBuilder(s)))
                .initForGenerate();
    }
    
    @Test
    public void testParseIfs() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s =
"cube {\n" +
"  if (x > 1) {\n" +
"  if (x > 2) {\n" +
"  if (x > 3) {\n" +
"    if (x > 4) {\n" +
"      SQUARE{}\n" +
"    }\n" +
"    SQUARE{{foo=1}}\n" +
"    if (x > 5) {\n" +
"      SQUARE{}\n" +
"    }\n" +
"  }\n" +
"  }\n" +
"  }\n" +
"}"
;
        Model m = new Parser().parse( 
            Tokenizer.retrieveTokenStrings(new StringBuilder(s))
        );
        m.initForGenerate();
        System.out.println(m.rules.toString());
        ConditionalStructure cond0 = (ConditionalStructure)
                (m.rules.get("cube").getRules().get(0).getRule().transforms.get(0));
        assertEquals(true, cond0.conditionalStructure);
        assertEquals(1, cond0.onCondition.size());
        ConditionalStructure cond1 = (ConditionalStructure)
                cond0.onCondition.get(0);
        assertEquals(1, cond1.onCondition.size());
        ConditionalStructure cond2 = (ConditionalStructure)
                cond1.onCondition.get(0);
        assertEquals(3, cond2.onCondition.size());
        
        assertEquals(1, ((ConditionalStructure)cond2.onCondition.get(0)).onCondition.size());
        assertEquals(1, ((ConditionalStructure)cond2.onCondition.get(2)).onCondition.size());

    }

    @Test
    public void testParseDef() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s =
"DEF foo 1\n" +
"DEF bar (1+2)\n" +
"DEF baz 1+2\n" +
"cube { SQUARE{} }"
;
        Model m = new Parser().parse( 
            Tokenizer.retrieveTokenStrings(new StringBuilder(s))
        );
        m.initForGenerate();
    }
    

    @Test
    public void testParseBooleanExpr() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s =
"eee { if (1<2 && 4<3) { if (x>0 || y>0 && x<1) { SQUARE{} } } }"
;
        Model m = new Parser().parse( 
            Tokenizer.retrieveTokenStrings(new StringBuilder(s))
        );
        m.initForGenerate();
        ConditionalStructure cond0 = (ConditionalStructure)
                m.rules.get("eee").getRules().get(0).getRule().transforms.get(0);
        System.out.println(cond0);
    }

}
