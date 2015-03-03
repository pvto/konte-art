package org.konte.generate;

import org.konte.misc.LCG64RndShift;
import org.konte.misc.RndGenerator;

public class RandomFeed {
    
    
    private String key = null;
    public RndGenerator rand;

    public void setRndGenerator(RndGenerator rndGenerator)
    {
        rand = rndGenerator;
        rand.setSeed(convertKey(key));
    }
    

    public RandomFeed()
    {
        char[] cc = new char[3];
        for (int i = 0; i < cc.length; i++)
        {
            cc[i] = (char) ('A' + Math.floor(Math.random() * 25));
        }
        this.key = String.valueOf(cc);
        setRndGenerator(new LCG64RndShift());
    }

    private long convertKey()
    {
        return convertKey(key);
    }
    public static long convertKey(String key)
    {
        long base = 1;
        long res = 0;
        for (int i = key.length()-1; i >= 0; i--)
        {
            res += ((int)(key.charAt(i)-'A')+1)*base;
            base <<= 5;
        }
        return res;        
    }
    public static String toConvertKey(long l)
    {
        StringBuilder bd = new StringBuilder();
        long top = 1;

        while (top < 60 && l >= (1 << (top -1)))
        {
            long matchrange = 31 << (top-1);
            long match = l & matchrange;
            int val = (int)(match >> (top-1));
            if (val == 0) val = 1;
            bd.insert(0,(char)(64 + Math.min(val,26)));
            top += 5;
                    //Math.min(l, l)) )
        }
        return bd.toString();
    }
    public RandomFeed(String key)
    {
        this();
        setKey(key);
    }

    public void incrementKey()
    {
        char[] cc = key.toCharArray();
        for (int i = cc.length - 1; i > 0; i--)
        {
            cc[i] = (char) (cc[i] + 1);
            if (cc[i] > (char) ('A' + 25))
            {
                cc[i] = 'A';
            }
            else
            {
                return;
            }
        }
    }

    public void setKey(String key)
    {
        this.key = key.toUpperCase().substring(0, Math.min(12,key.length()));
        rand.setSeed(convertKey(this.key));
    }

    public String getKey()
    {
        return key;
    }

    public double get()
    {
        return rand.nextDouble();
    }
    
    public static void main(String[] args)
    {
        System.out.println(toConvertKey(1)+convertKey("A"));
        System.out.println(toConvertKey(25)+convertKey("Y"));
        System.out.println(toConvertKey(33)+convertKey("AA"));
        System.out.println(toConvertKey(34)+convertKey("AB"));
        System.out.println(toConvertKey(58)+convertKey("AZ"));
        System.out.println(toConvertKey(65)+convertKey("BA"));
        System.out.println(toConvertKey(67)+convertKey("BC"));
        System.out.println(toConvertKey(90)+convertKey("BZ"));
        System.out.println(toConvertKey(858)+convertKey("ZZ"));
        System.out.println(toConvertKey(1057)+convertKey("AAA"));
        System.out.println(toConvertKey(1059)+convertKey("AAC"));
        System.out.println(toConvertKey(1082)+convertKey("AAZ"));
        System.out.println(toConvertKey(1089)+convertKey("ABA"));
        System.out.println(toConvertKey(33825)+convertKey("AAAA"));
        System.out.println(toConvertKey(1082401)+convertKey("AAAAA"));
        System.out.println(toConvertKey(28142426)+convertKey("ZZZZZ"));
        System.out.println(toConvertKey(34636833)+convertKey("AAAAAA"));
        System.out.println(toConvertKey(900557658)+convertKey("ZZZZZZ"));
    }

}
