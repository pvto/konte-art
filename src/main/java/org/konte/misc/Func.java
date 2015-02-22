package org.konte.misc;

public final class Func {
    
    public static void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException ex) {
            
        }
    }
}