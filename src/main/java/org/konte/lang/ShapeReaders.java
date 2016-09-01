package org.konte.lang;

import org.konte.generate.DiskBackedShapeReader;
import org.konte.generate.DiskedFlointShapeReader;
import org.konte.generate.PointMetric;
import org.konte.generate.ShapeReader;
import org.konte.generate.StreamingShapeReader;
import org.konte.generate.ZOrderShapeReader;
import org.konte.model.Model;

/**
 * @author pvto https://github.com/pvto
 */
public enum ShapeReaders {

    DEFAULT,
    MEM,
    DISK,
    WIDTH,
    SMALLNESS,
    STREAM,
    ;

    public static ShapeReader getReader(String name, Model m)
    {
        for (ShapeReaders r : ShapeReaders.values())
        {
            if (r.toString().equalsIgnoreCase(name))
            {
                switch(r)
                {
                    case WIDTH: return new DiskedFlointShapeReader(m, new PointMetric.MinWidthMetric(m));
                    case SMALLNESS: return new DiskedFlointShapeReader(m, new PointMetric.MaxWidthMetric(m));
                    case STREAM: return new StreamingShapeReader(m);
                    case MEM: return new ZOrderShapeReader(m);
                    case DISK: return new DiskBackedShapeReader(m, new PointMetric.ZMetric(m));
                }
            }
        }
        return new DiskedFlointShapeReader(m, new PointMetric.ZMetric(m));
    }
}