package org.konte.lang.func;

import java.awt.Image;
import java.awt.image.BufferedImage;
import org.konte.lang.Tokens.ContextualFunction;
import org.konte.model.Model;


public class Img {

    private static int getCol(Image image, float... val)
    {
        return getColF(image, (int)val[1], (int)val[2]);
    }
    private static int getColF(Image image, int u, int v)
    {
        u = u % image.getWidth(null);
        if (u < 0) u = image.getWidth(null) + u;
        v = v % image.getHeight(null);
        if (v < 0) v = image.getHeight(null) + v;
        return ((BufferedImage)image).getRGB(u,v);
    }
    private static float rval(int rgb) {
        return (float)((rgb >> 16) & 0xFF)/255f;
    }
    private static float gval(int rgb) {
        return (float)((rgb >> 8) & 0xFF)/255f;
    }
    private static float bval(int rgb) {
        return (float)(rgb & 0xFF)/255f;
    }
    private static float aval(int rgb) {
        return (float)((rgb >> 24) & 0xFF)/255f;
    }
    private static float interpolate(float xd, float yd, float ul, float ll, float ur, float lr) {
        float l = ul + yd * (ll - ul);
        float r = ur + yd * (lr - ur);
        return l + xd * (r - l);
    }

    public static class EImgRed extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgRed(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            int u = (int)val[1];
            int v = (int)val[2];
            float xd = val[1] - u;
            float yd = val[2] - v;
            Image image = model.bitmapCache.imageArr[img];
            int ul = getColF(image, u, v);
            int ll = getColF(image, u, v+1);
            int ur = getColF(image, u+1, v);
            int lr = getColF(image, u+1, v+1);
            int col = getCol(image, val);
            return interpolate(xd, yd, rval(ul), rval(ll), rval(ur), rval(lr));
        }
    }
    public static class EImgGreen extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgGreen(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            int u = (int)val[1];
            int v = (int)val[2];
            float xd = val[1] - u;
            float yd = val[2] - v;
            Image image = model.bitmapCache.imageArr[img];
            int ul = getColF(image, u, v);
            int ll = getColF(image, u, v+1);
            int ur = getColF(image, u+1, v);
            int lr = getColF(image, u+1, v+1);
            int col = getCol(image, val);
            return interpolate(xd, yd, gval(ul), gval(ll), gval(ur), gval(lr));
        }
    }
    public static class EImgBlue extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgBlue(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            int u = (int)val[1];
            int v = (int)val[2];
            float xd = val[1] - u;
            float yd = val[2] - v;
            Image image = model.bitmapCache.imageArr[img];
            int ul = getColF(image, u, v);
            int ll = getColF(image, u, v+1);
            int ur = getColF(image, u+1, v);
            int lr = getColF(image, u+1, v+1);
            int col = getCol(image, val);
            return interpolate(xd, yd, bval(ul), bval(ll), bval(ur), bval(lr));
        }
    }
    public static class EImgAlpha extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgAlpha(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            int u = (int)val[1];
            int v = (int)val[2];
            float xd = val[1] - u;
            float yd = val[2] - v;
            Image image = model.bitmapCache.imageArr[img];
            int ul = getColF(image, u, v);
            int ll = getColF(image, u, v+1);
            int ur = getColF(image, u+1, v);
            int lr = getColF(image, u+1, v+1);
            int col = getCol(image, val);
            return interpolate(xd, yd, aval(ul), aval(ll), aval(ur), aval(lr));
        }
    }
    public static class EImgWidth extends ContextualFunction {
        @Override public int getArgsCount() { return 1; }
        public EImgWidth(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            Image image = model.bitmapCache.imageArr[img];
            return image.getWidth(null);
        }
    }
    public static class EImgHeight extends ContextualFunction {
        @Override public int getArgsCount() { return 1; }
        public EImgHeight(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            Image image = model.bitmapCache.imageArr[img];
            return image.getHeight(null);
        }
    }
}
