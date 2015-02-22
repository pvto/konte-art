
package org.konte.generate;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import org.konte.model.BitmapCache;

/**
 *
 * @author pvto
 */
public class MemoryWatch {

    private static long memorySizeFactor;
    private MemoryMXBean mbb;

    
    public MemoryWatch()
    {
        mbb = java.lang.management.ManagementFactory.getMemoryMXBean();
        setMemorySizeFactor((mbb.getHeapMemoryUsage().getCommitted() - 16000) / 2);
    }


    private static void setMemorySizeFactor(long max)
    {
        memorySizeFactor = max;
    }

    public static long getMemorySizeFactor()
    {
        return memorySizeFactor;
    }

    public static int getMoveCountEstimate()
    {
        return (int)(getMemorySizeFactor()*2/5/500);
    }
    
}
