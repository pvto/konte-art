package org.konte.model;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import org.konte.image.OutputShape;
import org.konte.misc.Mathc3;
import org.konte.model.Untransformable.EffectApply;

/**
 * @author Paavo Toivanen https://github.com/pvto
 */
public class Effects {

    
    
    public static final EffectApply GBLUR = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return Math.max(1, (((s.col >>> 24) + 1) & 0xFF) >> 3);
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int r = 0, g = 0, b = 0, a = 0;
            int ew = xcontext(shape); int eh = ew;
            if (u < ew)
            {

            }
            else if (u > w - 1 - ew)
            {

            }
            else
            {
                if (v < eh) {}
                else if (v > h - 1 - eh) {}
                else
                {
                    int u_jvw = u + (-eh + v) * w;
                    for (int j = -eh; j <= eh; j++)
                    {
                        for (int i = -ew; i <= ew; i++)
                        {
                            int ind = i + u_jvw;
                            b += data[ind] & 0xFF;
                            g += (data[ind] >> 8  & 0xFF);
                            r += (data[ind] >> 16  & 0xFF);
                            a += (data[ind] >>> 24);
                        }
                        u_jvw += w;
                    }
                    int nadd = ((ew << 1) | 1) * ((eh << 1) | 1);
                    a = Math.min(255, a / nadd);
                    r = Math.min(255, r / nadd);
                    g = Math.min(255, g / nadd);
                    b = Math.min(255, b / nadd);
                    dest[u + w * v] = a << 24 | r << 16 | g << 8 | b;
                }
            }
        }
    };

    private static int getBright(int a) {
        return Math.max((a & 0xFF), Math.max((a >>> 8) & 0xFF, Math.max((a >>> 16) & 0xFF, (a >>> 24) & 0xFF)));
    }
    private static int getDark(int a) {
        return -Math.max((a & 0xFF), Math.max((a >>> 8) & 0xFF, Math.max((a >>> 16) & 0xFF, (a >>> 24) & 0xFF)));
    }

    private static Comparator<Integer> brightComp = new Comparator<Integer>() {
        public int compare(Integer a, Integer b) { return getBright(a) - getBright(b); }
    };
    private static Comparator<Integer> darkComp = new Comparator<Integer>() {
        public int compare(Integer a, Integer b) { return getDark(a) - getDark(b); }
    };

    public static final EffectApply BITRATE = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return Math.max(1, (((s.col >>> 24) + 1) & 0xFF) >> 3);
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int ew = xcontext(shape); int eh = ew;
            int ew2 = 2 * ew + 1;
            int eh2 = ew2;
            if (u < 0) return;
            if (u > w - 1 - ew2) return;
            if (v < 0) return;
            if (v > h - 1 - eh2) return;
            if (u % ew2 != 0) return;
            if (v % eh2 != 0) return;

            Integer[] stuff = new Integer[ew2*eh2];
            int mode = (shape.col >>> 16) & 0xFF;
            int quantile = (shape.col >>> 8) & 0xFF;
            int u_jvw = u + v * w;
            int sind = 0;
            for (int j = 0; j < eh2; j++)
            {
                for (int i = 0; i < ew2; i++)
                    stuff[sind++] = data[i + u_jvw];
                u_jvw += w;
            }
            Comparator<Integer> comp = null;
            switch(mode) {
                case 1: comp = darkComp; break;
                case 0: default: comp = brightComp; break;
            }
            Arrays.sort(stuff, comp);
            int src = 0;
            switch(mode) {
                case 1:
                    src = stuff[(int)(quantile / 255.0 * stuff.length)];
                    break;
                case 0:
                default:
                    int n = 0;
                    int maxn = 0;
                    int maxind = 0;
                    int ind = 0;
                    int ex = -1;
                    for(int i = 0; i < stuff.length; i++) {
                        if (ex != stuff[i]) {
                            n = 0;
                            ex = stuff[i];
                        }
                        n++;
                        if (n > maxn) {
                            maxn = n;
                            src = stuff[i];
                        }
                    }
                    break;
            }
            u_jvw = u + v * w;
            for (int j = 0; j < eh2; j++)
            {
                for (int i = 0; i < ew2; i++)
                    dest[u_jvw++] = src;
                u_jvw += w - ew2;
            }

        }
    };
    
    
    private static final double LOOK_4PI = 1024.0 / Math.PI;
    private static final double LOOK_2PI = 512.0 / Math.PI;
    private static final double LOOK_PI = 256.0 / Math.PI;
    private static final double LOOK_PIPER2 = 128.0 / Math.PI;
    private static final double LOOK_PIPER8 = 32.0 / Math.PI;
    
    private static final double[] cos = new double[256];
    
    
    private static final double cosx(double rad)
    { 
        int d = (int) Math.round(rad / Math.PI * 256);
        if (d < 0) d = -d;
        d = d & 0x01FF;
        boolean neg = d > 127 && d < 384;
        d = d & 0x00FF;
        return neg ? -cos[d] : cos[d];
    }
    private static final double sinx(double rad)
    { 
        return cosx(rad - Math.PI / 2.0);
    }
    
    static {
        for (int i = 0; i < cos.length; i++)
        {
            cos[i] = Math.abs(Math.cos(i / 255.0 * Math.PI));
        }
    }
    
    public static final EffectApply RADBLUR = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return Math.max(1, ((s.col >>> 24) & 0xFF) >> 3);
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            double alpha = Math.atan2(v - h / 2.0, u - w / 2.0);
            double turn = ((shape.col >> 16) & 0xFF) / LOOK_2PI - Math.PI / 4;
            alpha = alpha + turn;
            
            int r = 0, g = 0, b = 0, a = 0;
            int ew = xcontext(shape); int eh = ew;
            if (u < ew)
            {

            }
            else if (u > w - 1 - ew)
            {

            }
            else
            {
                if (v < eh) {}
                else if (v > h - 1 - eh) {}
                else
                {
                    int count = 0;
                    for(float f = -ew; f <= ew; f++)
                    {
                        int x = (int) Math.round(u + cosx(alpha) * f);
                        if (x < 0 || x >= w) continue;
                        double y = Math.round(v + sinx(alpha) * f);
                        if (y < 0 || y >= h) continue;
                        int col = data[(int)(x + y * w)];
                        a += (col >> 24) & 0xFF;
                        r += (col >> 16) & 0xFF;
                        g += (col >> 8) & 0xFF;
                        b += col & 0xFF;
                        count++;
                    }
                    a = a / count;
                    r = r / count;
                    g = g / count;
                    b = b / count;
                    dest[u + w * v] = a << 24 | r << 16 | g << 8 | b;
                }
            }
        }
    };
    
    
    

    public static final EffectApply MIX = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return Math.max(2, (((s.col >>> 24) + 1) & 0xFF) >> 3);
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int r = 0, g = 0, b = 0, a = 0;
            int ew = xcontext(shape); int eh = ew;
            if (u < ew)
            {

            }
            else if (u > w - 1 - ew)
            {

            }
            else
            {
                if (v < eh) {}
                else if (v > h - 1 - eh) {}
                else
                {
                    int i = u + (int) (Math.random() * ew) - (ew >> 1);//model.getRandomFeed().random()
                    if (i < 0) { return; } else if (i >= w) { return; }
                    int j = v + (int) (Math.random() * eh) - (eh >> 1);//model.getRandomFeed().random()
                    if (j < 0) { return; } else if (j >= h) { return; }
                    int nadd = ((ew << 1) | 1) * ((eh << 1) | 1);
                    a = Math.min(255, a / nadd);
                    r = Math.min(255, r / nadd);
                    g = Math.min(255, g / nadd);
                    b = Math.min(255, b / nadd);
                    dest[u + w * v] = dest[i + w * j];
                }
            }
        }
    };

    public static final EffectApply RUBBER = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return Math.max(1, (((s.col >>> 24) + 1) & 0xFF) >> 3);
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int r = 0, g = 0, b = 0, a = 0;
            int ew = xcontext(shape); int eh = ew;
            if (u < ew)
            {

            }
            else if (u > w - 1 - ew)
            {

            }
            else
            {
                if (v < eh) {}
                else if (v > h - 1 - eh) {}
                else
                {
                    dest[u + w * v] = bg;
                }
            }
        }
    };
    
    public static final EffectApply GOL = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return 1;
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int ew = xcontext(shape); int eh = ew;
            if (u < ew || u > w - 1 - ew || v < eh || v > h - 1 - eh)
                return;
            int DIV = (shape.col >> 24 & 0xFF) >> 1;
            int r = 0, g = 0, b = 0, a = 0;
            int ind = u + w * v;
            for(int j = -1; j <= 1; j++)
                for(int i = -1; i <= 1; i++)
                {
                    if (i == 0 && j == 0) 
                        continue;
                    int x = data[ind + i + j * w];
                    if ((x >> 24 & 0xFF) > DIV) a++;
                    if ((x >> 16 & 0xFF) > DIV) r++;
                    if ((x >> 8 & 0xFF) > DIV) g++;
                    if ((x & 0xFF) > DIV) b++;
                }
            int A = (dest[ind] >> 24 & 0xFF);
            int R = (dest[ind] >> 16 & 0xFF);
            int G = (dest[ind] >> 8 & 0xFF);
            int B = (dest[ind] & 0xFF);
            
            if (A > DIV) { if (a < 2 || a > 3) A = 255 - A; } else { if (a == 3) A = 255 - A; }
            if (R > DIV) { if (r < 2 || r > 3) R = 255 - R; } else { if (r == 3) R = 255 - R; }
            if (G > DIV) { if (g < 2 || g > 3) G = 255 - G; } else { if (g == 3) G = 255 - G; }
            if (B > DIV) { if (b < 2 || b > 3) B = 255 - B; } else { if (b == 3) B = 255 - B; }
            dest[ind] = A << 24 | R << 16 | G << 8 | B;
        }
    };
    
    public static final EffectApply EDGE = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return 1;
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int ew = xcontext(shape); int eh = ew;
            if (u < ew || u > w - 1 - ew || v < eh || v > h - 1 - eh)
                return;
            int DIFF = Math.max(1, shape.col >> 24 & 0xFF);

            
            int ind = u + w * v;
            int A = (dest[ind] >> 24 & 0xFF);
            int R = (dest[ind] >> 16 & 0xFF);
            int G = (dest[ind] >> 8 & 0xFF);
            int B = (dest[ind] & 0xFF);
            
            int n = 9;
            for(int j = -1; j <= 1; j++)
                for(int i = -1; i <= 1; i++)
                {
                    if (i == 0 && j == 0) 
                        continue;
                    int x = data[ind + i + j * w];
                    if (Math.abs((x >> 24 & 0xFF) - A) > DIFF
                        || Math.abs((x >> 16 & 0xFF) - R) > DIFF
                        || Math.abs((x >> 8 & 0xFF) - G) > DIFF
                        || Math.abs((x & 0xFF) - B) > DIFF
                    ) n--;
                }

            if (n > 2 && n < 8) { /*noop*/ } // has 3..8 quite same colors
            else { dest[ind] &= 0x00FFFFFF; } // make non-edges transparent (susceptible to other operations!)
        }
    };


    
    public static final EffectApply RADTR = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return Math.max(1, (((s.col >>> 24)) & 0xFF) >> 3);
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            double alpha = Math.atan2(v - h / 2.0, u - w / 2.0);
            double turn = ((shape.col >> 16) & 0xFF) / 31.75 * Math.PI - Math.PI * 4;
            double dist = Math.sqrt((u-w/2.0)*(u-w/2.0)+(v-h/2.0)*(v-h/2.0));
            alpha = alpha + turn * dist / w;
            double radialDistance = ((shape.col >> 0) & 0xFF) / 127.0 * dist;
            
            int x = (int) (w / 2.0 + Math.cos(alpha) * radialDistance );
            if (x < 0 || x >= w) return;
            int y = (int) (h / 2.0 + Math.sin(alpha) * radialDistance );
            if (y < 0 || y >= h) return;
            int ind = x + w * y;
            dest[u + w * v] = data[ind];
        }
    };
    
    
    public static final EffectApply BRIGHTNESS = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return 1;
        }
        @Override public int ycontext(OutputShape s)
        { 
            return 1;
        }
        private float[] tmpvals = new float[3];
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int ind = u + w * v;
            int adjusted = data[ind];
            int b = data[ind] & 0xFF; 
            int g = data[ind] >> 8  & 0xFF;
            int r = data[ind] >> 16  & 0xFF; 
            float level = (float)(shape.col & 0xFF) / 128f - 1f;
            int a = data[ind] >>> 24 & 0xFF;
            Color.RGBtoHSB(r, g, b, tmpvals);
            tmpvals[2] = Mathc3.bounds1(
                    tmpvals[2] + level
            );
            adjusted = Color.HSBtoRGB(tmpvals[0],tmpvals[1],tmpvals[2]);
            adjusted = adjusted & 0x00FFFFFF | (data[ind] & 0xFF000000);
            dest[ind] = adjusted; 
        }
    };
    
    public static final EffectApply CONTRAST = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return 1;
        }
        @Override public int ycontext(OutputShape s)
        { 
            return 1;
        }
        private float[] tmpvals = new float[3];
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int ind = u + w * v;
            int adjusted = data[ind];
            int b = data[ind] & 0xFF; 
            int g = data[ind] >> 8  & 0xFF;
            int r = data[ind] >> 16  & 0xFF; 
            int a = data[ind] >>> 24 & 0xFF;
            Color.RGBtoHSB(r, g, b, tmpvals);
            float level = (float)(shape.col & 0xFF) / 127.5f;
            tmpvals[2] = Mathc3.bounds1(
                    0.5f + (float)(tmpvals[2] - 0.5f) * level
            );
            adjusted = Color.HSBtoRGB(tmpvals[0],tmpvals[1],tmpvals[2]);
            adjusted = adjusted & 0x00FFFFFF | (data[ind] & 0xFF000000);
            dest[ind] = adjusted; 
        }
    };
    
    public static final EffectApply SATURATE = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return 1;
        }
        @Override public int ycontext(OutputShape s)
        { 
            return 1;
        }
        private float[] tmpvals = new float[3];
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg)
        {
            int ind = u + w * v;
            int adjusted = data[ind];
            int b = data[ind] & 0xFF; 
            int g = data[ind] >> 8  & 0xFF;
            int r = data[ind] >> 16  & 0xFF; 
            int a = data[ind] >>> 24 & 0xFF;
            Color.RGBtoHSB(r, g, b, tmpvals);
            float level = ((float)(shape.col & 0xFF) / 255f - 0.5F) * 2F;
            tmpvals[1] = Mathc3.bounds1(
                   0.5f + (float)(tmpvals[1] - 0.5f) + level
            );
            adjusted = Color.HSBtoRGB(tmpvals[0],tmpvals[1],tmpvals[2]);
            adjusted = adjusted & 0x00FFFFFF | (data[ind] & 0xFF000000);
            dest[ind] = adjusted; 
        }
    };
    
    
    public static void main(String[] args) {
        for(int i = 0; i <= 16; i++)
        {
            double a = 2.0 * Math.PI / 16.0 * i;
            System.out.println(a/Math.PI*180 + "  cos " + cosx(a) + " sin " + sinx(a));
            System.out.println(-a/Math.PI*180 + "  -cos " + cosx(-a) + " -sin " + sinx(-a));
        }
    }
}
