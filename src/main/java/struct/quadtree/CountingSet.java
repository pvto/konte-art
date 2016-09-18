
package struct.quadtree;

import java.util.HashMap;

/**
 * @author pvto https://github.com/pvto
 */
public class CountingSet<K> extends HashMap<K, Double> {

    public Double increment(K key, double amount) {
        Double d = getCount(key);
        put(key, (d = d + amount));
        return d;
    }

    public double getCount(K key) {
        Double d = get(key);
        if (d == null) {
            return 0.0;
        }
        return d;
    }
}
