package org.konte.parse;

import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
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
            Tokenizer.retrieveTokenStrings(s)
        );
        m.initForGenerate();
    }

    @Test(expected = ParseException.class)
    public void testParseInnerExprArg() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s = "throws { SQUARE{col0 col0+1} }";
        new Parser().parse(Tokenizer.retrieveTokenStrings(s))
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
            Tokenizer.retrieveTokenStrings(s)
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
            Tokenizer.retrieveTokenStrings(s)
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
            Tokenizer.retrieveTokenStrings(s)
        );
        m.initForGenerate();
        ConditionalStructure cond0 = (ConditionalStructure)
                m.rules.get("eee").getRules().get(0).getRule().transforms.get(0);
        System.out.println(cond0);
    }

    @Test
    public void testParseFunctions() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s = "FNS { SQUARE{ L brndf(10,ih-1-(32*(vP*(v+PI/4)%ih))) "
                + "B imgred(img0,u*uP*32%iw, ih-1-(32*(vP*(v+PI/4)%ih)) ) "
                + "} }";
//(10.0000000 ((ih-1.0000000)-(32.0000000*((vP*(v+(3.1415927/4.0000000)))%ih))) )
        Model m = new Parser().parse( 
            Tokenizer.retrieveTokenStrings(s)
        );
        m.initForGenerate();
        System.out.println(m.rules.get("FNS").getRules().get(0).getRule().transforms.get(0).acqExps.get(1).exprs);
    }
    
    @Test(expected = ParseException.class)
    public void testParseUnknownFunction() throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        String s = "fov{llookat(1,0,0)} F{SPHERE{}}";
        Model m = new Parser().parse( 
            Tokenizer.retrieveTokenStrings(s)
        );
    }
    
    @Test
    public void testParseInclude() throws ParseException {
        String s = 
                "include \"http://pvto.github.io/assets/img/art/2016-09-24-15-30-sierp-textp-corr-frac-landscape-ADK.png\" myimg\n"
                + "include #monospace\n"
                + "foo{SQUARE{}}";
        new Parser().parse(Tokenizer.retrieveTokenStrings(s));
    }
}
