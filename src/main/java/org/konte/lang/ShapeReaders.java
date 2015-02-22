package org.konte.lang;

import org.konte.generate.DiskBackedShapeReader;
import org.konte.generate.ShapeReader;
import org.konte.generate.StreamingShapeReader;
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
                    case WIDTH: return new DiskBackedShapeReader(m, new DiskBackedShapeReader.MinWidthMetric(m));
                    case SMALLNESS: return new DiskBackedShapeReader(m, new DiskBackedShapeReader.MaxWidthMetric(m));
                    case STREAM: return new StreamingShapeReader(m);
                    case DB: return new DiskBackedShapeReader(m, new DiskBackedShapeReader.ZMetric(m));
                    case Z:
                    default:
                        return new ZOrderShapeReader(m);
                }
            }
        }
        return null;
    }
}