
package org.konte.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.konte.expression.Expression;
import org.konte.expression.Value;
import org.konte.image.Camera;
import org.konte.image.Canvas;
import org.konte.image.DefaultCanvas;
import org.konte.lang.Language;
import org.konte.misc.Matrix4;
import org.konte.misc.Vector3;
import org.konte.model.Background;
import org.konte.model.DrawingContext;
import org.konte.model.GlobalLighting;
import org.konte.model.Path;
import org.konte.model.Transform;
import org.konte.model.Untransformable;

/**This will draw an editable/draggable path with control points,
 * mapping mouse coordinates to path coordinates.
 *
 * @author pvto
 */
public class PathPanel extends JPanel {



    private List<Path> paths;
    private Canvas canvas;
    private Camera camera;
    private int 
            lastActiveNode = -1,
            lastActiveBend = -1
            ;
    private float 
            centerX, centerY,
            xmin, xmax, ymin, ymax,
            dmax,
            zoomFactor = 1.6f,
            x0, y0,
            grid = 0.1f
            ;
    private int 
            stretchAlong
            ;

    public Path getPath()
    {
        if (paths.size() == 0)
            return null;
        return paths.get(0);
    }

    public void setPath(Path path)
    {
        this.paths = new ArrayList<Path>();
        paths.add(path);
    }

    public Canvas getCanvas()
    {
        return canvas;
    }

    public void setCanvas(Canvas canvas)
    {
        this.canvas = canvas;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera;
    }

    public int getLastActiveNode()
    {
        return lastActiveNode;
    }

    public void setLastActiveNode(int lastSelected)
    {
        this.lastActiveNode = lastSelected;
    }

    public int getLastActiveBend()
    {
        return lastActiveBend;
    }

    public void setLastActiveBend(int lastSelectedBend)
    {
        this.lastActiveBend = lastSelectedBend;
    }

    public float getZoomFactor()
    {
        return zoomFactor;
    }

    public void setZoomFactor(float zoomFactor)
    {
        this.zoomFactor = zoomFactor;
    }

    public int getStretchAlong()
    {
        return stretchAlong;
    }
    
    public void setStretchAlong(int val)
    {
        this.stretchAlong = val;
    }

    public float getGrid()
    {
        return grid;
    }

    public void setGrid(float grid)
    {
        this.grid = grid;
    }

    public float getX0() { return x0; }

    public float getY0() { return y0; }
    
    public void setP0(float x0, float y0)
    {
        if (x0 < xmin-1f)
            return;
        if (y0 < ymin-1f)
            return;
        if (x0 > xmax+1f)
            return;
        if (y0 > ymax+1f)
            return;
        this.x0 = x0;
        this.y0 = y0;
    }
    
    public PathPanel()
    {
        paths = new ArrayList<Path>();
        this.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                init();
                repaint();
            }
        });
    }
    
    public void init()
    {
        if (canvas == null)
            canvas = new EditCanvas(
                    new Background(1f,1f,1f,1f),
                    new GlobalLighting()
                    {
                public void lightObject(DrawingContext point)
                {
                }
            });
        canvas.init(this.getWidth(), this.getHeight());
    }

    
    @Override
    public void paint(Graphics g)
    {
//        super.paint(g);
        if (getPath() != null)
        try {
            canvas.setBackground(Color.WHITE);
            // find (x,y) boundaries for path
            xmin = Float.MAX_VALUE;
            ymin = Float.MAX_VALUE;
            xmax = Float.MIN_VALUE;
            ymax = Float.MIN_VALUE;
            for (int p0 = 0; p0 < getPath().getShapes().size(); p0++)
            {
                Iterable<Matrix4> shapes = getPath().getShapes().get(p0);
                Iterable<Matrix4[]> cps = getPath().getControlPoints().get(p0);
                for (Matrix4 m : shapes)
                {
                    updateMinMax(m);
                    Iterator<Matrix4[]> cpsi = cps.iterator();
                    for(int j = 0; j < 2; j++)
                    {
                        Matrix4[] bends = cpsi.next();
                        if (bends != null)
                        {
                            Matrix4 n = bends[j];
                            updateMinMax(n);
                        }
                        
                    }
                }
            }
            switch(stretchAlong)
            {
                case 0:
                    dmax = zoomFactor;
                    break;
                case 1:
                    dmax = (xmax-xmin) * zoomFactor;
                    break;
                case 2:
                    dmax = (ymax-ymin) * zoomFactor;
            }

                //(float)Math.max(xmax-xmin, ymax-ymin) * marginSize;
            // set camera so that the whole path shows
            Transform pos = new Transform(0,0);
            pos.setShapeTransform(Language.x, mexplist(centerX = (xmax+xmin)/2f+x0));
            pos.setShapeTransform(Language.y, mexplist(centerY = (ymax+ymin)/2f+y0));
            pos.setShapeTransform(Language.z, mexplist(-dmax));
            camera.setPosition(pos);
            camera.lookat(new Vector3(centerX,-centerY,0));
            // draw grid
            DrawingContext point = new DrawingContext();

            point.R = 1f;
            point.shape = (Untransformable)Language.SQUARE;
            float mult = (1f / grid);
            float tmps = (float)Math.floor((xmin - 1f)*mult)*grid;
            float start = 0f;
            if (xmin < 0)
                while (start > tmps)
                    start -= grid;
            else
                while (start < tmps)
                    start += grid;
            for (float ff = start; ff < xmax + 1.1f; ff += grid)
            {
                point.matrix = 
                        Matrix4.translation(ff, 0f, 0f).
                        multiply(Matrix4.scale(0.0015f, 100f, 1f))
                        ;
                canvas.drawPolygon(camera, point.toOutputShape());
            }
            tmps = (float)Math.floor((ymin - 1f)*mult)*grid;
            start = 0f;
            if (ymin < 0)
                while (start > tmps)
                    start -= grid;
            else
                while (start < tmps)
                    start += grid;
            for (float ff = start; ff < ymax + 1.1f; ff += grid)
            {
                point.matrix = 
                        Matrix4.translation(0f, ff, 0f).
                        multiply(Matrix4.scale(100f, 0.0015f, 1f))
                        ;

                canvas.drawPolygon(camera, point.toOutputShape());
            }
            // draw path
            point.shape = getPath();
            Matrix4 pos0 =
            point.matrix = Matrix4.IDENTITY;
            canvas.drawCurve(camera, point.toOutputShape());

            // draw nodes
            for (int p0 = 0; p0 < getPath().getShapes().size(); p0++)
            {
                Iterable<Matrix4> shapes = getPath().getShapes().get(p0);
                Iterator<Matrix4[]> cps = getPath().getControlPoints().get(p0).iterator();
                int i = 0;
                for(Matrix4 m : shapes)
                {
                    Matrix4 tmp = point.matrix = pos0.multiply(m);
                    point.matrix = point.matrix.multiply(Matrix4.scale(0.015f*zoomFactor));
                    point.shape = (Untransformable)Language.SQUARE;
                    if (i == lastActiveNode)
                    {
                        point.R = point.G = point.B = 0.3f;
                    }
                    else
                    {
                        point.R = point.G = point.B = 0f;
                    }
                    canvas.drawPolygon(camera, point.toOutputShape());
                    Matrix4[] cpsi = cps.next();
                    if (cpsi != null)
                    for(int j = 0; j < 2; j++)
                    {
                        point.matrix = pos0.multiply(cpsi[j]);
                        point.matrix = point.matrix.multiply(Matrix4.scale(0.015f*zoomFactor));
                        point.shape = (Untransformable)Language.CIRCLE;
                        point.R = point.G = point.B = 0f;
                        canvas.drawCurve(camera, point.toOutputShape());
                        point.matrix = point.matrix.multiply(Matrix4.scale(0.5f));
                        if (i == lastActiveNode)
                        {
                            point.R = point.G = point.B = 1f;
                        }
                        else
                        {
                            point.R = point.G = point.B = 0.5f;
                        }
                        canvas.drawPolygon(camera, point.toOutputShape());
                    }
                    i++;
                }
            }
            while (!g.drawImage(canvas.getImage(), 0, 0, null));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    Vector3 snapToGrid(Vector3 v)
    {
        Vector3 ret = new Vector3(
                (float)((Math.round(v.x / grid)) * grid ),
                (float)((Math.round(v.y / grid)) * grid ),
                (float)((Math.round(v.z / grid)) * grid )
                );
        return ret;
    }

    Vector3 toModelCoords(int x, int y)
    {
        float mult = 0f;
        float factorx = 1f;
        float factory = 1f;
        switch(stretchAlong)
        {
            case 0:
                dmax = zoomFactor;
                float divisor = (float)Math.min(getWidth(), getHeight());
                mult = dmax / divisor;
                if (getWidth() > getHeight())
                {
                    factorx = (float)getWidth() / (float)getHeight();
                } else if (getHeight() > getWidth())
                {
                    factory = (float)getHeight() / (float)getWidth();
                }
                break;
            case 1:
                dmax = (xmax-xmin) * (zoomFactor);
                mult = dmax / (float)getWidth();
                factory = (float)getHeight() / (float)getWidth();
                break;
            case 2:
                dmax = (ymax-ymin) * (zoomFactor);
                mult = dmax / (float)getHeight();
                factorx = (float)getWidth() / (float)getHeight();
                break;

        }
        float dx1 = (float)x * mult - dmax/2f*factorx + centerX;
        float dy1 = - ((float)y * mult - dmax/2f*factory - centerY);
        Vector3 v = new Vector3( dx1, dy1, 0f);
        return v;
    }


    public int[] getPivot(MouseEvent e, float fuzzy, int types)
    {
        float fuzzyMark = Float.MAX_VALUE;
        fuzzy *= dmax;
        int it = 0;
        int[] ret = new int[] { -1, -1, 0 };
        for(int subpath = 0; subpath < getPath().getShapes().size(); subpath++)
        {
            Iterable<Matrix4> nodes = getPath().getShapes().get(subpath);
            Iterator<Matrix4[]> bends = getPath().getControlPoints().get(subpath).iterator();
            int i = 0;
            for(Matrix4 m : nodes)
            {
                try {
                    float tmpf = fuzzyMark;
                    Matrix4[] bendsi = bends.next();
                    if ((types & 2) != 0)
                    {
                        for(int j = 0; j < 2; j++)
                        {
                            int[] rsp = new int[] { -1, j, subpath };
                            fuzzyMark = checkPivot(i, bendsi[j], e, fuzzy, fuzzyMark, rsp);
                            if (rsp[0] != -1 && fuzzyMark < tmpf)
                            {
                                ret = rsp;
                            }
                        }
                    }
                    if ((types & 1) != 0)
                    {
                        int[] rsp = new int[] { -1, -1, subpath };
                        fuzzyMark = checkPivot(i, m, e, fuzzy, fuzzyMark*2f, rsp);
                        if (rsp[0] != -1 && fuzzyMark < tmpf)
                        {
                            ret = rsp;
                        }
                    }
                }
                catch (Exception ex)
                {
                }
                i++;
            }
        }
        return ret;
    }

    private void updateMinMax(Matrix4 n)
    {
        if (xmin > n.m03)
            xmin = n.m03;
        if (xmax < n.m03)
            xmax = n.m03;
        if (ymin > n.m13)
            ymin = n.m13;
        if (ymax < n.m13)
            ymax = n.m13;
    }

    private float checkPivot(int it, Matrix4 get, MouseEvent e, float fuzzy, float fuzzyMark, int[] rsp)
    {
        float xsp = get.m03;
        Vector3 vec = toModelCoords(e.getX(), e.getY());
        float xpnl = vec.x;
        float nearness0 = Math.abs(xpnl - xsp);
        if (nearness0 <= fuzzy &&
                (nearness0 < fuzzyMark || lastActiveNode == it))
                {
            float ypnl = vec.y;
            float ysp = get.m13;
            float nearness = Math.abs(ypnl - ysp);
            if (nearness <= fuzzy &&
                    (nearness < fuzzyMark || lastActiveNode == it))
                    {
                fuzzyMark = nearness0;
                rsp[0] = it;
            }
        }
        return fuzzyMark;
    }

    private ArrayList<Expression> mexplist(float f)
    {
        ArrayList<Expression> list = new ArrayList<Expression>();
        list.add(new Value(f));
        return list;
    }

    public class EditCanvas extends DefaultCanvas {
        public EditCanvas(Background bg, GlobalLighting lighting)
        {
            super(null, bg, lighting);
        }
        @Override
        public int getPixelSizeFactor()
        {
            switch(stretchAlong)
            {
                case 0: return Math.min(super.getWidth(), super.getHeight());
                case 1: return super.getWidth();
                case 2: return super.getHeight();
            }
            return getWidth();
        }

    }

}
