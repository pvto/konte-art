package org.konte.model.systems;

import org.konte.model.GreyBoxSystem;
import org.konte.model.Model;
import org.konte.model.systems.GradientFunction.Gradients;
import org.konte.model.systems.GradientFunction.PowGradient;

/** A mutable perlin noise system in 3d.
 *  Adapted from https://en.wikipedia.org/wiki/Perlin_noise .
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class PerlinNoise3DSystem implements GreyBoxSystem {

    public int w, h, d;
    public float[][][][] Gradient;
    public GradientFunction gf = Gradients.SMOOTHSTEP.gf;
    
    private Model model;
    
    
    
    


    // Computes the dot product of the distance and gradient vectors.
    float dotGridGradient(int ix, int iy, int iz, float x, float y, float z)
    {

        int ixc = ix % w;
        int iyc = iy % h;
        int izc = iz % d;
        
        // Compute the distance vector
        float dx = x - (float) ix - Gradient[izc][iyc][ixc][3];
        float dy = y - (float) iy - Gradient[izc][iyc][ixc][4];
        float dz = z - (float) iz - Gradient[izc][iyc][ixc][5];

        // Compute the dot-product
        return (dx * Gradient[izc][iyc][ixc][0] 
                + dy * Gradient[izc][iyc][ixc][1]
                + dz * Gradient[izc][iyc][ixc][2]);
    }

    // Compute Perlin noise at coordinates x, y
    public float perlin(float x, float y, float z)
    {

        x = normalizeX(x);
        y = normalizeY(y);
        z = normalizeZ(z);
        // Determine grid cell coordinates
        int x0 = (int) x;
        int x1 = x0 + 1;
        int y0 = (int) y;
        int y1 = y0 + 1;
        int z0 = (int) z;
        int z1 = z0 + 1;
        // Determine interpolation weights
        float sx = x - (float) x0;
        float sy = y - (float) y0;
        float sz = z - (float) z0;

        // Interpolate between grid point gradients
        float n0, n1, ix0, ix1, iy0, iy1, value;
        n0 = dotGridGradient(x0, y0, z0, x, y, z);
        n1 = dotGridGradient(x1, y0, z0, x, y, z);
        ix0 = gf.gradient(n0, n1, sx);
        n0 = dotGridGradient(x0, y1, z0, x, y, z);
        n1 = dotGridGradient(x1, y1, z0, x, y, z);
        ix1 = gf.gradient(n0, n1, sx);
        iy0 = gf.gradient(ix0, ix1, sy);

        n0 = dotGridGradient(x0, y0, z1, x, y, z);
        n1 = dotGridGradient(x1, y0, z1, x, y, z);
        ix0 = gf.gradient(n0, n1, sx);
        n0 = dotGridGradient(x0, y1, z1, x, y, z);
        n1 = dotGridGradient(x1, y1, z1, x, y, z);
        ix1 = gf.gradient(n0, n1, sx);
        iy1 = gf.gradient(ix0, ix1, sy);

        value = gf.gradient(iy0, iy1, sz);
        
        return value;
    }

    public void setGridVal(int x, int y, int z, float rotz, float rotx)
    {
        float xt = (float)Math.cos(rotz);
        float yt = (float)Math.sin(rotz);
        Gradient[z][y][x][2] = (float) Math.sin(rotx) * xt;
        Gradient[z][y][x][0] = (float) Math.cos(rotx) * xt;
    }
    
    @Override
    public GreyBoxSystem newInstance()
    {
        return new PerlinNoise3DSystem();
    }

    @Override
    public void initialize(Object[] args)
    {
        int w = this.w = args.length > 1 ? ((Float)args[0]).intValue() : 16;
        int h = this.h = args.length > 2 ? ((Float)args[1]).intValue() : w;
        int d = this.d = args.length > 3 ? ((Float)args[2]).intValue() : h;
        Gradient = new float[d][h][w][6];
        Object grad = args.length > 4 ? args[3] : "SMOOTHSTEP";
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
        float z = args[3];
        return perlin(x, y, z);
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

    public float normalizeZ(float z)
    {
        z = z % d;
        if (z < 0) z += d;
        return z;
    }
    
    @Override
    public void write(float[] args)
    {
        initInternal();
        int x = (int) normalizeX(args[1]);
        int y = (int) normalizeY(args[2]);
        int z = (int) normalizeZ(args[3]);
        float rotz = args[4];
        float rotx = args[5];
        setGridVal(x, y, z, rotz, rotx);
        if (args.length > 5) { Gradient[z][y][x][2] = args[5]; }
        if (args.length > 6) { Gradient[z][y][x][3] = args[6]; }
        if (args.length > 7) { Gradient[z][y][x][4] = args[7]; }
    }
    
    private boolean initialized = false;
    private void initInternal()
    {
        if (initialized)
            return;
        initialized = true;
        
        for(int k = 0; k < d; k++) {
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    float rotz = (float)( model.getRandomFeed().get() * Math.PI * 2.0 );
                    float rotx = (float)( model.getRandomFeed().get() * Math.PI * 2.0 );
                    setGridVal(i, j, k, rotz, rotx);
                }
            }
        }
    }
}
