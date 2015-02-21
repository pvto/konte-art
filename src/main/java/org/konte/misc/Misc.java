package org.konte.misc;

public class Misc {
    
    public static String replicate(String s, int n)
    {
        StringBuilder bd = new StringBuilder();
        for(int i=  0; i < n; i++)
            bd.append(s);
        return bd.toString();
    }
}
