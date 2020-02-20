package org.konte.lang.func;

import org.junit.Test;
import static org.junit.Assert.*;
import org.konte.generate.RandomFeed;
import org.konte.lang.Tokenizer;
import org.konte.model.Model;
import org.konte.parse.Parser;

public class ProbTest {

    private Model emptyModel() throws org.konte.parse.ParseException, IllegalAccessException {
        Model m = new Parser().parse(Tokenizer.retrieveTokenStrings("foo { SQUARE{} }"));
        m.initForGenerate();
        return m;
    }

    public ProbTest() {
    }

    @Test
    public void testNcr() {
        assertTrue(Prob.ncr(30, 10) > 0);
        assertTrue(Prob.ncr(40, 20) > 0);
        assertTrue(Prob.ncr(50, 24) > 0);
        assertTrue(Prob.ncr(60, 31) > 0);
        assertTrue(Prob.ncr(70, 34) > 0);
    }

    @Test
    public void testBincuml() {
        assertTrue(Prob.binmCuml(10, 0.3, 10) == 1.0);
        assertTrue(Prob.binmCuml(10, 0.3, 9) < 1.0);
    }

    @Test
    public void testNegbinm() {
        assertEquals(Prob.negbinm(4, 0.5, 2), 3.0 * 0.5*0.5*0.5*0.5, 1e-6);
    }

    @Test
    public void testGaussian() {
        RandomFeed rfeed = new RandomFeed();
        for(int i = 0; i < 20; i++) {
            double u = Prob.gaussian(rfeed);
            System.out.print(u + "  ");
        }
    }
}
