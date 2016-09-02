package org.konte.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public abstract class PrefixStringMap {

    public static final List<PrefixStringMap> ALL_PREFIX_STRING_MAPS = new ArrayList<>();
    
    public static final void init(PrefixStringMap map) 
    {
        ALL_PREFIX_STRING_MAPS.add(map);
    }
    
    public final String prefix;
    public final Map<String,String> exceptions = new HashMap<>();
    
    
    public PrefixStringMap(String prefix, String[] exceptions)
    {
        this.prefix = prefix;
        for(int i = 0; i < exceptions.length; )
        {
            String a = exceptions[i++];
            String b = exceptions[i++];
            this.exceptions.put(a, b);
        }
    }
    
    public String mapByRule(String a)
    {
        return prefix + a;
    }
    
    public String map(String a)
    {
        String b = exceptions.get(a);
        if (b != null)
            return b;
        b = mapByRule(a);
        return b;
    }
}
