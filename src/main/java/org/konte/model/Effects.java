package org.konte.model;

import org.konte.image.OutputShape;
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
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg) {
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

    public static final EffectApply MIX = new Untransformable.EffectApply() {
        
        @Override public int xcontext(OutputShape s)
        { 
            return Math.max(1, (((s.col >>> 24) + 1) & 0xFF) >> 3);
        }
        @Override public int ycontext(OutputShape s)
        { 
            return xcontext(s);
        }
        @Override
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg) {
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
                    int j = v + (int) (Math.random() * eh) - (ew >> 1);//model.getRandomFeed().random()
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
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg) {
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
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape, int bg) {
            int ew = xcontext(shape); int eh = ew;
            if (u < ew || u > w - 1 - ew || v < eh || v > h - 1 - eh)
                return;
            int r = 0, g = 0, b = 0, a = 0;
            int ind = u + w * v;
            for(int j = -1; j <= 1; j++)
                for(int i = -1; i <= 1; i++)
                {
                    if (i == 0 && j == 0) 
                        continue;
                    int x = data[ind + i + j * w];
                    if ((x >> 24 & 0xFF) > 127) a++;
                    if ((x >> 16 & 0xFF) > 127) r++;
                    if ((x >> 8 & 0xFF) > 127) g++;
                    if ((x & 0xFF) > 127) b++;
                }
            int A = (dest[ind] >> 24 & 0xFF);
            int R = (dest[ind] >> 16 & 0xFF);
            int G = (dest[ind] >> 8 & 0xFF);
            int B = (dest[ind] & 0xFF);
            if (A > 127) { if (a < 2 || a > 3) A = 255 - A; } else { if (a == 3) A = 255 - A; }
            if (R > 127) { if (r < 2 || r > 3) R = 255 - R; } else { if (r == 3) R = 255 - R; }
            if (G > 127) { if (g < 2 || g > 3) G = 255 - G; } else { if (g == 3) G = 255 - G; }
            if (B > 127) { if (b < 2 || b > 3) B = 255 - B; } else { if (b == 3) B = 255 - B; }
            dest[ind] = A << 24 | R << 16 | G << 8 | B;
        }
    };
}
