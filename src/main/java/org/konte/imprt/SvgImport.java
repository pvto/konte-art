
package org.konte.imprt;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.konte.expression.Expression;
import org.konte.expression.Value;
import org.konte.misc.Matrix3;
import org.konte.misc.ReverseParseTools;
import org.konte.misc.Vector3;
import org.konte.model.Path;
import org.konte.model.PathRule;
import org.konte.model.PathRule.Placeholder;
import org.konte.parse.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author pvto
 */
public class SvgImport {

    private Document doc;
    private XPath xpath;
    private float w;
    private float h;
    private float hperw;

    // see http://www.w3.org/TR/SVG/coords.html#Units
    public static final float pt_px = 1.25f;
    public static final float pc_px = 15f;
    public static final float mm_px = 3.5433f;
    public static final float cm_px = 35.433f;
    public static final float in_px = 90f;
    public static final String[] unit_name_tbl = new String[] {
        "px","pt","pc","mm","cm","in"
    };
    public static final float[] unit_tbl = new float[] {
        1f,pt_px,pc_px,mm_px,cm_px,in_px
    };

    public SvgImport()
    {
    }

    public Document initDocument(File f) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
    {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            return initDocument(in);
       } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
        }
    }
    
    public Document initDocument(InputStream in) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        doc = builder.parse(in);

        xpath = XPathFactory.newInstance().newXPath();

        NodeList wlist = doc.getElementsByTagName("svg");
        if (wlist != null && wlist.getLength() > 0)
        {
            String s_w = "1000";
            String s_h = "1000";
            s_w = wlist.item(0).getAttributes().getNamedItem("width").getTextContent();
            s_h = wlist.item(0).getAttributes().getNamedItem("height").getTextContent();
            w = Float.parseFloat(s_w.replaceAll("[a-zA-Z]+", ""));
            h = Float.parseFloat(s_h.replaceAll("[a-zA-Z]+", ""));
            for(int i = 0; i < unit_tbl.length; i++)
            {
                if (s_w.matches("\\d+"+unit_name_tbl[i]))
                {
                    w = w*unit_tbl[i];
                }
                if (s_h.matches("\\d+"+unit_name_tbl[i]))
                {
                    h = h*unit_tbl[i];
                }
            }
        } else {
            w = 1.0f;
            h = 1.0f;
        }
        hperw = h/w;
        return doc;
    }
    
                            //new ByteArrayInputStream(str.getBytes()));

    public NodeList getPaths() throws XPathExpressionException {
        XPathExpression expr = xpath.compile("//path");
        NodeList paths = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        if (paths.getLength() == 0)
        {
            System.out.println("No paths");
        }
        return paths;
    }

    public Path toKontePath(int sel) throws XPathExpressionException, ParseException {
        Node path = getPaths().item(sel);
        Matrix3 group = new Matrix3(1f,0f,0f,0f,1f,0f,0f,0f,1f);
        ArrayDeque<Node> groups = new ArrayDeque<Node>();
        Node parent = path.getParentNode();
        while (parent.getNodeName().toLowerCase().equals("g"))
        {
            groups.push(parent);
            parent = parent.getParentNode();
        }
        while(groups.size() > 0)
        {
            parent = groups.pop();
            if (parent.hasAttributes())
            {
                Node tr = parent.getAttributes().getNamedItem("transform");
                if (tr != null)
                {
                    Matrix3 g = new Matrix3(1f,0f,0f,0f,1f,0f,0f,0f,1f);
                    String d = tr.getTextContent();
                    if (d.startsWith("matrix"))
                    {
                        d = d.substring(d.indexOf('(') + 1, d.indexOf(')'));
                        String[] dd = d.split("[\\s,]+");
                        g = new Matrix3(Float.parseFloat(dd[0]), 0f, Float.parseFloat(dd[4]),
                                0f, Float.parseFloat(dd[3]), Float.parseFloat(dd[5]),
                                0f, 0f, 1f);
                    }
                    Matrix3 tmp = new Matrix3();
                    Matrix3.multiply(group, g, tmp);
                    group = tmp;
                }
                
            }
        }
        String d = path.getAttributes().getNamedItem("d").getTextContent();
        if (d.matches(".*[aAqQ].*"))
        {
            System.out.println(sel + ": unsupported directive(s) in " + d);
            return null;
        }
        
        StringTokenizer tok = new StringTokenizer(d," \t\r\n,");
        float curx,cury;
        PathRule pr = new PathRule();
        char lastDig = 'M';
        Expression[] data = new Expression[3];
        Expression[] lastData = new Expression[3];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = new Value(0f);
            lastData[i] = new Value(0f);
        }
        int pl = 0;
        int it = 0;
        char digBefore = 'x';
        int flags = PathRule.REMOVE_CLOSING;
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken();
            if (!Character.isDigit(token.charAt(0)) 
                    && token.charAt(0) != '-')
                    {
                lastDig = token.charAt(0);
                if ((lastDig == 'm' || lastDig == 'M') && (digBefore != 'z' && digBefore != 'x'))
                {
                    pr.steps.add(new Placeholder(PathRule.CLOSE, null));
                    //flags ^= PathRule.REMOVE_CLOSING;
                }
                digBefore = lastDig;
                it = 0;
                if (lastDig == 'z' && pr.steps.size() > 0)
                {
                    pr.steps.add(new Placeholder(PathRule.CLOSE, null));
                }
            } else {
                float val = Float.parseFloat(token);
                if (!Character.isLowerCase(lastDig) || pr.steps.size() == 0)
                {
                    val -= 0.5f * (pl == 0 ? 1f*w/group.m00 : hperw*w/group.m11);
                }
                if (pl == 1)
                {
                    val = -val; // y axis is downward in svg (is it regularly)
                }

                data[pl] = new Value(val);
                if (pl == 1)
                {
                    Placeholder e = null;
                    if (Character.isLowerCase(lastDig)) { // lower case are relative
                        data[0] = new Value(data[0].evaluate() + lastData[0].evaluate());
                        data[1] = new Value(data[1].evaluate() + lastData[1].evaluate());
                    }
                    if (lastDig == 'm' && it > 0)
                    {
                        lastDig = 'l';
                    }
                    Expression[] fdata = new Expression[] { 
                        new Value((data[0].evaluate()*group.m00 + group.m02) / w),
                        new Value((data[1].evaluate()*group.m11 - group.m12) / w),
                        new Value(0f) };

                    switch(lastDig)
                    {
                        case 'm':
                        case 'M': e = new Placeholder(PathRule.MOVE_TO, fdata);
                            break;
                        case 'c':
                        case 'C': e = new Placeholder((it % 3 != 2) ?
                            PathRule.BEND : PathRule.CURVE_TO,
                                fdata);
                            break;
                        case 'l':
                        case 'L': e = new Placeholder(PathRule.LINE_TO, fdata);
                            break;
                    }
                    pr.steps.add(e);
                    pl = 0;
                    it++;
                    if (lastDig == 'm' || lastDig == 'M' || lastDig == 'l' || lastDig == 'L' || ((lastDig == 'c' || lastDig == 'C') && e.type == PathRule.CURVE_TO))
                        lastData = data;
                    data = new Expression[3];
                    data[2] = new Value(0f);
                } else {
                    pl++;
                }
            }
        }
        Path p0 = (Path)pr.createUntransformable(flags);
        return p0;

    }


    public String allPathsToScript(String prefix, HashMap<String, String> props)
    {
        StringBuilder bd = new StringBuilder();
        try {
            StringBuilder all = new StringBuilder();
            all.append(String.format("rule __%s_ALL_ {\n", prefix));

            NodeList paths = getPaths();
            Object[] objs = new Object[paths.getLength()];
            for (int i = 0; i < paths.getLength(); i++)
            {
                Path p0 = toKontePath(i);
                if (p0 != null)
                {
                    Node styleNode = paths.item(i).getAttributes().getNamedItem("style");
                    String style = styleNode == null ? "" : styleNode.getTextContent();
                    p0.name = prefix + paths.item(i).getAttributes().getNamedItem("id").getTextContent();
                    String name = p0.name;
                    String fill = props.get("fill");
                    if (fill != null)
                    {
                        name = p0.name+"_";
                        Color col = null;
                        String trfm = "rule %s{ %s{RGB %.3f %.3f %.3f A %.3f} }\n";
                        if (fill.equals("R G B A"))
                        {
                            trfm = "rule %s{ %s{R %.3f G %.3f B %.3f A %.3f} }\n";
                        } else if (fill.equals("R G B"))
                        {
                            trfm = "rule %s{ %s{R %.3f G %.3f B %.3f} }\n";
                        }
                        int ind = style.indexOf("fill:#");
                        if (ind >= 0)
                        {
                            int rgb = Integer.parseInt(style.substring(ind+6,ind+12), 16);
                            col = new Color(rgb);
                        } else {
                            col = Color.BLACK;
                        }
                        bd.append(String.format(Locale.ENGLISH,
                                trfm,
                                name,
                                p0.name,
                                (float)col.getRed()/255f,
                                (float)col.getGreen()/255f,
                                (float)col.getBlue()/255f,
                                (float)col.getAlpha()/255f
                                ));

                    }
                    bd.append(ReverseParseTools.pathToScript(p0));
                    bd.append("\n");
                    all.append(String.format("    %s {}\n", name));
                }
            }
            all.append("}\n\n");
            bd.insert(0, all.toString());
            return bd.toString();
        } catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println(bd);
        }
        return "";
    }

}
