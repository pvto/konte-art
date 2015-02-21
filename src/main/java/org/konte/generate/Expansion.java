package org.konte.generate;

import java.io.Serializable;
import org.konte.model.DrawingContext;

/**
 *
 * @author pto
 */
public class Expansion implements Comparable<Expansion>, Serializable {

    /** stores the received data (a point in konte space) */
    DrawingContext point;
    /** stores the received rule which will transform the received data */
    int ndruleIndex;

    public Expansion(DrawingContext point, int ndruleIndex) {
        this.point = point;
        this.ndruleIndex = ndruleIndex;
    }

    public int compareTo(Expansion o) {
        float diff = point.getMinWidth() - o.point.getMinWidth();
        if (diff < 0f) return -1;
        else if (diff > 0f) return 1;
        else return 0;
    }
    public String toString() {
        return String.format("[%s,%s]", ndruleIndex,point.d);
    }
}
