package org.konte.misc;

/** Adapted from Melissa O'Neill's lecture notes
 * at https://www.youtube.com/watch?v=45Oet5qjlms.
 *
 * This generator is not thread safe.
 * 
 * It uses the same LCG parameters as java's java.util.Random
 * (mult = 25214903917L and add = 11)
 * 
 * Tempering mask for float conversion adapted from Mersenne Twister
 * implementation by Michael Lecuyer, .
 * 
 * @author pvto https://github.com/pvto
 */
public class LCG64RndShift implements RndGenerator {

    public final long 
            mult = 25214903917L,
            add = 11L
            ;
    public long 
            state = (long)(Math.random() * Long.MAX_VALUE)
            ;
    
    public static final int 
            TEMPERING_MASK_B = 0x9d2c5680,
            TEMPERING_MASK_C = 0xefc60000
            ;

    public LCG64RndShift()
    {
        this.state = 0;
    }
    
    public LCG64RndShift(long state)
    {
        this.state = state;
    }
    
    @Override
    public void setSeed(long seed) {
        this.state = seed;
    }

    public void rotate()
    {
        state = state * mult + add;
    }
    
    public long rndShift(long longval)
    {
        int shift = (29 - (int)(state >> 61)) & 0xFF;
        return longval >> shift;
    }
    
    public int nextInt()
    {
        rotate();
        long shifted = rndShift(state);
        return (int)shifted;
    }
    
    private int temper(int intval)
    {
        int i = intval;
        i ^= i >>> 11;
        i ^= (i << 7) & TEMPERING_MASK_B;
        i ^= (i << 15) & TEMPERING_MASK_C;
        i ^= (i >>> 18);
        return i;
    }

    @Override
    public double nextDouble()
    {
        rotate();
        int y = (int)rndShift(state);
        y = temper(y);
        rotate();
        int z = (int)rndShift(state);
        return ((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53);
    }
    
    public double nextFloat()
    {
        int i = nextInt();
        i ^= i >>> 11;
        i ^= (i << 7) & TEMPERING_MASK_B;
        i ^= (i << 15) & TEMPERING_MASK_C;
        i ^= (i >>> 18);
        return (i >>> 8) / ((float) (1 << 24));
    }

}
