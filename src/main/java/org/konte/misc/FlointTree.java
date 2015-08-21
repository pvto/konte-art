package org.konte.misc;


/**
 * This is a somewhat memory-hungry fast-put fast-get tree
 * implementation.
 * 
 * Put operations to java.util.TreeMap slow down somewhere near 1e7 items, 
 * whereas FlointTree gives better latencies beyond.
 * 
 * Beware though that if your data is spread over a big subspace of R, 
 * this will slow down and eventually run out of memory due to generating 
 * a massive amount of relatively empty branch arrays. In such a case,
 * try reducing DECBITS to 3 (supported minimum), or better still,
 * modify this class to drop decimal branching out altogether.  You can also
 * raise the proportional bit weights of node-0 and node-1.
 * 
 * 
 * 
 * Statistics.
 * <to be inserted here>
 * 
 * "Floint" comes as a word-bag from float+int, meaning that integer arithmetic
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
 * there is an ordered linked list in place that use the actual floating point
 * key for absolute ordering.
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
    static{ init(17, 8, 15, 5, 5); }
    
    private static void init(int N0, int N1, int DECBITS, int N3, int N4)
    {
        if (N0 + N1 > 28) throw new RuntimeException("must have N0 + N1 <= 28");
        if (N3 + N4 > 12) throw new RuntimeException("must have N3 + N4 <= 12");
        if (N0 < 8) throw new RuntimeException("must have N0 >= 8");
        if (N1 < 2) throw new RuntimeException("must have N1 >= 2");
        if (N2 < 2) throw new RuntimeException("must have N2 >= 2");
        if (N3 < 1) throw new RuntimeException("must have N3 >= 2");
        if (N4 < 1) throw new RuntimeException("must have N4 >= 2");
        if (N5 < 1) throw new RuntimeException("must have N5 >= 2");
        if ((DECBITS < 3)
        || (DECBITS > 32)) throw new RuntimeException("can only map 3 <= x <= 32 decimal bits");
        FlointTree.N0 = N0;
        FlointTree.N1 = N1;
        FlointTree.N2 = 32 - N0 + 1 - N1;
        FlointTree.DECBITS = DECBITS;
        FlointTree.N3 = N3;
        FlointTree.N4 = N4;
        FlointTree.N5 = DECBITS - N3 - N4;
	N0shr = 32 - N0;
	N1shr = N1;
	N3shr = DECBITS - N3;
        N4shr = N5;
	N0Nbm = (1 << (N0 - 1)) - 1;
	N0Padd = (1 << (N0 - 1));
	N1bm = (1 << (32 - N0 + 1)) - 1;
	N2bm = (1 << (32 - N0 + 1 - N1)) - 1;
	N4bm = (1 << N4) - 1;
        N5bm = (1 << N5) - 1;
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
        return (int) ((f - (int)f) * 32768f);
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
    
    /** A simple ordered linked list of key-values.
    * Preserves insertion order while storing equal-key items.
    */ 
    public static class Node6 {
        
        public FUPair firstChild;
        
        public void put(float rank, Object o)
        {
            
            FUPair x = new FUPair(rank, o);
            if (firstChild == null)
            {
                firstChild = x;
                return;
            }
            if (firstChild.t > rank)
            {
                x.next = firstChild;
                firstChild = x;
                return;
            }
            FUPair
                    y0 = firstChild,
                    y = firstChild.next
                    ;
            while(y != null && y.t <= rank)
            {
                y0 = y;
                y = y.next;
            }
            y0.next = x;
            x.next = y;
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
    
}
