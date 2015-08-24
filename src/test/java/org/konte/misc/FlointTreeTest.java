package org.konte.misc;

import static org.junit.Assert.*;
import org.junit.Test;
import org.konte.misc.FlointTree.Do;
import org.konte.misc.FlointTree.FUPair;

public class FlointTreeTest {
    
    @Test
    public void testPut()
    {
        FlointTree t = new FlointTree();
        t.put(0f, 0);
        t.put(1f, 1);
        t.put(10f, 10);
        t.put(100f, 100);
        t.put(1000f, 1000);
        t.put(10000f, 10000);
        t.put(100000f, 100000);
        t.put(1000000f, 1000000);
        t.put(10000000f, 10000000);
        t.put(100000000f, 100000000);
//        t.put(1000000000f, 1000000000);
        final int[] sum = new int[]{0, Integer.MIN_VALUE, 0};
        final float[] prev = new float[]{Float.NEGATIVE_INFINITY};
        final boolean[] INC = new boolean[]{true};
        Do dod = new Do(){
            @Override
            public void now(FUPair fu) {
                sum[2]++;
                if (INC[0])
                    assertTrue(sum[1] < (Integer)fu.u);
                assertTrue(fu.t >= prev[0]);
                prev[0] = fu.t;
                sum[1] = (Integer)fu.u;
                sum[0] += sum[1];
            }
        };
        t.traverse(dod); System.out.println("");
        assertEquals(10, sum[2]);
        assertEquals(111111111, sum[0]);
        
        t.put(0.1f, -1);
        t.put(0.01f, -10);
        t.put(0.001f, -100);
        t.put(0.0001f, -1000);
        t.put(0.00001f, -10000);
        t.put(0.000001f, -100000);
        assertEquals(16, t.size);

        sum[0] = 0; sum[1] = Integer.MIN_VALUE; sum[2] = 0; prev[0] = Float.NEGATIVE_INFINITY;
        INC[0] = false;
        t.traverse(dod); System.out.println("");
        assertEquals(16, sum[2]);
        assertEquals(111000000, sum[0]);

        int n = 27;
        for(int i = 1; i < (1<<n); i <<= 1)
        {
            t.put((float)i, i);
            t.put((float)(i-1), 1-i);
        }
        System.out.println("");
        assertEquals(16 + n*2, t.size);
        
        sum[0] = 0; sum[1] = Integer.MIN_VALUE; sum[2] = 0; prev[0] = Float.NEGATIVE_INFINITY;
        t.traverse(dod); System.out.println("");
        assertEquals(16 + n*2, sum[2]);
        assertEquals(111000000 + n, sum[0]);
    }
    
}
