package org.konte.model.systems;

import org.konte.model.GreyBoxSystem;
import org.konte.model.Model;
import org.konte.model.systems.GradientFunction.Gradients;
import org.konte.model.systems.GradientFunction.PowGradient;

/** A mutable perlin noise system in 2d.
 *  Adapted from https://en.wikipedia.org/wiki/Perlin_noise .
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class PerlinNoise2DSystem implements GreyBoxSystem {

    public int w, h;
    public float[][][] Gradient;
    public GradientFunction gf = Gradients.SMOOTHSTEP.gf;

    private Model model;




    // Computes the dot product of the distance and gradient vectors.
    float dotGridGradient(int ix, int iy, float x, float y)
    {

        int ixc = ix % w;
        int iyc = iy % h;

        // Compute the distance vector
        float dx = x - (float) ix - Gradient[iyc][ixc][2];
        float dy = y - (float) iy - Gradient[iyc][ixc][3];

        // Compute the dot-product
        return (dx * Gradient[iyc][ixc][0] + dy * Gradient[iyc][ixc][1]);
    }

    // Compute Perlin noise at coordinates x, y
    public float perlin(float x, float y)
    {

        x = normalizeX(x);
        y = normalizeY(y);
        // Determine grid cell coordinates
        int x0 = (int) x;
        int x1 = x0 + 1;
        int y0 = (int) y;
        int y1 = y0 + 1;

        // Determine interpolation weights
        float sx = x - (float) x0;
        float sy = y - (float) y0;

        // Interpolate between grid point gradients
        float n0, n1, ix0, ix1, value;
        n0 = dotGridGradient(x0, y0, x, y);
        n1 = dotGridGradient(x1, y0, x, y);
        ix0 = gf.gradient(n0, n1, sx);
        n0 = dotGridGradient(x0, y1, x, y);
        n1 = dotGridGradient(x1, y1, x, y);
        ix1 = gf.gradient(n0, n1, sx);
        value = gf.gradient(ix0, ix1, sy);

        return value;
    }

    public void setGridVal(int x, int y, float angle)
    {
        Gradient[y][x][0] = (float)Math.cos(angle);
        Gradient[y][x][1] = (float)Math.sin(angle);
    }

    @Override
    public GreyBoxSystem newInstance()
    {
        return new PerlinNoise2DSystem();
    }

    @Override
    public void initialize(Object[] args)
    {
        int w = this.w = args.length > 1 ? ((Float)args[0]).intValue() : 16;
        int h = this.h = args.length > 2 ? ((Float)args[1]).intValue() : w;
        Gradient = new float[h][w][4];
        Object grad = args.length > 3 ? args[2] : "SMOOTHSTEP";
        if (grad instanceof Float) {
            this.gf = new PowGradient((Float)grad);
        } else {
            Gradients g = Gradients.valueOf(grad.toString());
            this.gf = g.gf;
        }
        model = (Model)args[args.length - 1];
    }

    @Override
    public void evaluate(float[] args) { initInternal(); }

    @Override
    public float read(float[] args)
    {
        initInternal();
        float x = args[1];
        float y = args[2];
        return perlin(x, y);
    }

    public float normalizeX(float x)
    {
        x = x % w;
        if (x < 0) x += w;
        return x;
    }

    public float normalizeY(float y)
    {
        y = y % h;
        if (y < 0) y += h;
        return y;
    }

    @Override
    public void write(float[] args)
    {
        initInternal();
        int x = (int) normalizeX(args[1]);
        int y = (int) normalizeY(args[2]);
        float potential = args[3];
        setGridVal(x, y, potential);
        if (args.length > 4) { Gradient[y][x][2] = args[4]; }
        if (args.length > 5) { Gradient[y][x][3] = args[5]; }
    }

    private boolean initialized = false;
    private void initInternal()
    {
        if (initialized)
            return;
        initialized = true;

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                float ang = (float)( model.getRandomFeed().get() * Math.PI * 2.0 );
                setGridVal(j, i, ang);
            }
        }
    }
}
