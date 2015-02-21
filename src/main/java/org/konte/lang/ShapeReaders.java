package org.konte.lang;

import org.konte.generate.MapDBShapeReader;
import org.konte.generate.ShapeReader;
import org.konte.generate.SmallnessOrderShapeReader;
import org.konte.generate.StreamingShapeReader;
import org.konte.generate.WidthOrderShapeReader;
import org.konte.generate.ZOrderShapeReader;
import org.konte.model.Model;

/**
 * @author pvto https://github.com/pvto
 */
public enum ShapeReaders {

    Z,
    WIDTH,
    SMALLNESS,
    STREAM,
    DB;

    public static ShapeReader getReader(String name, Model m)
    {
        for (ShapeReaders r : ShapeReaders.values())
        {
            if (r.toString().equalsIgnoreCase(name))
            {
                switch(r)
                {
                    case WIDTH: return new WidthOrderShapeReader(m);
                    case SMALLNESS: return new SmallnessOrderShapeReader(m);
                    case STREAM: return new StreamingShapeReader(m);
                    case DB: return new MapDBShapeReader(m, new MapDBShapeReader.ZMetric(m));
                    case Z:
                    default:
                        return new ZOrderShapeReader(m);
                }
            }
        }
        return null;
    }
}