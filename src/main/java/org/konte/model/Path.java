
package org.konte.model;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import org.konte.image.Camera;
import org.konte.image.Canvas;
import org.konte.image.OutputShape;
import org.konte.lang.Language;
import org.konte.misc.Matrix4;


public class Path extends Untransformable implements Serializable {

    public static int id_gen = 0x1000;
    public int closed;


    @Override
    public void draw(Camera camera, Canvas canvas, OutputShape shape)
    {
        canvas.drawCurve(camera, shape);
    }

    
    public Path()
    {
        this.id = id_gen;
        this.name = "Path" + id_gen++;
        this.shapes = new ArrayList<ArrayList<Matrix4>>();
        this.controlPoints = new ArrayList<ArrayList<Matrix4[]>>();
        this.isCurved = true;
    }


    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException 
           {
        out.writeInt(closed);
        if (closed == 2)
        {
            out.writeInt(id);
        }
        else
        {
            out.writeObject(shapes);
            out.writeObject(controlPoints);
        }
    }

    @SuppressWarnings(
        value = {"unchecked"}
    )
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException 
           {
        closed = in.readInt();
        if (closed == 2)
        {
            id = in.readInt();
            Untransformable u = Language.getUntransformable(id);
            if (u == null)
            {
                return;
            }
            isCurved = u.isCurved;
            shapes = u.shapes;
            controlPoints = u.controlPoints;
        }
        else
        {
            isCurved = true;
            shapes = (ArrayList<ArrayList<Matrix4>>)in.readObject();
            controlPoints = (ArrayList<ArrayList<Matrix4[]>>)in.readObject();
        }
    }

    private void readObjectNoData() throws ObjectStreamException 
    {
    }

}
