
package org.konte.export;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.konte.generate.ShapeReader;
import org.konte.image.Camera;
import org.konte.image.OutputShape;
import org.konte.misc.Matrix4;
import org.konte.misc.Point2;
import org.konte.misc.Vector3;
import org.konte.model.Background;
import org.konte.model.Model;
import org.konte.model.Untransformable;

public class SvgExporter extends AbstractExporterBase {
    private float w,h,whmax;
    
    @Override
    protected void init()
    {
    }

    @Override
    protected String transform(Model m, ShapeReader bsr)
    {
        String s = String.format(
"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<!-- Created with konte (http://sourceforge.net/projects/konte/) -->\n" +
"<svg\n" +
"   xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
"   xmlns=\"http://www.w3.org/2000/svg\"\n" +
"   version=\"1.0\"\n" +
"   width=\"%spx\"\n" +
"   height=\"%spx\"\n" +
"   id=\"svg1901\">\n" +
"  <defs\n" +
"     id=\"defs1902\" />\n",
                (w = bsr.getCanvas().getWidth()),
                (h = bsr.getCanvas().getHeight()));
        whmax = Math.max(w, h);
        return s;
    }

    private float curLayer = Float.MIN_VALUE;
    private int pathCounter = 2380;
    @Override
    protected String transform(OutputShape p, Model m)
    {
        StringBuilder bd = new StringBuilder();
        int app = 0;
        if (curLayer == Float.MIN_VALUE)
        {
            app = 1;
        }
        else if (p.layer != curLayer)
        {
            bd.append(
"   </g>\n");
            app = 1;
        }
        curLayer = p.layer;
        if (app == 1)
        {
            bd.append(String.format(Locale.ENGLISH,
"  <g\n     transform=\"matrix(%.4f,%.4f,%.4f,%.4f,%.4f,%.4f)\"\n     id=\"layer%.3f\"\n     style=\"opacity:1\">\n",
                whmax,0f,0f,whmax,w/2f,h/2f,p.layer));
//).append(String.format("%.3f",).replaceAll("[\\.,]", "-"))
        }
        Untransformable unt = p.shape;
        if (unt.getShapes() != null)
        {
            bd.append(String.format(Locale.ENGLISH, 
"   <g\n     id=\"g%d\"\n     style=\"opacity:%.5f\">\n",
                    pathCounter, p.getA()));
            bd.append(
"   <path\n      d=\"");
            Camera cam = m.cameras.get(p.fov);
            for (int i = 0; i < unt.getShapes().size(); i++)
            {
                List<Matrix4> pts = unt.getShapes().get(i);
                List<Matrix4[]> cpts = unt.getControlPoints() == null ? null :
                    unt.getControlPoints().get(i);
                appMoveTo(bd, p.matrix.multiply(pts.get(0)), cam);
                for(int j = 1; j <= pts.size(); j++)
                {
                    Matrix4 pt = pts.get(j%pts.size());
                    Matrix4[] cpt = cpts == null ? null : cpts.get(j-1);
                    if (cpt == null)
                    {
                        appLineTo(bd, p.matrix.multiply(pt), cam);
                    } else {
                        appCurveTo(bd, p.matrix.multiply(cpt[0]),
                                p.matrix.multiply(cpt[1]),
                                p.matrix.multiply(pt), cam);
                    }
                }
                if (!unt.isCurved)
                {
                    bd.append("z\"\n");
                    appId(bd, p);
                    if (i < unt.getShapes().size() - 1)
                        bd.append(
"   <path\n      d=\"");
                }
            }
            if (unt.isCurved)
            {
                bd.append("z\"\n");
                appId(bd, p);
            }
            bd.append("   </g>\n");
        } 
        return bd.toString();
    }

    private void appMoveTo(StringBuilder bd, Matrix4 get, Camera cam)
    {
        Point2 p = cam.mapTo2D(new Vector3(get.m03, get.m13, get.m23));
        bd.append(String.format(Locale.ENGLISH, "M %.5f,%.5f ",
                mpx(p.x), mpy(p.y)));
    }
    private void appLineTo(StringBuilder bd, Matrix4 get, Camera cam)
    {
        Point2 p = cam.mapTo2D(new Vector3(get.m03, get.m13, get.m23));
        bd.append(String.format(Locale.ENGLISH, "L %.5f,%.5f ",
                mpx(p.x), mpy(p.y)));
    }
    private void appCurveTo(StringBuilder bd, Matrix4 cpt0, Matrix4 cpt1, Matrix4 get, Camera cam)
    {
        Point2 p = cam.mapTo2D(new Vector3(get.m03, get.m13, get.m23));
        Point2 c0 = cam.mapTo2D(new Vector3(cpt0.m03, cpt0.m13, cpt0.m23));
        Point2 c1 = cam.mapTo2D(new Vector3(cpt1.m03, cpt1.m13, cpt1.m23));
        bd.append(String.format(Locale.ENGLISH, "C %.5f,%.5f %.5f,%.5f %.5f,%.5f ",
                mpx(c0.x), mpy(c0.y),
                mpx(c1.x), mpy(c1.y),
                mpx(p.x), mpy(p.y)));
    }
    private void appId(StringBuilder bd, OutputShape p)
    {
        int col = p.col & 0xFFFFFF;
        bd.append(String.format(
"      style=\"fill:#%06x;fill-opacity:1;stroke:none\"\n", col));
        bd.append(
"      id=\"path").append(pathCounter).append("\" />\n");
        pathCounter++;

    }

    private float mpx(float x)
    {
        return x;
    }
    private float mpy(float y)
    {
        return -y;
    }
    @Override
    protected String transform(Background bg)
    {
        return "";
    }

    @Override
    protected String transform(List<Camera> cameras)
    {
        return "";
    }

    @Override
    protected String transform(Untransformable shape)
    {
        return "";
    }

    @Override
    protected String finish(Model m, ShapeReader bsr)
    {
        StringBuilder bd = new StringBuilder();
        if (curLayer != Float.MIN_VALUE)
        {
            bd.append(
"   </g>\n");
        }
        bd.append("</svg>\n");
        return bd.toString();
    }

    @Override
    protected Iterator<OutputShape> getIterator(ShapeReader bsr)
    {
        return bsr.descendingIterator();
    }




}
