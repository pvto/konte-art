package org.konte.misc;


/**
 * This is a somewhat memory-hungry fast-put fast-get tree
 * implementation.
 * 
 * Put operations to java.util.TreeMap slow down somewhere near 2e6 items, 
 * whereas FlointTree gives better latencies beyond.
 * 
 * Beware though that if your data is spread over a big subspace of R, 
 * FlointTree will slow down and eventually run out of memory due to
 * massive amounts of relatively empty branch arrays. 
 * Try reducing DECBITS to 3 (supported minimum), or better still,
 * modify this class to drop decimal branching out altogether.  You can also
 * raise the proportional bit weights of node-0 and node-1.
 * 
 * 
 * 
 * Statistics.
 * <to be inserted here>
 * 
 * "Floint" comes as a word-pocket from float+int, meaning that integer arithmetic
 * is used when actual tree keys are floating point numbers.
 * (That is, a tree key is a float, but all branch nodes map by ints derived
 * from that float.)
 * 
 * This uses a fixed hierarchy of 6 branch levels (3 levels for the integer part of the
 * key and 3 levels for the decimal part).  Root-attending branches, naturally,
 * are much wider (in terms of the possible number of active sub-branches) than
 * branches nearer to the leaves.
 * 
 * Beyond the precision provided by the integral hierarchy, 
 * there are an ordered linked list, for small sub-arrays, and custom a binary tree,
 * for larger sub-arrays. They use a floating point key (not boxed! :).
 * 
 * @author Paavo Toivanen https://github.com/pvto
 */
public class FlointTree {
        
    public int size = 0;
    public Node0 root = new Node0();

    // I didn't like to pass these parameters around.  Made them static.
    // --> Can use only one active parameterisation per VM!
    // You could copy-paste this class if you need more :P
    private static int
            DECBITS,    //nuber of bits for decimal branching
            N0, // node-0 array size in bits (first integral level node)
            N1, // node-1 array size in bits (second integral level node...)
            N2,
            N3, // node-3 array size in bits (first decimal level node...)
            N4,
            N5,
            N0shr,  // shift-right (shr) amount for node-0 key
            N1shr,
            N3shr,
            N4shr,
            N0Nbm,  // node-0 bitmap for negative float keys
            N0Padd, // node-0 array shift for positive float keys
            N1bm,   // node-1 bitmap for key
            N2bm,
            N4bm,
            N5bm
            ;
    private static float
            DECFLOAT
            ;
    static{ init(28, 18, 5, 15, 5, 5); }
    
    private static void init(int INTBITS, int N0, int N1, int DECBITS, int N3, int N4)
    {
        if ((INTBITS < 8)
        || (INTBITS > 32)) throw new RuntimeException("8 <= INTBITS <= 32 must be (Yoda said)");
        if (N0 + N1 > INTBITS - 4) throw new RuntimeException("must have N0 + N1 <= " + (INTBITS - 4));
        if (N3 + N4 > DECBITS - 2) throw new RuntimeException("must have N3 + N4 <= " + (DECBITS - 2));
        if (N0 < 5) throw new RuntimeException("must have N0 >= 5");
        if (N1 < 2) throw new RuntimeException("must have N1 >= 2");
        if (N0 + N1 > 30) throw new RuntimeException("must have N2 >= 2");
        if (N3 < 1) throw new RuntimeException("must have N3 >= 2");
        if (N4 < 1) throw new RuntimeException("must have N4 >= 2");
        if (N3 + N4 > DECBITS - 1) throw new RuntimeException("must have N5 >= 2");
        if ((DECBITS < 3)
        || (DECBITS > 32)) throw new RuntimeException("can only map 3 <= x <= 32 decimal bits");
        FlointTree.N0 = N0;
        FlointTree.N1 = N1;
        FlointTree.N2 = INTBITS - N0 + 1 - N1;
        FlointTree.DECBITS = DECBITS;
        FlointTree.N3 = N3;
        FlointTree.N4 = N4;
        FlointTree.N5 = DECBITS - N3 - N4;
	N0shr = INTBITS - N0;
	N1shr = N1;
	N3shr = DECBITS - N3;
        N4shr = N5;
	N0Nbm = (1 << (N0 - 1)) - 1;
	N0Padd = (1 << (N0 - 1));
	N1bm = (1 << (INTBITS - N0)) - 1;
	N2bm = (1 << (INTBITS - N0 + 1 - N1)) - 1;
	N4bm = (1 << N4) - 1;
        N5bm = (1 << N5) - 1;
        DECFLOAT = (float)(1 << DECBITS);
    }
    
    public void put(float rank, Object t)
    {
        int 
                i = mapIntPart(rank),
                d = mapDecimalPart(rank)
                ;
        root.put(i, d, rank, t);
        size++;
    }
    
    public static int mapIntPart(float f)
    {
        return (int)f;
    }
    
    public static int mapDecimalPart(float f)
    {
        return (int) ((f - (int)f) * DECFLOAT);
    }

    
    public static class Node0 {
        
        public Node1[] 
                children = new Node1[1<<N0]
                ;
        
        public void put(int ihash, int dhash, float rank, Object o)
        {
            int x;
            if ((ihash & 0x80000000) == 0x80000000) // negative key (rank < 0f)
            {
                x = (ihash >> N0shr) & N0Nbm;
            }
            else
            {
                x = (ihash >> N0shr) + N0Padd;
            }
            if (children[x] == null)
                children[x] = new Node1();
            children[x].put(ihash, dhash, rank, o);
        }
    }
    
    public static class Node1 {
        
        public Node2[] children = new Node2[1<<N1];
        
        public void put(int ihash, int dhash, float rank, Object o)
        {
            int x = (ihash & N1bm) >> N1shr;
            if (children[x] == null)
                children[x] = new Node2();
            children[x].put(ihash, dhash, rank, o);
        }
    }
    
    public static class Node2 {
        
        public Node3[] children = new Node3[1<<N2];
        
        public void put(int ihash, int dhash, float rank, Object o)
        {
            int x = (ihash & N2bm);
            if (children[x] == null)
                children[x] = new Node3();
            children[x].put(dhash, rank, o);
        }
    }
    
    public static class Node3 {
        
        public Node4[] children = new Node4[1<<N3];
        
        public void put(int dhash, float rank, Object o)
        {
            int x = (dhash >> N3shr);
            if (children[x] == null)
                children[x] = new Node4();
            children[x].put(dhash, rank, o);
        }
    }
 
    public static class Node4 {
        
        public Node5[] children = new Node5[1<<N4];
        
        public void put(int dhash, float rank, Object o)
        {
            int x = ((dhash >> N4shr) & N4bm);
            if (children[x] == null)
                children[x] = new Node5();
            children[x].put(dhash, rank, o);
        }
    }

    public static class Node5 {
        
        public Node6[] children = new Node6[1<<N5];
        
        public void put(int dhash, float rank, Object o)
        {
            int x = (dhash & N5bm);
            if (children[x] == null)
                children[x] = new Node6();
            children[x].put(rank, o);
        }
    }
    
    /** An ordered linked list of key-values.
    * Preserves insertion order while storing equal-key items.
    * 
    * If size grows, uses a binary tree instead of LList. (This helps HUGELY
    * when you insert many values with nearly the same key).
    */ 
    public static class Node6 {
        
        public FUPair 
                firstChild,
                lastChild
                ;
        public BinBranch optimization;
        byte problem = 0;
        private static final byte PROBLEM = 4;
        
        public void put(float rank, Object o)
        {
            FUPair x = new FUPair(rank, o);
            
            if (problem > PROBLEM)
            {
                putOptimization(rank, x);
                return;
            }

            problem++;
            if (firstChild == null)
            {
                firstChild = lastChild = x;
                return;
            }
            if (firstChild.t > rank)
            {
                x.next = firstChild;
                firstChild = x;
                return;
            }
            if (lastChild.t <= rank)
            {
                lastChild.next = x;
                lastChild = x;
                return;
            }
            FUPair 
                    y0 = firstChild,
                    y = y0.next
                    ;
            while(y != null && y.t <= rank)
            {
                y0 = y;
                y = y.next;
            }
            y0.next = x;
            x.next = y;
        }
        
        public void putOptimization(float rank, FUPair x)
        {
            if (optimization == null)
            {
                optimization = new BinBranch();
                if (PROBLEM == 0)
                {
                    optimization.first = optimization.last = x;
                    return;
                }
                else
                {
                    FUPair[] tmp = new FUPair[problem];
                    FUPair y = firstChild;
                    for(int i = 0; y != null; y = y.next)
                        tmp[i++] = y;
                    optimization.binaryFill(tmp, 0, tmp.length - 1);
                }
            }
            optimization.put(rank, x);
        }
        
    }

    
    
    public static class FUPair implements Comparable<FUPair> {
        
        public float t;
        public Object u;
        public FUPair next;
        
        public FUPair(float t, Object u)
        {
            this.t = t;
            this.u = u;
        }
        
        @Override
        public int compareTo(FUPair o)
        {
            if (t > o.t) return 1;
            if (t < o.t) return -1;
            return 0;
        }

    }
    
    
    
    public static class BinBranch {
        public FUPair first, last;
        public BinBranch 
                parent,
                lt,
                gt
                ;

        private void binaryFill(FUPair[] ordered, int left, int right)
        {
            int 
                    mid = (right + left) >> 1,
                    midleft = mid,
                    midright = mid
                    ;
            while(midleft > 0 && ordered[midleft - 1].t == ordered[midleft].t)
                midleft--;
            while(midright < right && ordered[midright + 1].t == ordered[midright].t)
                midright++;
            
            first = ordered[midleft];
            (last = ordered[midright]).next = null;
            
            if (midleft > left)
            {
                lt = new BinBranch();
                lt.parent = this;
                lt.binaryFill(ordered, left, midleft - 1);
            }
            if (midright < right)
            {
                gt = new BinBranch();
                gt.parent = this;
                gt.binaryFill(ordered, midright + 1, right);
            }
        }

        private void put(float rank, FUPair x)
        {
            put(this, rank, x);
        }
        private static void put(BinBranch bb, float rank, FUPair x)
        {
            for(;;)
            {
                if (rank == bb.last.t)
                {
                    bb.last.next = x;
                    bb.last = x;
                    return;
                }
                if (rank < bb.last.t)
                {
                    if (bb.lt == null)
                    {
                        bb.lt = new BinBranch();
                        bb.lt.parent = bb;
                        bb.lt.first = bb.lt.last = x;
                        return;
                    }
                    bb = bb.lt;
                    continue;
                }
                if (bb.gt == null)
                {
                    bb.gt = new BinBranch();
                    bb.gt.parent = bb;
                    bb.gt.first = bb.gt.last = x;
                    return;
                }
                bb = bb.gt;
            }
        }
        
        public interface Do {
            void now(BinBranch bb);
        }
        
        public void traverse(Do Do)
        {
            BinBranch bb = this;
            //while(bb.lt != null) bb = bb.lt;
            float 
                    max = Float.MIN_VALUE
                    ;
            while(bb != null)
            {
                if ((bb.lt == null || bb.lt.first.t <= max)
                        && bb.first.t > max)
                {
                    Do.now(bb);
                    max = bb.first.t;
                }
                if (bb.lt != null && bb.lt.first.t > max)
                {
                    bb = bb.lt;
                }
                else if (bb.gt != null && bb.gt.first.t > max)
                {
                    bb = bb.gt;
                }
                else
                {
                    if (bb == this)
                        return;
                    bb = bb.parent;
                }
            }
        }
    }
    
    public interface Do {
        void now(FUPair fu);
    }
    
    public void traverse(final Do now)
    {
        BinBranch.Do binDo = new BinBranch.Do() {

            @Override
            public void now(BinBranch bb)
            {
                FUPair fu = bb.first;
                while(fu != null)
                {
                    now.now(fu);
                    fu = fu.next;
                }
            }
        };
        for (int i = 0; i < root.children.length; i++) { Node1 N1 = root.children[i]; if (N1 != null)
            for (int j = 0; j < N1.children.length; j++) { Node2 N2 = N1.children[j]; if (N2 != null)
                for (int k = 0; k < N2.children.length; k++) { Node3 N3 = N2.children[k]; if (N3 != null)
                    for (int L = 0; L < N3.children.length; L++) { Node4 N4 = N3.children[L]; if (N4 != null)
                        for (int m = 0; m < N4.children.length; m++) { Node5 N5 = N4.children[m]; if (N5 != null)
                            for (int n = 0; n < N5.children.length; n++) { Node6 N6 = N5.children[n]; if (N6 != null)
                                {
                                    if (N6.optimization != null)
                                    {
                                        System.out.println("foo " + N6.optimization.first.t);
                                        binDo.now(N6.optimization);
                                        continue;
                                    }
                                    FUPair fu = N6.firstChild;
                                    while(fu != null)
                                    {
                                        now.now(fu);
                                        fu = fu.next;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
}
