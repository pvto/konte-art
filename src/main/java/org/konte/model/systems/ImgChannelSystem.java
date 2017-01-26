package org.konte.model.systems;

import org.konte.model.GreyBoxSystem;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class ImgChannelSystem implements GreyBoxSystem {

    int w, h;
    float[][] data;
    
    @Override
    public GreyBoxSystem newInstance() { return new ImgChannelSystem(); }
    
    @Override
    public void initialize(Object[] args)
    {
        if (args.length != 2)
            throw new RuntimeException("ImgChannelSystem: expecting two args, width and height - got " + args.length);
        int width = ((Float)args[0]).intValue();
        if (width <= 0)
            throw new RuntimeException("ImgChannelSystem: expecting positive width component, got " + width);
        int height = ((Float)args[0]).intValue();
        if (height <= 0)
            throw new RuntimeException("ImgChannelSystem: expecting positive height component, got " + height);

        data = new float[height][width];
        this.w = width;
        this.h = height;
    }

    @Override
    public void evaluate(float[] args) { /*noop*/ }

    @Override
    public float read(float[] args)
    {
        int x = (int)args[1];
        int y = (int)args[2];
        int wrap = 0;
        if (args.length > 3) {
            wrap = (int)args[3];
        }
        if (wrap <= 0) {
            if (x < 0 || x >= w)
                return 0f;
            if (y < 0 || y >= h)
                return 0f;
        } else {
            x = wrap(x, w);
            y = wrap(y, h);
        }
        return data[y][x];
    }
    
    private int wrap(int x, int h)
    {
        if (x < 0)
            x = data[0].length + (-x % data[0].length);
        else if (x >= data[0].length)
            x = x % data[0].length;
        return x;
    }

    @Override
    public void write(float[] args) {
        int x = (int)args[1];
        int y = (int)args[2];
        int wrap = 1;
        if (args.length > 4) {
            wrap = (int)args[4];
        }
        if (wrap <= 0) {
            if (x < 0 || x >= w)
                return;
            if (y < 0 || y >= h)
                return;
        } else {
            x = wrap(x, w);
            y = wrap(y, h);
        }
        data[y][x] = args[3];
    }

}
