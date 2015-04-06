package org.konte.lang.func;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProbTest {
    
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
}
