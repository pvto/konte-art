package org.konte.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.HashMap;
import java.util.List;
import org.konte.model.MeshSqu;
import org.konte.misc.Matrix4;
import org.konte.misc.Point2;
import org.konte.misc.Vector3;
import org.konte.model.Background;
import org.konte.model.GlobalLighting;
import org.konte.model.Model;
import org.konte.generate.Runtime;
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
    private GeneralPath path;
    private AffineTransform toScreen;
    private final HashMap<RenderingHints.Key, Object> renderHints = new HashMap<>();

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

    public void init(int width, int height)
    {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setBackground(image, bg.getColor());
        if (model != null && model.canvasEffects.size() > 0)
        {
            layerimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            setBackground(layerimg, new Color(0, 0, 0, 0));
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

    public void drawCurve(Camera camera, OutputShape shape)
    {
        Matrix4 orig = shape.matrix;
        draw.setColor(shape.getColor());
        //draw.setColor(lighting.lightObject(shape));
        path.reset();
        List<? extends List<Matrix4>> shapes = shape.shape.getShapes();
        for (int j = 0; j < shapes.size(); j++)
        {
            List<Matrix4> m = shapes.get(j);
            List<Matrix4[]> pcs = shape.shape.getControlPoints().get(j);
            Matrix4 tmp = orig.multiply(m.get(0));
            Point2 p2 = camera.mapTo2D(v3d.set(tmp.m03, -tmp.m13, tmp.m23));
            Matrix4 otmp = tmp;
            path.moveTo(p2.x, p2.y);
            Point2 p3;
            Point2 p4;
            for (int i = 1; i <= m.size(); i++)
            {
                tmp = i == m.size() ? otmp : orig.multiply(m.get(i));
                p2 = camera.mapTo2D(v3d.set(tmp.m03, -tmp.m13, tmp.m23));
                Matrix4[] bends = pcs.get((i - 1) % m.size());
                if (bends == null)
                {
                    path.lineTo(p2.x, p2.y);
                }
                else
                {
                    tmp = orig.multiply(bends[0]);
                    p3 = camera.mapTo2D(v3d.set(tmp.m03, -tmp.m13, tmp.m23));
                    tmp = orig.multiply(bends[1]);
                    p4 = camera.mapTo2D(v3d.set(tmp.m03, -tmp.m13, tmp.m23));
                    path.curveTo(p3.x, p3.y, p4.x, p4.y, p2.x, p2.y);
                }
            }
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
        Matrix4 orig = shape.matrix;
//        draw.setColor(lighting.lightObject(shape));
        draw.setColor(shape.getColor());
        for (List<Matrix4> m : shape.shape.getShapes())
        {
            path.reset();

            Matrix4 tmp = orig.multiply(m.get(0));
            Point2 p2 = camera.mapTo2D(v3d.set(tmp.m03, -tmp.m13, tmp.m23));
            path.moveTo(p2.x, p2.y);
            for (int i = 1; i < m.size(); i++)
            {
                tmp = orig.multiply(m.get(i));
                p2 = camera.mapTo2D(v3d.set(tmp.m03, -tmp.m13, tmp.m23));
                path.lineTo(p2.x, p2.y);
            }
            path.closePath();
            path.transform(toScreen);
            draw.fill(path);
        }

    }

    public void drawSphere(Camera camera, OutputShape shape)
    {
        Matrix4 orig = shape.matrix;
//        draw.setColor(lighting.lightObject(shape));
        draw.setColor(shape.getColor());
        Matrix4 m = orig.multiply(HALF);
        Point2 p2 = camera.mapTo2D(v3d.set(orig.m03, -orig.m13, orig.m23));
        Point2 p3 = camera.mapTo2D(v3d.set(m.m03, -m.m13, m.m23));
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
                setBackground(layerimg, new Color(0, 0, 0, 0));
            }
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
            setBackground(layerimg, new Color(0, 0, 0, 0));
        }
    }
}
