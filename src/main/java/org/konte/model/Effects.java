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
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape) {
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
                    for (int i = -ew; i <= ew; i++)
                    {
                        for (int j = -eh; j <= eh; j++)
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
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape) {
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
        public void apply(int[] data, int[] dest, int w, int h, int u, int v, OutputShape shape) {
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
                    dest[u + w * v] = 0x00FFFFFF;
                }
            }
        }
    };
}
