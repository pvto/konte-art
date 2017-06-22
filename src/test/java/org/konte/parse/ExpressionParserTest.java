package org.konte.parse;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.konte.expression.Expression;
import org.konte.lang.Tokenizer;
import org.konte.misc.Matrix4;
import org.konte.model.DrawingContext;
import org.konte.model.Model;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class ExpressionParserTest {

    private Model evModel(String s) throws ParseException, IllegalArgumentException, IllegalAccessException
    {
        Model m = new Parser().parse(Tokenizer.retrieveTokenStrings(s));
        m.initForGenerate();
        m.context = new DrawingContext();
        m.context.matrix = Matrix4.IDENTITY;
        return m;
    }
    
    @Test
    public void parseDefMax() throws ParseException, IllegalArgumentException, IllegalAccessException {
        String s = 
"foo { CIRCLE { {SAT=1*(max(1,(sin(cu*3))**.7))\n" +
"     + 2 }} }\n";
        Model m = evModel(s);
        Expression def = m.rules.get("foo").getRules().get(0).getRule().transforms.get(0).defs.get(0).definition;
        System.out.println(def);
        assertEquals(3f, def.evaluate(), 1e-5f);
    }
}
