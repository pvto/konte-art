
package org.konte.misc;

import java.util.List;
import java.util.Locale;
import org.konte.model.Path;

/**
 *
 * @author pvto
 */
public class ReverseParseTools {

    public static String pathToScript(Path path) {
        StringBuilder bd = new StringBuilder();
        String name = "mypath";
        if (path.name != null && path.name.length() > 0)
            name = path.name;
        bd.append(String.format("path %s {\n", name));
        for(int p0 = 0; p0 < path.getShapes().size(); p0++) {
            Matrix4[] bends0 = null;
            List<Matrix4> paths = path.getShapes().get(p0);
            List<Matrix4[]> bends = path.getControlPoints().get(p0);
            for(int i = 0; i < paths.size(); i++) {
                Matrix4 m = paths.get(i);
                bd.append(String.format(Locale.ENGLISH,
                        "    %s(%.3f, %.3f, %.3f)\n",
                        (i == 0 ? "moveto" : 
                            (bends0 == null ? "lineto" : "curveto")),
                        m.m03, m.m13, m.m23
                        ));
                bends0 = bends.get(i % bends.size());
                if (bends0 != null)
                    for(int j = 0; j < 2; j++) {
                        m = bends0[j];
                        bd.append(String.format(Locale.ENGLISH,
                            "    %s(%.3f, %.3f, %.3f)\n",
                            "bend",
                            m.m03, m.m13, m.m23
                            ));
                    }
            }
//            if (paths.size() > 0)
                bd.append("    close\n");
        }
        bd.append("}\n");
        return bd.toString();
    }
}
