package org.konte.lang.func;

import java.awt.Image;
import java.awt.image.BufferedImage;
import org.konte.lang.Tokens.ContextualFunction;
import org.konte.model.Model;


public class Img {

    public static class EImgRed extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgRed(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            Image image = model.bitmapCache.imageArr[img];
            int col = ((BufferedImage)image).getRGB((int)val[1], (int)val[2]);
            return (float)((col >> 16) & 0xFF)/255f;
        }
    }
    public static class EImgGreen extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgGreen(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            Image image = model.bitmapCache.imageArr[img];
            int col = ((BufferedImage)image).getRGB((int)val[1], (int)val[2]);
            return (float)((col >> 8) & 0xFF)/255f;
        }
    }
    public static class EImgBlue extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgBlue(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            Image image = model.bitmapCache.imageArr[img];
            int col = ((BufferedImage)image).getRGB((int)val[1], (int)val[2]);
            return (float)(col & 0xFF)/255f;
        }
    }
    public static class EImgAlpha extends ContextualFunction {
        @Override public int getArgsCount() { return 3; }
        public EImgAlpha(String name, Model model) { super(name, model); }
        @Override
        public float value(float... val)
        {
            int img = (int)val[0];
            Image image = model.bitmapCache.imageArr[img];
            int col = ((BufferedImage)image).getRGB((int)val[1], (int)val[2]);
            return (float)((col >> 24) & 0xFF)/255f;
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
