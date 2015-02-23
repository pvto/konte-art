
package org.konte.export;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.konte.generate.ShapeReader;
import org.konte.image.Camera;
import org.konte.image.OutputShape;
import org.konte.lang.Language;
import org.konte.model.Untransformable;
import org.konte.misc.Matrix4;
import org.konte.misc.Vector3;
import org.konte.model.Background;
import org.konte.model.Definition;
import org.konte.model.Model;


class SunflowExporter extends AbstractExporterBase {

    
    private int shapeCnt = 1;

    @Override
    protected String transform(OutputShape p, Model m)
    {
        StringBuilder bd = new StringBuilder();
        String shader = addShader(p, bd);
        if (p.shape == Language.SPHERE)
        {
            bd.append(String.format(
                    "object {\n  shader \"%s\"\n  type sphere\n  c %s %s %s\n  r %s\n}", 
                    shapeMap.get(p.shape.getId()),
                    p.matrix.m03, p.matrix.m13, p.matrix.m23,
                    p.getMinWidth()));
        } else if (!p.shape.isCurved)
        {
            bd.append("instance {\n");
            bd.append("\tname \"").append(p.shape.name + shapeCnt++).append("\"\n");
            bd.append("\tgeometry \"").append(shapeMap.get(p.shape.getId())).append("\"\n");
            bd.append(String.format(
                    "\ttransform row %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s\n",
                    p.matrix.m00, p.matrix.m01, p.matrix.m02, p.matrix.m03,
                    p.matrix.m10, p.matrix.m11, p.matrix.m12, p.matrix.m13,
                    p.matrix.m20, p.matrix.m21, p.matrix.m22, -p.matrix.m23,
                    p.matrix.m30, p.matrix.m31, p.matrix.m32, p.matrix.m33));
            bd.append("\tshader \"").append(shader).append("\"\n");
            bd.append("}\n\n");
        }

        return bd.toString();
    }
    private int shaderCount = 1;
    private HashMap<MapType, String> shaderMap = new HashMap<MapType, String>();

    @Override
    protected Iterator<OutputShape> getIterator(ShapeReader bsr)
    {
        return bsr.iterator();
    }


    private class MapType {
        Color col;
//        DrawingContext.Def[] defs;

        @Override
        public int hashCode()
        {
            return col.hashCode();
            //return (defs == null ? 0 : defs.hashCode()) + col.hashCode();
        }
        
    }
    MapType search = new MapType();

    private String addShader(OutputShape p, StringBuilder b)
    {
        Color c = p.getColor();
        search.col = c;
//        search.defs = p.defs;
        String name = shaderMap.get(search);
        if (name == null)
        {
            MapType unique = new MapType();
            unique.col = c;
//            unique.defs = p.defs;
            shaderMap.put(unique, name = ("shader" + shaderCount++));
            b.append("shader {\n");
            b.append("  name \"").append(name).append("\"\n");
            
            String type = null;
            String col = null;
            String extra = null;
            float val;
/*
            if (phongDefId > -1 && p.getDef(phongDefId) != 0f)
            {
                type = "phong";
            } else if (shinyDefId > -1 && p.getDef(shinyDefId) != 0f)
            {
                type = "shiny";
                col = "diff";
                extra = String.format("  refl %s\n", 
                        reflId > -1 && (val = p.getDef(reflId)) != 0f ? val : 1f);
            } else if (mirrorDefId > -1 && p.getDef(mirrorDefId) != 0f)
            {
                type = "mirror";
                col = "refl";
            } else if (ambOccDefId > -1 && p.getDef(ambOccDefId) != 0f)
            {
                type = "amb-occ";
                col = "bright";
                val = p.getDef(darkId);
                extra = String.format("  dark %s %s %s\n  samples %s\n  dist %s\n",
                           val / 10000f, val % 100f - val % 1f, val % 1f,
                           samplesId > -1 && (val = p.getDef(samplesId)) != 0f ? (int)val : 4,
                           distId > -1 && (val = p.getDef(distId)) != 0f ? (int)val : 4);
            } else {*/
                type = "diffuse";
                col = "diff";
//            }
            b.append(String.format("  type %s\n",type)); 
            b.append(String.format("  %s { \"sRGB nonlinear\" ", col));
            b.append(p.getR()).append(" ").append(p.getG()).append(" ").append(p.getB()).append(" }\n");
            if (extra != null)
                b.append(extra);
            b.append("}\n\n");
        }
        return name;
    }


    /*
    shader {
    name "shaderSphere4"
    %type amb-occ
    %bright   1 0.333333 0
    %dark     0 0 0
    %samples  16
    %dist     6
    type diffuse
    diff { "sRGB nonlinear" 1 0.333333 0 }
    }
     * */
    @Override
    protected String transform(Background bg)
    {
        StringBuilder bd = new StringBuilder();
        bd.append("background {\n\tcolor  { \"sRGB nonlinear\" ");
        bd.append(String.format("%s %s %s }\n}\n\n",
                bg.getColor().getRed() / 255f,
                bg.getColor().getGreen() / 255f,
                bg.getColor().getBlue() / 255f));
        return bd.toString();
    }
    /*
    camera {
    type pinhole
    eye    0 10 40
    target 0 0 0
    up     0 1 0
    fov    40
    aspect 1
    }
     */

    @Override
    protected String transform(List<Camera> cameras)
    {
        StringBuilder bd = new StringBuilder();
        Camera camera = cameras.get(0);
        bd.append("camera {\n");
        bd.append("  type pinhole\n");
        bd.append(String.format(
                "  eye %s %s %s\n",
                camera.getPosition().x,
                camera.getPosition().y,
                -camera.getPosition().z));
        bd.append(String.format(
                "  target %s %s %s\n",
                camera.getTarget().x,
                camera.getTarget().y,
                camera.getTarget().z));
        bd.append("  up 0 1 0\n");
        bd.append("  fov 40\n");
        bd.append("  aspect 1\n");
        bd.append("}\n\n");
        return bd.toString();
    }

    /*
    object {
    shader none
    transform col 0.001 0 0 0  0 0.001 0 0  0 0 0.001 0  0 0 0 1
    type generic-mesh
    name "Box"
    points 8
    1  1  1
    1  0  1
    0  0  1
    0  1  1
    0  1  0
    0  0  0
    1  0  0
    1  1  0
    triangles 12
    0 3 2
    0 2 1
    2 3 4
    2 4 5
    3 0 7
    3 7 4
    0 1 6
    0 6 7
    1 2 5
    1 5 6
    5 4 7
    5 7 6
    normals none
    uvs none
    }
     */
    private Vector3 v3d = new Vector3();
    private HashMap<Integer, String> shapeMap = new HashMap<Integer, String>();

    @Override
    protected String transform(Untransformable shape)
    {
        if (shape == Language.SPHERE)
        {
            return "";
        }
        if (shape.getShapes() == null)
        {
            return "";
        }
        shapeMap.put(shape.getId(), shape.name);

        Matrix4 orig = Matrix4.IDENTITY;
        //System.out.print(shape.shape.name);
        //Color c = new Color(shape.getR(),shape.getG(),shape.getB(),shape.getA());

        StringBuilder bd = new StringBuilder();
        bd.append("object {\n");
        bd.append("  shader none\n");
        bd.append("  transform col 0.001 0 0 0  0 0.001 0 0  0 0 0.001 0  0 0 0 1\n");
        int cnt = 0;
        ArrayList<Integer> centInd = new ArrayList<Integer>();

        for (List<Matrix4> l : shape.getShapes())
        {
            for (Matrix4 m : l)
            {
                cnt++;
            }
            centInd.add(cnt++);
        }
        if (!shape.isCurved)
        {
            bd.append("  type generic-mesh\n");
            bd.append("  name \"").append(shape.name).append("\"\n");
            bd.append("  points ");
            
            if (shape == Language.SQUARE)
            {
                bd.append("4\n");
                bd.append(String.format("\t%s\t%s\t%s\n",-0.5,-0.5,0));
                bd.append(String.format("\t%s\t%s\t%s\n",-0.5,0.5,0));
                bd.append(String.format("\t%s\t%s\t%s\n",0.5,0.5,0));
                bd.append(String.format("\t%s\t%s\t%s\n",0.5,-0.5,0));
                bd.append("  triangles 2\n");
                bd.append(String.format("\t%s\t%s\t%s\n",0,1,2));
                bd.append(String.format("\t%s\t%s\t%s\n",0,3,2));
            } else {
                bd.append(cnt).append("\n");
                for (List<Matrix4> l : shape.getShapes())
                {
                    float cntx = 0f;
                    float cnty = 0f;
                    float cntz = 0f;
                    Matrix4 tmp;
                    for (Matrix4 m : l)
                    {
                        tmp = orig.multiply(m);
                        cntx+=tmp.m03; cnty+=tmp.m13; cntz+=tmp.m23;
                        bd.append(String.format("\t%s\t%s\t%s\n",
                                tmp.m03,tmp.m13,tmp.m23));
                    }
                    bd.append(String.format("\t%s\t%s\t%s\n",cntx/l.size(),cnty/l.size(),cntz/l.size()));

                }

                bd.append("  triangles ").append(cnt - shape.getShapes().size()).append("\n");
                int cnt2 = 0;
                for (List<Matrix4> l : shape.getShapes())
                {
                    for (int i = 0; i < l.size() - 1; i++)
                    {
                        bd.append("\t").append(cnt2).append(" ").append(++cnt2).append(" ").append(centInd.get(0)).append("\n");
                    }
                    bd.append("\t").append(cnt2).append(" ").append(++cnt2 - (l.size())).append(" ").append(centInd.remove(0)).append("\n");
                    cnt2++;
                }
            }
        }

        bd.append("  normals none\n");
        bd.append("  uvs none\n");
        bd.append("}\n\n");
        
        if (shape.isCurved)
            return "% curved shape removed !! " + shape.name + "\n";
        return bd.toString();
    }

    @Override
    protected void init()
    {

    }

    @Override
    protected String finish(Model m, ShapeReader bsr)
    {
        return "";
    }
/*
%% common settings
image {
	resolution 500 500
	aa 0 2
}

gi {
   type ambocc
   bright { "sRGB nonlinear" 1 1 1 } 
   dark { "sRGB nonlinear" 0 0 0 }
   samples 64 
   maxdist 3.0 
}

accel bih
filter mitchell
bucket 32 spiral
 */    

    private int diffuseDefId = -1;    
    private int phongDefId = -1;
    private int shinyDefId = -1;
    private int mirrorDefId = -1;    
    private int reflId = -1;
    private int ambOccDefId = -1;
    private int darkId = -1;    
    private int samplesId = -1;
    private int distId = -1;
    private ArrayList<Definition.NameMap> shadings = new ArrayList<Definition.NameMap>();
    
    @Override
    protected String transform(Model m, ShapeReader bsr)
    {
        for (Definition.NameMap nm : m.defMaps)
        {
            if (nm.compareTo(new Definition.NameMap(-1, "diffuse"))==0)
                diffuseDefId = nm.getId();
            if (nm.compareTo(new Definition.NameMap(-1, "phong"))==0)
                phongDefId = nm.getId();
            else if (nm.compareTo(new Definition.NameMap(-1, "shiny"))==0)
                shinyDefId = nm.getId();
            else if (nm.compareTo(new Definition.NameMap(-1, "mirror"))==0)
                mirrorDefId = nm.getId();
            else if (nm.compareTo(new Definition.NameMap(-1, "refl"))==0)
                reflId = nm.getId();
            else if (nm.compareTo(new Definition.NameMap(-1, "ambocc"))==0)
                ambOccDefId = nm.getId();
            else if (nm.compareTo(new Definition.NameMap(-1, "dark"))==0)
                darkId = nm.getId();
            else if (nm.compareTo(new Definition.NameMap(-1, "samples"))==0)
                samplesId = nm.getId();
            else if (nm.compareTo(new Definition.NameMap(-1, "dist"))==0)
                distId = nm.getId();
        }
        
        StringBuilder bd = new StringBuilder();
        bd.append("% Generated by konte\n\n");
        
        bd.append(String.format(
                "image {\n\tresolution %s %s\n\taa 0 2\n}\n\n",
                bsr.getCanvas().getWidth(),bsr.getCanvas().getHeight())
                );
        bd.append(String.format(
"gi {\n\ttype %s\n\tbright { \"sRGB nonlinear\" 1 1 1 }\n\tdark { \"sRGB nonlinear\" 0 0 0 }\n\tsamples %s\n\tmaxdist %s\n}\n\n",
                "ambocc",64,3.0f));
        
        bd.append(String.format(
                "bucket %s %s\n\n",
                32,"spiral"));
        
        bd.append(
                "light {\n\ttype meshlight\n\tname \"meshlight\"\n\temit 1 1 1\n\tradiance 16\n\tsamples 32\n\tpoints 4\n\t" +
      "-1.79750967026 -6.22097349167 5.70054674149\n\t" +
      "-2.28231739998 -7.26064729691 4.06224298477\n\t" +
      "-4.09493303299 -6.41541051865 4.06224298477\n\t" +
      "-3.61012482643 -5.37573671341 5.70054721832\n\t" +
      "triangles 2\n\t0 1 2\n\t0 2 3\n}\n\n"
                );
        
        bd.append(
                "light {\n\ttype meshlight\n\tname \"meshlight.001\"\n\temit 1 1 1\n\tradiance 15\n\tsamples 32\n\tpoints 4\n\t" +
      "-4.25819396973 -4.8784570694 5.70054674149\n\t" +
      "-5.13696432114 -5.61583280563 4.06224298477\n\t" +
      "-6.422539711 -4.08374404907 4.06224298477\n\t" +
      "-5.54376888275 -3.34636831284 5.70054721832\n\t" +
      "triangles 2\n\t0 1 2\n\t0 2 3\n}\n\n"
                );        
      
      
      
              
        return bd.toString();
    }
}
