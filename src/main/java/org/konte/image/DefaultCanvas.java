package org.konte.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.*;

import org.konte.model.MeshSqu;
import org.konte.misc.Matrix4;
import org.konte.misc.Point2;
import org.konte.misc.Vector3;
import org.konte.model.Background;
import org.konte.model.GlobalLighting;
import org.konte.model.Model;
import org.konte.generate.Runtime;
import org.konte.misc.Matrix4Red;
import org.konte.model.Untransformable.EffectApply;
/**
 *
 * @author pvto
 */
public class DefaultCanvas implements Canvas {

    private final Model model;
    private final Background bg;
    private final GlobalLighting lighting;
    protected int width,
            height;
    protected Graphics2D draw;
    private BufferedImage image;
    private BufferedImage layerimg;
    private BufferedImage tmpImage;
    private GeneralPath path;
    private AffineTransform toScreen;
    private final HashMap<RenderingHints.Key, Object> renderHints = new HashMap<>();
    private int layerRenders = 0;
    private boolean finished = false;

    public DefaultCanvas(Model model, Background bg, GlobalLighting lighting)
    {
        this.model = model;
        this.bg = bg;
        this.lighting = lighting;
        renderHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    public GeneralPath getPath()
    {
        return path;
    }

    public AffineTransform getScreenTransform()
    {
        return toScreen;
    }

    @Override
    public void init(int width, int height)
    {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setBackground(image, bg.getColor());
        if (model != null && model.isDrawLayersSeparately())
        {
            layerimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            setBackground(layerimg, new Color(0, 0, 0, 0));
            tmpImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        else
        {
            layerimg = image;
        }
        draw = layerimg.createGraphics();
        draw.setRenderingHints(renderHints);

        path = new GeneralPath();
        int size = getPixelSizeFactor();
        toScreen = new AffineTransform(size, 0, 0, size, width / 2, height / 2);
    }

    @Override
    public void finish() {
        finished = true;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setBackground(Color color)
    {
        setBackground(image, color);
    }

    public void setBackground(BufferedImage image, Color color)
    {
        DataBuffer ar = image.getAlphaRaster().getDataBuffer();
        int col = ((int) color.getAlpha() << 24)
                + ((int) color.getRed() << 16)
                + ((int) color.getGreen() << 8)
                + ((int) color.getBlue());
        for (int i = 0; i < width * height; i++)
        {
            ar.setElem(i, col);
        }
    }
    final private Matrix4 HALF = Matrix4.translation(0.5f, 0f, 0f);
    final private Vector3 v3d = new Vector3();

    public synchronized BufferedImage getImage()
    {
        if (finished) {
            return image;
        }
        if (model != null && model.isDrawLayersSeparately())
        {
            tmpImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            tmpImage.getGraphics().drawImage(image, 0, 0, null);
            if (layerimg != image) {
                tmpImage.getGraphics().drawImage(layerimg, 0, 0, null);
            }
            tmpImage.flush();
            return tmpImage;
        }
        return image;
    }

    public void pathReset()
    {
        path.reset();
    }

    public void pathMoveTo(float x, float y)
    {
        path.moveTo(x, y);
    }

    public void pathLineTo(float x, float y)
    {
        path.lineTo(x, y);
    }

    public void pathCurveTo(float c1x, float c1y, float c2x, float c2y, float p2x, float p2y)
    {
        path.curveTo(c1x, c1y, c2x, c2y, p2x, p2y);
    }

    public void closePath()
    {
        path.closePath();
        path.transform(toScreen);
        draw.fill(path);
    }

    public void setColor(Color c)
    {
        draw.setColor(c);
    }


    @Override
    public void drawCurve(Camera camera, OutputShape shape)
    {
        Matrix4Red orig = shape.matrix;
        draw.setColor(shape.getColor());
        //draw.setColor(lighting.lightObject(shape));
        path.reset();
        List<? extends Iterable<Matrix4>> shapes = shape.shape.getShapes();
        for (int j = 0; j < shapes.size(); j++)
        {
            Iterator<Matrix4> m = ((Iterable<Matrix4>) shapes.get(j)).iterator();
            Iterator<Matrix4[]> pcs = ((Iterable<Matrix4[]>)shape.shape.getControlPoints().get(j)).iterator();
            Matrix4 b = m.next();
            Matrix4 first = b;
            Point2 p2 = camera.mapTo2D(v3d.setXyz(orig, b));
            path.moveTo(p2.x, p2.y);
            Point2 p3;
            Point2 p4;
            do
            {
                if (m.hasNext()) {
                    b = m.next();
                } else {
                    b = first;
                    first = null;
                }
                p2 = camera.mapTo2D(v3d.setXyz(orig, b));
                Matrix4[] bends = pcs.next();
                if (bends == null)
                {
                    path.lineTo(p2.x, p2.y);
                }
                else
                {
                    b = bends[0];
                    p3 = camera.mapTo2D(v3d.setXyz(orig, b));
                    b = bends[1];
                    p4 = camera.mapTo2D(v3d.setXyz(orig, b));
                    path.curveTo(p3.x, p3.y, p4.x, p4.y, p2.x, p2.y);
                }
            } while (first != null);
            path.closePath();
        }
        path.transform(toScreen);
        draw.fill(path);
    }

    public void drawMeshPiece(Camera camera, OutputShape shape)
    {
        //draw.setColor(lighting.lightObject(shape));
        draw.setColor(shape.getColor());
        path.reset();
        float[][] co = ((MeshSqu) shape.shape).coords;
        Point2 p2 = camera.mapTo2D(v3d.set(co[0][0], -co[0][1], co[0][2]));
        path.moveTo(p2.x, p2.y);
        for (int i = 1; i < 4; i++)
        {
            p2 = camera.mapTo2D(v3d.set(co[i][0], -co[i][1], co[i][2]));
            path.lineTo(p2.x, p2.y);
        }
        path.closePath();
        path.transform(toScreen);
        draw.fill(path);

    }

    public void drawPolygon(Camera camera, OutputShape shape)
    {
        Matrix4Red orig = shape.matrix;
//        draw.setColor(lighting.lightObject(shape));
        draw.setColor(shape.getColor());
        for (Iterable<Matrix4> m : shape.shape.getShapes())
        {
            path.reset();
            Iterator<Matrix4> it = m.iterator();
            Matrix4 b = it.next();
            Point2 p2 = camera.mapTo2D(v3d.setXyz(orig, b));
            path.moveTo(p2.x, p2.y);
            while(it.hasNext())
            {
                b = it.next();
                p2 = camera.mapTo2D(v3d.setXyz(orig, b));
                path.lineTo(p2.x, p2.y);
            }
            path.closePath();
            path.transform(toScreen);
            draw.fill(path);
        }

    }

    public void drawSphere(Camera camera, OutputShape shape)
    {
        Matrix4Red orig = shape.matrix;
//        draw.setColor(lighting.lightObject(shape));
        draw.setColor(shape.getColor());
        Matrix4 b = HALF;
        Point2 p2 = camera.mapTo2D(v3d.set(orig.m03, -orig.m13, orig.m23));
        Point2 p3 = camera.mapTo2D(v3d.setXyz(orig, b));
        float dx = (p3.x - p2.x);
        float dy = (p3.y - p2.y);
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float fact = 0.55f;
        path.reset();
        path.moveTo(p2.x, p2.y - r);
        path.curveTo(p2.x + r * fact, p2.y - r, p2.x + r, p2.y - r * fact, p2.x + r, p2.y);
        path.curveTo(p2.x + r, p2.y + r * fact, p2.x + r * fact, p2.y + r, p2.x, p2.y + r);
        path.curveTo(p2.x - r * fact, p2.y + r, p2.x - r, p2.y + r * fact, p2.x - r, p2.y);
        path.curveTo(p2.x - r, p2.y - r * fact, p2.x - r * fact, p2.y - r, p2.x, p2.y - r);

        path.closePath();
        path.transform(toScreen);
        draw.fill(path);

    }

    public int getPixelSizeFactor()
    {
        return Math.max(getWidth(), getHeight());
    }

    @Override
    public void applyEffects(Model model, float layer)
    {
        List<CanvasEffect> effects = model.canvasEffects.get(layer);
        if (effects == null)
        {
            if (layerimg != image)
            {
                image.getGraphics().drawImage(layerimg, 0, 0, null);
            }
            else {
                layerimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                draw = layerimg.createGraphics();
                draw.setRenderingHints(renderHints);
            }
//            setBackground(layerimg, new Color(0, 0, 0, 0));
            layerRenders++;
            return;
        }
        for(CanvasEffect effect : effects)
        {
            Runtime.sysoutln("applying effect " + effect, 5);
            int[][] m = effect.matrix;
            int[] M = effect.copy;
            DataBuffer ar = layerimg.getAlphaRaster().getDataBuffer();

            int[] ref = new int[m.length * m[0].length];
            for(int i = 0; i < ref.length; i++)
            {
                ref[i] = width * (-m.length / 2 + i / m[0].length) - m[0].length / 2 + i % m[0].length;
            }
            int len = ar.getSize();
            int[] tmp = new int[ar.getSize()];
            for(int rep = 0 ; rep < effect.repeat; rep++)
            {
                for(int i = 0; i < len; i++)
                {
                    int tota = 0;
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    int nadd = 0;
                    int type = 0;
                    for (int u = 0; u < ref.length; u++)
                    {
                        int off = i + ref[u];
                        if (off < 0 || off >= len)
                            continue;
                        int v2 = ar.getElem(off);
                        int a2 = v2 >> 24 & 0xFF;
                        if (M[u] != 0)
                        {
                            nadd++;
                            tota += a2;
                        }
                        if ((M[u] & 1) == 1)
                        {
                            b += (v2 & 0xFF);
                        }
                        if ((M[u] & 2) == 2)
                        {
                            g += (v2 >> 8 & 0xFF);
                        }
                        if ((M[u] & 4) == 4)
                        {
                            r += (v2 >> 16 & 0xFF);
                        }
                    }
                    tota = Math.min(255, tota / nadd);
                    r = Math.min(255, r / nadd);
                    g = Math.min(255, g / nadd);
                    b = Math.min(255, b / nadd);
                    tmp[i] = tota << 24 | r << 16 | g << 8 | b;
                }
                for (int i = 0; i < tmp.length; i++)
                {
                    ar.setElem(i, tmp[i]);
                }
            }
        }
        if (layerimg != image)
        {
            image.getGraphics().drawImage(layerimg, 0, 0, null);
        }
    }

    @Override
    public void drawEffect(Camera camera, OutputShape shape, EffectApply how)
    {
        BufferedImage img = layerimg;
        int background = (img == this.image ? bg.getColor().getRGB() : 0x00FFFFFF);
        //layerimg.getRaster().getPixels(width, width, width, width, iArray);
        Matrix4Red orig = shape.matrix;

        List<Point2> lp = new ArrayList<>();
        for (Iterable<Matrix4> m : shape.shape.getShapes())
        {
            for (Matrix4 b : m)
            {
                Point2 p2 = camera.mapTo2D(v3d.setXyz(orig, b));
                p2.x = (int) (p2.x * toScreen.getScaleX() + toScreen.getTranslateX());
                p2.y = (int) (p2.y * toScreen.getScaleY() + toScreen.getTranslateY());
                lp.add(p2);
            }
        }
        int ind = 0, miny, maxy, minx, maxx;
        miny = maxy = (int) lp.get(0).y;
        minx = maxx = (int) lp.get(0).x;
        for (int i = 1; i < lp.size(); i++)
        {
            Point2 p = lp.get(i);
            if (p.y < miny) { ind = i;  miny = (int) p.y; }
            else if (p.y > maxy) { maxy = (int) p.y; }
            if (p.x < minx) { minx = (int)p.x; }
            else if (p.x > maxx) { maxx = (int)p.x; }
        }
        if (miny == maxy) return;
        while(--ind >= 0) { lp.add(lp.remove(0)); }
        int leftU = 0, rightU = 0;
        int leftD = lp.size() - 1;
        int ldir = -1;
        int rightD = 1;
        int rdir = 1;
        if (lp.get(leftD).x > lp.get(rightD).x)
        {
            int tmp = rightD; rightD = leftD; leftD = tmp;
            ldir = - ldir; rdir = -rdir;
        }
        if (maxx < 0 || maxy < 0 || minx >= img.getWidth() || miny > img.getHeight())
            return;
        int u0 = Math.max(0, minx - how.xcontext(shape));
        int v0 = Math.max(0, miny - how.ycontext(shape));
        int u1 = Math.min(img.getWidth() - 1, maxx + how.xcontext(shape));
        int v1 = Math.min(img.getHeight() - 1, maxy + how.ycontext(shape));
        int w = u1 - u0;
        int h = v1 - v0;

        int[] data = (int[]) img.getAlphaRaster().getDataElements(u0, v0, w, h, null);

        int[] dest = Arrays.copyOf(data, data.length);
        int y = (int)miny;
        while (y <= maxy)
        {
            if (y < v0) { y++; continue; }
            if (y >= v1) { break; }
            Point2 LU = lp.get(leftU);
            Point2 LD = lp.get(leftD);
            Point2 RU = lp.get(rightU);
            Point2 RD = lp.get(rightD);
            while (y > LD.y || LU.y == LD.y && LU.x >= LD.x)
            {
                leftU = leftD;
                leftD = leftD + ldir;
                if (leftD == -1) leftD = lp.size() - 1;
                else if (leftD == lp.size()) leftD = 0;
                LU = LD;
                LD = lp.get(leftD);
            }
            while (y > RD.y || RU.y == RD.y && RU.x <= RD.x)
            {
                rightU = rightD;
                rightD = rightD + rdir;
                if (rightD == -1) rightD = lp.size() - 1;
                else if (rightD == lp.size()) rightD = 0;
                RU = RD;
                RD = lp.get(rightD);
            }
            int x0, x1;
            if (LU.y == LD.y)
            {
                x0 = (int) ((LU.x + LD.x) / 2.0f);
            }
            else if (y == LU.y)
            {
                x0 = (int) LU.x;
            }
            else
            {
                x0 = (int) (LU.x + (LD.x-LU.x) * (y - LU.y) / (LD.y-LU.y));
            }
            if (RU.y == RD.y)
            {
                x1 = (int) ((RU.x + RD.x) / 2.0f);
            }
            else if (y == RU.y)
            {
                x1 = (int) RU.x;
            }
            else
            {
                x1 = (int) (RU.x + (RD.x-RU.x) * (y - RU.y) / (RD.y-RU.y));
            }
            if (x0 < u0) { x0 = u0; }
            if (x1 >= u1) { x1 = u1 - 1; }
            for(int i = x0 - u0; i <= x1 - u0; i++)
            {
                how.apply(data, dest, w, h, i, y - v0, shape, background);
            }
            y++;
        }
        img.getAlphaRaster().setDataElements(u0, v0, w, h, dest);
    }


    @Override
    public void initLayer(Model model, float layer) {
        if (layerimg != image) {
            setBackground(layerimg, new Color(0, 0, 0, 0));
        }
    }
}
