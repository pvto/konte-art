
package org.konte.model;

import java.util.ArrayList;
import java.util.Arrays;
import org.konte.expression.Expression;
import org.konte.expression.Value;
import org.konte.misc.Matrix4;
import org.konte.parse.ParseException;

/**
 *
 * @author pvto
 */
public class PathRule extends Rule {
    public ArrayList<Placeholder> steps = new ArrayList<Placeholder>();
    public int closed = 0;

    public static final int MOVE_TO = 1;
    public static final int BEND = 2;
    public static final int CURVE_TO = 3;
    public static final int LINE_TO = 4;
    public static final int CLOSE = 10;

    // flags:
    public static final int REMOVE_CLOSING = 1;
    
    public static class Placeholder {
        public int type;
        public Expression[] data;
        public Placeholder(int type, Expression[] data) {
            this.type = type;
            this.data = data;
        }
        public String toString() { return type + ":" + Arrays.toString(data); }
    }

    
    public Matrix4 translation(Expression[] trns) throws ParseException {
        return new Matrix4(
                1f,0f,0f,trns[0].evaluate(),
                0f,1f,0f,trns[1].evaluate(),
                0f,0f,1f,trns[2].evaluate(),
                0f,0f,0f,1f
                );
    }


    public void checkBezierControls() {
   /*     int lastType = -1;
        if (steps.size() > 0) {
            lastType = steps.get(steps.size()-1).type;
        }
        while(steps.get(steps.size() - 1).type != CLOSE &&
                (steps.size() < 2 ||
                (steps.get(steps.size() - 1).type != PathRule.BEND
                || steps.get(steps.size() - 2).type != PathRule.BEND))) {
            Placeholder ph = new Placeholder(PathRule.BEND,
                    new Expression[] { new Value(0f), new Value(0f), new Value(0f)} );
            if (lastType == PathRule.BEND)
                ph = steps.get(steps.size()-1);
            steps.add(ph);
        }*/
    }

    public Untransformable createUntransformable() throws ParseException {
        return createUntransformable(0);
    }
    public Untransformable createUntransformable(int flags) throws ParseException {
        checkBezierControls();
        Path p = new Path();
        ArrayList<ArrayList<Matrix4>> sl = new ArrayList<ArrayList<Matrix4>>();
        ArrayList<ArrayList<Matrix4[]>> cpl = new ArrayList<ArrayList<Matrix4[]>>();
        ArrayList<Matrix4> points = new ArrayList<Matrix4>();
        ArrayList<Matrix4[]> cpoints = new ArrayList<Matrix4[]>();
        Matrix4[] lastCp = null;
        for(int i = 0; i < steps.size(); i++) {
            Placeholder ph = steps.get(i);
            if (ph.type == PathRule.BEND) {
                cpoints.add(lastCp = new Matrix4[] {
                   translation(ph.data),
                   translation(steps.get(++i).data) 
                });
            } else if (ph.type == PathRule.CLOSE){
                if (i < steps.size() -1) {
                    if ((flags & REMOVE_CLOSING) != 0) {
                        if (cpoints.size() > 0 && cpoints.get(cpoints.size()-1) == null) {
                            while (points.size() >= cpoints.size())
                                cpoints.add(null);
                        } else
                        while (points.size() > cpoints.size())
                            points.remove(points.size()-1);
                        
                    }
                    sl.add(points);
                    cpl.add(cpoints);
                    points = new ArrayList<Matrix4>();
                    cpoints = new ArrayList<Matrix4[]>();
                }
                p.closed = 1;
            } else if (ph.type == PathRule.LINE_TO){
                points.add(translation(ph.data));
                cpoints.add(null);
            } else if (ph.type == PathRule.MOVE_TO){
                if (i > 0 && steps.get(i - 1).type == PathRule.MOVE_TO) {   // two relative movetos are consumed in the last
                    points.set(points.size() - 1, translation(ph.data));
                } else {
                    points.add(translation(ph.data));
                }
            } else {
                points.add(translation(ph.data));
            }

        }
        while (points.size() >= cpoints.size())
            cpoints.add(null);
        sl.add(points);
        p.setShapes(sl);
        cpl.add(cpoints);
        p.setControlPoints(cpl);
        p.closed = this.closed;
        return p;
    }

    /**Builds a filled quadratic bezier path from a list of coordinate triplets.
     * Each triplet consists of three coordinate. Each coordinate consists of
     * [x,y,z] coordinates.
     *   Last coordinate is closed back to the first one.
     * 
     * [curve start coordinate],
     * [bend coordinate for start coordinate],
     * [bend coordinate for curve point 1],
     * [curve point 1 coordinate],
     * [bend coordinate for curve point 1],
     * [bend coordinate for curve point 2],
     * [curve point 2 coordinate],
     * . . .
     * [curve point n coordinate],
     * [bend coordinate for curve point n],
     * [bend coordinate for start coordinate],
     *
     * @param data
     * @return
     * @throws org.konte.parse.ParseException
     */
    public static Untransformable createUntransformable(float[] data) throws ParseException {
        PathRule pr = new PathRule();
        int i = 0;
        pr.steps.add(new Placeholder(PathRule.MOVE_TO, new Expression[]
        { new Value(data[i++]), new Value(data[i++]), new Value(data[i++]) }));
        while(i < data.length) {
            pr.steps.add(new Placeholder(PathRule.BEND, new Expression[]
            { new Value(data[i++]), new Value(data[i++]), new Value(data[i++]) }));
            pr.steps.add(new Placeholder(PathRule.BEND, new Expression[]
            { new Value(data[i++]), new Value(data[i++]), new Value(data[i++]) }));
            if ( i < data.length) {
                pr.steps.add(new Placeholder(PathRule.CURVE_TO, new Expression[]
                { new Value(data[i++]), new Value(data[i++]), new Value(data[i++]) }));
            }

        }
        return pr.createUntransformable();
    }

}
