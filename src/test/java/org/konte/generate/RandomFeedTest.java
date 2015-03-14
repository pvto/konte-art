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

    @Test
    public void testConvert()
    {
        RandomFeed f = new RandomFeed();
        assertEquals(f.toConvertKey(1), f.toConvertKey(f.convertKey("A")));
        assertEquals(f.toConvertKey(25), f.toConvertKey(f.convertKey("Y")));
        assertEquals(f.toConvertKey(33), f.toConvertKey(f.convertKey("AA")));
        assertEquals(f.toConvertKey(34), f.toConvertKey(f.convertKey("AB")));
        assertEquals(f.toConvertKey(58), f.toConvertKey(f.convertKey("AZ")));
        assertEquals(f.toConvertKey(65), f.toConvertKey(f.convertKey("BA")));
        assertEquals(f.toConvertKey(67), f.toConvertKey(f.convertKey("BC")));
        assertEquals(f.toConvertKey(90), f.toConvertKey(f.convertKey("BZ")));
        assertEquals(f.toConvertKey(858), f.toConvertKey(f.convertKey("ZZ")));
        assertEquals(f.toConvertKey(1057), f.toConvertKey(f.convertKey("AAA")));
        assertEquals(f.toConvertKey(1059), f.toConvertKey(f.convertKey("AAC")));
        assertEquals(f.toConvertKey(1082), f.toConvertKey(f.convertKey("AAZ")));
        assertEquals(f.toConvertKey(1089), f.toConvertKey(f.convertKey("ABA")));
        assertEquals(f.toConvertKey(33825), f.toConvertKey(f.convertKey("AAAA")));
        assertEquals(f.toConvertKey(1082401), f.toConvertKey(f.convertKey("AAAAA")));
        assertEquals(f.toConvertKey(28142426), f.toConvertKey(f.convertKey("ZZZZZ")));
        assertEquals(f.toConvertKey(34636833), f.toConvertKey(f.convertKey("AAAAAA")));
        assertEquals(f.toConvertKey(900557658), f.toConvertKey(f.convertKey("ZZZZZZ")));
    }

}
