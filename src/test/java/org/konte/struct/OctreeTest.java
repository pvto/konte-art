
package org.konte.struct;

import org.junit.Test;

public class OctreeTest {


    @Test
    public void testCocentric() {
        Octree octree = new Octree();
        for(int i = 0; i < 1000; i++)
        {
            octree.place(0.1, 0.1, 0.1, 1);
        }
    }

}