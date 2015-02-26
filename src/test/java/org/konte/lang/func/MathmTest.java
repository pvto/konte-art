package org.konte.lang.func;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathmTest {
    
    public MathmTest() {
    }

    @Test
    public void testSaw() {
        Mathm.ESawWave saw = new Mathm.ESawWave("saw");
        
        assertEquals(0f, saw.value(-1f), 1e-6f);
        assertEquals(1f, saw.value(-0.5f), 1e-6f);
        assertEquals(0f, saw.value(0f), 1e-6f);
        assertEquals(1f, saw.value(0.5f), 1e-6f);
        assertEquals(0f, saw.value(1f), 1e-6f);
        assertEquals(1f, saw.value(1.5f), 1e-6f);
    }
    
}
