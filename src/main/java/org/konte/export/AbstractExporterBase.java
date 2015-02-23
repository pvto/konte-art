
package org.konte.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.konte.generate.ShapeReader;
import org.konte.image.Camera;
import org.konte.image.OutputShape;
import org.konte.lang.Language;
import org.konte.model.Untransformable;
import org.konte.model.Background;
import org.konte.model.Model;


public abstract class AbstractExporterBase {

    protected ShapeReader bsr;
    protected Model m;


    private void setModel(Model m)
    {
        this.m = m;
    }

    private void setShapeReader(ShapeReader bsr)
    {
        this.bsr = bsr;
    }

    public enum Type {

        SUNFLOW("sc"),
        SVG("svg");
        
        
        String ext;

        private Type(String ext)
        {
            this.ext = ext;
        }
        public String getFileNameExtension()
        {
            return ext;
        }
        
    }

    public static AbstractExporterBase createExporter(ShapeReader bsr, Model m, Type t)
    {
        AbstractExporterBase e;
        switch (t)
        {
            case SUNFLOW:
                e = new SunflowExporter();
                break;
            case SVG:
            default:
                e = new SvgExporter();
                break;

        }
        e.setModel(m);
        e.setShapeReader(bsr);
        return e;
    }

    public void export(File file) throws IOException {
        init();
        bsr.rewind();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        bw.write(transform(m, bsr));
        bw.write(transform(m.cameras));
        bw.write(transform(m.bg));
        for (Untransformable shape : Language.untransformables())
        {
            bw.write(transform(shape));
        }


        Iterator<OutputShape> ii = getIterator(bsr);
        OutputShape p = null;
        while ((p = ii.next()) != null)
        {
            bw.write(transform(p, m));
        }
        bw.write(finish(m,bsr));
        bw.close();
    }

    protected abstract void init();

    protected abstract Iterator<OutputShape> getIterator(ShapeReader bsr);

    protected abstract String transform(Model m, ShapeReader bsr );
    
    protected abstract String transform(OutputShape p, Model m);

    protected abstract String transform(Background bg);

    protected abstract String transform(List<Camera> cameras);

    protected abstract String transform(Untransformable shape);

    protected abstract String finish(Model m, ShapeReader bsr);
}
