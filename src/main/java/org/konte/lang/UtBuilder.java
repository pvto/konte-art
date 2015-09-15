
package org.konte.lang;

import org.konte.model.Untransformable;
import java.util.ArrayList;
import java.util.Arrays;
import org.konte.misc.Matrix4;
import org.konte.misc.Vector3;
import org.konte.model.Untransformable.EffectApply;

/**Helper class to build output shape prototypes.
 *
 * @author pvto
 */
public class UtBuilder implements
        java.util.Comparator<Untransformable> {

    private String name;
    private ArrayList<ArrayList<Matrix4>> shapes;
    private ArrayList<ArrayList<Matrix4[]>> controlPoints;
    private EffectApply effect;

    private final Matrix4 HALF = Matrix4.translation(0.5f, 0f, 0f);
    private final Vector3 v3d = new Vector3();
    
    public Untransformable build() throws Exception 
    {
        Untransformable u;
        if (effect != null)
        {
            u = new Untransformable.Effect(name, nextId--, shapes, controlPoints, effect);
        }
        else if (name.equals("SPHERE"))
        {
            u = new Untransformable.Sphere(name, nextId--, shapes, controlPoints);
        }
        else if (name.equals("MESH") )
        {
            u = new Untransformable.Dummy(name);
        }
        else if (controlPoints != null) 
        {
            u = new Untransformable.Curve(name, nextId--, shapes, controlPoints);
        }
        else
        {
            u = new Untransformable.Polygon(name, nextId--, shapes, controlPoints);
        }
        instances.add(u);
        return u;
    }

    @Override
    public int compare(Untransformable o1, Untransformable o2)
    {
        return o1.getId() - o2.getId();
    }

    private UtBuilder() {}
    
    private static UtBuilder instance = null;

    public static UtBuilder getUtBuilder()
    {
        if (instance == null)
        {
            instance = new UtBuilder();
        }
        else
        {
            instance.clearShapes();
        }
        return instance;
    }

    public UtBuilder name(String name)
    {
        this.name = name;
        return this;
    }

    public UtBuilder effect(Untransformable.EffectApply effectApply) {
        this.effect = effectApply;
        return this;
    }
    
    public UtBuilder addShape(Matrix4... points)
    {
        if (shapes == null)
        {
            shapes = new ArrayList<>();
        }
        ArrayList<Matrix4> shape = new ArrayList<>();
        shape.addAll(Arrays.asList(points));
        if (shape.size() > 0)
        {
            shapes.add(shape);
        }
        return this;
    }
    private static final Matrix4[] STRGHT = new Matrix4[]{Matrix4.ZERO, Matrix4.ZERO};

    public UtBuilder addControlPoints(Matrix4[][] points)
    {
        if (controlPoints == null)
        {
            controlPoints = new ArrayList<>();
        }
        ArrayList<Matrix4[]> cps = new ArrayList<>();
        // add points in supplied order, pad each pair to two if one or zero supplied points
        for (Matrix4[] mm : points)
        { // shapes
            Matrix4[] cp2 = Arrays.copyOf(mm, 2);
            if (mm.length == 1)
            {
                cp2[1] = cp2[0];
            }
            else if (mm.length == 0) 
            {
                cp2[0] = cp2[1] = Matrix4.ZERO;
            }
            cps.add(cp2);
        }
        // if more shapes than control points, add empty control points to the tail
        for (int i = cps.size(); i < shapes.get(controlPoints.size()).size(); i++)
        {
            cps.add(STRGHT);
        }
        // if no control points were supplied for earlier subshape, append empty ones
        for (int i = 0; i < shapes.get(controlPoints.size()).size() - cps.size(); i++)
        {
            ArrayList<Matrix4[]> cps2 = new ArrayList<>();
            controlPoints.add(cps2);
            for (Matrix4 m : shapes.get(controlPoints.size() - 1))
            {
                cps2.add(STRGHT);
            }
        }
        // append the newly constructed control points
        controlPoints.add(cps);
        return this;
    }

    public UtBuilder clearShapes()
    {
        this.shapes = null;
        this.controlPoints = null;
        this.effect = null;
        return this;
    }
    private static final ArrayList<Untransformable> instances = new ArrayList<>();
    private static int nextId = -1;

    public static ArrayList<Untransformable> getUntransformables()
    {
        return instances;
    }


}
