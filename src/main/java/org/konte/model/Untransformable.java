/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.konte.model;


import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import org.konte.image.Camera;
import org.konte.image.Canvas;
import org.konte.image.OutputShape;
import org.konte.lang.Language;
import org.konte.lang.Tokens.Token;
import org.konte.misc.Matrix4;

/**<p>A shape (Untransformable) that can be drawn.
 * 
 * @author pto
 */
public abstract class Untransformable extends Token implements Serializable {

    protected int id;
    public boolean isCurved = false;
    protected List<? extends List<Matrix4>> shapes;
    protected List<? extends List<Matrix4[]>> controlPoints;


    
    public abstract void draw(Camera camera, Canvas canvas, OutputShape shape);



    public Untransformable() {
    }

    public Untransformable(String name, int id) {
        this(name, id, null, null);

    }

    public Untransformable(String name, int id, List<? extends List<Matrix4>> shapes,
            List<? extends List<Matrix4[]>> controlPoints) {
        super(name);
        this.id = id;
        this.shapes = shapes;
        this.controlPoints = controlPoints;
        this.isCurved = controlPoints == null ? false : true;
    }





    public int getId() {        
        return id;                  }
    public List<? extends List<Matrix4>> getShapes() {
        return shapes;              }
    public void setShapes(List<? extends List<Matrix4>> shapes) {
        this.shapes = shapes;       }
    public List<? extends List<Matrix4[]>> getControlPoints() {
        return controlPoints;       }
    public void setControlPoints(List<? extends List<Matrix4[]>> cpl) {
        this.controlPoints = cpl;   }
    
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeInt(id);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        id = in.readInt();
        Untransformable u = Language.getUntransformable(id);
        if (u == null) {
            return;
        }
        isCurved = u.isCurved;
        shapes = u.shapes;
        controlPoints = u.controlPoints;
    }

    private void readObjectNoData() throws ObjectStreamException {
    }



    
    public static class Polygon extends Untransformable implements Serializable {

        public Polygon(String name, int id, List<? extends List<Matrix4>> shapes,
                List<? extends List<Matrix4[]>> controlPoints) {
            super(name, id, shapes, controlPoints);
        }
        @Override
        public void draw(Camera camera, Canvas canvas, OutputShape shape) {
            canvas.drawPolygon(camera, shape);
        }
    }


    public static class Sphere extends Untransformable implements Serializable {

        public Sphere(String name, int id, List<? extends List<Matrix4>> shapes,
                List<? extends List<Matrix4[]>> controlPoints) {
            super(name, id, shapes, controlPoints);
        }
        @Override
        public void draw(Camera camera, Canvas canvas, OutputShape shape) {
            canvas.drawSphere(camera, shape);
        }
    }


    public static class Curve extends Untransformable implements Serializable {

        public Curve(String name, int id, List<? extends List<Matrix4>> shapes,
                List<? extends List<Matrix4[]>> controlPoints) {
            super(name, id, shapes, controlPoints);
        }
        @Override
        public void draw(Camera camera, Canvas canvas, OutputShape shape) {
            canvas.drawCurve(camera, shape);
        }
    }


    public static class Dummy extends Untransformable implements Serializable {
        
        public Dummy(String name) {
            this.name = name;
            id = Integer.MIN_VALUE;
        }
        @Override
        public void draw(Camera camera, Canvas canvas, OutputShape shape) {
        }
    }    
}
