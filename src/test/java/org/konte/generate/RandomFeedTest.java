package org.konte.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class RandomFeedTest {
    
    @Test
    public void testRandomFeed()
    {
        RandomFeed A = new RandomFeed("AAB");
        RandomFeed A2 = new RandomFeed("AAB");
        
        double PREC = 1e-7;
        assertEquals(A.get(), A2.get(), PREC);
        assertEquals(A.get(), A2.get(), PREC);

        RandomFeed AB = new RandomFeed("AAB");
        RandomFeed B = new RandomFeed("BAB");
        assertFalse(Math.abs(AB.get() - B.get()) < PREC);
        assertFalse(Math.abs(AB.get() - B.get()) < PREC);
    }
    
}
