package org.konte.lang.func;

import java.util.HashMap;
import java.util.Map;
import org.konte.lang.Tokens;

/**
 * @author pvto https://github.com/pvto
 */
public final class Prob {

    static long prod(int n)
    {
        return prodCache[n];
    }
    
    static long npr(int n, int r)
    {
        long res = 1L;
        for(int i = n; i >= r; i--)
            res *= (long)i;
        return res;
    }

    static long ncr(int n, int r)
    {
        int m = Math.max(r, n-r);
        r = n - m;
        long res = 1L;
        int j = 2;
        for(int i = n; i > m; i--)
        {
            res *= (long)i;
            if (j <= r && res % j == 0)
                res /= j++;
        }
        while(j <= r)
            res /= j++;
        return res;
    }

    static double binm(int n, double p, int x)
    {
        checkp(p);
        if (x < 0 || x > n)
            throw new IllegalArgumentException("binm("+n+","+p+") [0..+"+n+"] not defined for " + x);
        return ncr(n,x) * Math.pow(p, x) * Math.pow(1.0 - p, n - x);
    }

    static double binmCuml(int n, double p, int x)
    {
        checkp(p);
        double res = 0;
        int y = (n - x < x) ? n - x - 1 : x;
        for(int i = 0; i <= y; i++)
            res += binm(n, p, i);
        return (y < x) ? 1.0 - res : res;
    }

    static double binmE(int n, double p)
    {
        checkp(p);
        return n * p;
    }
    
    static double binmVar(int n, double p)
    {
        checkp(p);
        return n * p * (1.0 - p);
    }
    
    static int binmRnd(int n, double p, double rndFromU)
    {
        checkp(p);
        double[] cached = getBinmCache(n, p);
        double x = 0.0;
        int i = 0;
        while(x < rndFromU)
        {
            x += cached[i++ - 1];
        }
        return i - 1;
    }
    
    static double hypg(int N1, int N2, int n, int x)
    {
        return ncr(N1, x) * ncr(N2, n - x) / (double)ncr(N1+N2, n);
    }

    static double hypgCuml(int N1, int N2, int n, int x)
    {
        double res = 0;
        int y = (n - x < x) ? n - x - 1 : x;
        for(int i = 0; i <= y; i++)
            res += hypg(N1, N2, n, i);
        return (y < x) ? 1.0 - res : res;
    }

    static double hypgE(int N1, int N2, int n, int x)
    {
        return x * N1 / (double)(N1 + N2);
    }

    static double hypgVar(int N1, int N2, int n, int x)
    {
        throw new UnsupportedOperationException("nep");
    }
    
    static int hypgRnd(int N1, int N2, int n, double rndFromU)
    {
        double z = 0.0;
        int i = 0;
        while(z < rndFromU)
        {
            z += hypg(N1, N2, n, i++ - 1);
        }
        return i - 1;
    }
    
    static double negbinm(int x, double p, int r)
    {
        checkp(p);
        return ncr(x - 1, r - 1) * Math.pow(p, r) * Math.pow(1.0 - p, x-r);
    }
    
    static double negbinmE(int x, double p)
    {
        checkp(p);
        if (p == 0)
            return 0;
        return x / p;
    }
    
    static double negbinmVar(int x, double p)
    {
        checkp(p);
        if (p == 0)
            return 0;
        return -1;
    }
    
    
    public static class EProd extends Tokens.Function1 {

        public EProd(String name) { super(name); }

        @Override
        public float value(float... val) {
            return (float) prod((int)Math.floor(val[0]));
        }        
    }
    
    public static class Enpr extends Tokens.Function2 {
        public Enpr(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int n = (int)Math.floor(val[0]);
            int r = (int)Math.floor(val[1]);
            return (float)npr(n,r);
        }
    }

    public static class Encr extends Tokens.Function2 {
        public Encr(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int n = (int)Math.floor(val[0]);
            int r = (int)Math.floor(val[1]);
            return (float)ncr(n,r);
        }
    }
    
    public static class EBinm extends Tokens.Function3 {
        public EBinm(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int n = (int)Math.floor(val[0]);
            float p = val[1];
            int k = (int)Math.floor(val[2]);
            return (float)binm(n, p, k);
        }
    }

    public static class EBincuml extends Tokens.Function3 {
        public EBincuml(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int n = (int)Math.floor(val[0]);
            float p = val[1];
            int k = (int)Math.floor(val[2]);
            return (float)binmCuml(n, p, k);
        }
    }

    public static class ERndbin extends Tokens.Function2 {
        public ERndbin(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int n = (int)Math.floor(val[0]);
            float p = val[1];
            double rnd = Math.random();
            return (float)binmRnd(n, p, rnd);
        }
    }
    
    public static class EHypg extends Tokens.Function4 {
        public EHypg(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int N1 = (int)Math.floor(val[0]);
            int N2 = (int)Math.floor(val[1]);
            int n = (int)Math.floor(val[2]);
            int x = (int)Math.floor(val[3]);
            return (float)hypg(N1, N2, n, x);
        }
    }
    
    public static class EHypgcuml extends Tokens.Function4 {
        public EHypgcuml(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int N1 = (int)Math.floor(val[0]);
            int N2 = (int)Math.floor(val[1]);
            int n = (int)Math.floor(val[2]);
            int x = (int)Math.floor(val[3]);
            return (float)hypgCuml(N1, N2, n, x);
        }
    }

    public static class ERndhypg extends Tokens.Function3 {
        public ERndhypg(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int N1 = (int)Math.floor(val[0]);
            int N2 = (int)Math.floor(val[1]);
            int n = (int)Math.floor(val[2]);
            double rnd = Math.random();
            return (float)hypgRnd(N1, N2, n, rnd);
        }
    }
    
    public static class ENegbinm extends Tokens.Function3 {
        public ENegbinm(String name) { super(name); }
        
        @Override
        public float value(float... val) {
            int x = (int)Math.floor(val[0]);
            float p = (int)Math.floor(val[1]);
            int r = (int)Math.floor(val[2]);
            return (float)negbinm(x, p, r);
        }
    }
    
    static private void checkp(double p)
    {
        if (p < 0 || p > 1.0)
            throw new IllegalArgumentException("binm: p outside of [0..1]");
    }
    
    static private final long[] prodCache = new long[40];
    static {
        long x = prodCache[0] = 1L;
        for(int i = 1; i < prodCache.length; i++)
            prodCache[i] = prodCache[i - 1] * (long)i;
    }

    private static Map binmcache = new HashMap();
    private static double[] getBinmCache(int n, double p)
    {
        double key = n+p/2.0;
        double[] cached = (double[])binmcache.get(key);
        if (cached == null)
        {
            binmcache.put(key, cached = new double[n+1]);
            for(int i = 0; i <= n; i++)
                cached[i] = binm(n, p, i);
        }
        return cached;
    }
    

}
