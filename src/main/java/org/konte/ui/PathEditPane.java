
/*
 * PathEditPane.java
 *
 * Created on 1.10.2009, 23:21:14
 */

package org.konte.ui;



import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.konte.image.SimpleCamera;
import org.konte.imprt.SvgImport;
import org.konte.lang.Tokenizer;
import org.konte.lang.Tokenizer.TokenizerString;
import org.konte.misc.Matrix4;
import org.konte.misc.ReverseParseTools;
import org.konte.misc.Vector3;
import org.konte.model.Model;
import org.konte.model.Path;
import org.konte.model.PathRule;
import org.konte.model.Rule;
import org.konte.parse.ParseException;
import org.konte.parse.Parser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author pvto
 */
public class PathEditPane extends javax.swing.JPanel {

    private PathPanel pathPanel1;
    private Float x0,y0;
    private float cr_ = (float)Math.sqrt(2f)/5f;
    private JFrame help;
    
    public Object[] defaultPaths = new Object[] {
        "SQUARE", new float[] {
            0.5f, 0.5f, 0f,
            0.5f, 0.5f, 0f,
            0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f,
            -0.5f, 0.5f, 0f,
            -0.5f, 0.5f, 0f,
            -0.5f, 0.5f, 0f,
            0.5f, 0.5f, 0f
        },
        "TRIANGLE", new float[] {
            0f, 0.5f, 0f,
            0f, 0.5f, 0f,
            0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0f, 0.5f, 0f
        },
        "CIRCLE", new float[] {
            0f, 0.5f, 0f,
            cr_, 0.5f, 0f,
            0.5f, cr_, 0f,
            0.5f, 0f, 0f,
            0.5f, -cr_, 0f,
            cr_, -0.5f, 0f,
            0f, -0.5f, 0f,
            -cr_, -0.5f, 0f,
            -0.5f, -cr_, 0f,
            -0.5f, 0f, 0f,
            -0.5f, cr_, 0f,
            -cr_, 0.5f, 0f
        }

        
    };

    int[] sel = null;
    /** Creates new form PathEditPane */
    public PathEditPane()
    {
        initComponents();
        pathPanel1 = new PathPanel();
        pathPanel1.setStretchAlong(0);
        this.add(pathPanel1, BorderLayout.CENTER);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("Pick object");
        for(int i = 0; i < defaultPaths.length; i+= 2)
        {
            model.addElement(defaultPaths[i]);
        }
        jComboBox1.setModel(model);

        pathPanel1.addMouseMotionListener(new MouseMotionListener()
        {
            private int grabbedNode = -1;   // add 0x1000 or 0x2000 if bend
            public void mouseDragged(MouseEvent e)
            {
                Vector3 v = pathPanel1.toModelCoords(e.getX(), e.getY());
                if (grabbedNode == -1)
                {
                    if (x0 == null)
                        sel = pathPanel1.getPivot(e, 0.02f, 3);
                    if (sel != null && sel[0] != -1)
                    {
                        x0 = y0 = null;
                        grabbedNode = sel[0];
                        if (sel[1] != -1)
                            grabbedNode += (sel[1]+1) * 0x1000;
                    }
                    else
                    {
                        if (x0 != null)
                        {
                            pathPanel1.setP0(
                                    pathPanel1.getX0()+(v.x-x0)/2f,
                                    pathPanel1.getY0()+(v.y-y0)/2f);
                            pathPanel1.repaint();
                        } 
                        x0 = v.x;
                        y0 = v.y;
                    }
                }
                if (grabbedNode != -1)
                {
                    int node = grabbedNode & 0xFFF;
                    if (e.isControlDown())
                    {
                        v = pathPanel1.snapToGrid(v);
                    }
                    float mx = v.x;
                    float my = v.y;
                    if (grabbedNode >= 0x1000)
                    {
                        int bend = grabbedNode >= 0x2000 ? 1 : 0;
                        Matrix4[] bends = pathPanel1.getPath().getControlPoints().get(sel[2]).get(node);
                        bends[bend] = Matrix4.translation(mx, my, bends[bend].m23);
                        if (e.isAltDown())
                        {
                            List<Matrix4> list = pathPanel1.getPath().getShapes().get(sel[2]);
                            int node2 = node;
                            if (bend == 1)
                            {
                                node2 = (node+1) % list.size();
                            }
                            list.set(node2, Matrix4.translation(mx, my, list.get(node2).m23));
                            Matrix4[] bends2 = pathPanel1.getPath().getControlPoints().get(sel[2]).get(node2);
                            bends2[1-bend] = Matrix4.translation(mx, my, bends2[1-bend].m23);
                        }
                    }
                    else
                    {
                        List<Matrix4> list = pathPanel1.getPath().getShapes().get(sel[2]);
                        list.set(grabbedNode, Matrix4.translation(mx, my, list.get(grabbedNode).m23));
                        if (e.isAltDown())
                        {
                            int node2 = node-1;
                            if (node2 == -1)
                                node2 = list.size()-1;
                            Matrix4[] bends = pathPanel1.getPath().getControlPoints().get(sel[2]).get(node);
                            bends[0] = Matrix4.translation(mx, my, bends[0].m23);
                            bends = pathPanel1.getPath().getControlPoints().get(sel[2]).get(node2);
                            bends[1] = Matrix4.translation(mx, my, bends[1].m23);
                        }
                    }
                    updateTextFromModel();
                    pathPanel1.repaint();
                }
            }
            public void mouseMoved(MouseEvent e)
            {
                grabbedNode = -1;
            }

        });
        pathPanel1.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseReleased(MouseEvent e)
            {
                super.mouseReleased(e);
                x0 = y0 = null;
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 1)
                {
                    if (e.getButton() != MouseEvent.BUTTON1 && e.isShiftDown())
                    {
                        int[] sel = pathPanel1.getPivot(e, 0.02f, 1);
                        if (sel[0] != -1 && sel[1] == -1)
                        {
                            pathPanel1.getPath().getShapes().get(sel[2]).remove(sel[0]);
                            List<Matrix4[]> cps = pathPanel1.getPath().getControlPoints().get(sel[2]);
                            int sel2 = sel[0] == 0 ? cps.size()-1 : sel[0]-1;
                            cps.get(sel2)[1] = cps.get(sel[0])[1];
                            cps.remove(sel[0]);
                            pathPanel1.repaint();
                            updateTextFromModel();
                        }
                    }
                }
                if (e.getClickCount() == 2)
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        Vector3 v = pathPanel1.toModelCoords(e.getX(), e.getY());
                        Path p = pathPanel1.getPath();
                        List<Matrix4> shapes = p.getShapes().get(0);
                        List<Matrix4[]> bends = p.getControlPoints().get(0);
                        int j = 1;
                        int sel = 0;
                        float mind = Float.MAX_VALUE;
                        for (int i = 0; i < shapes.size(); i++)
                        {
                            Matrix4 m = shapes.get(i);
                            Matrix4 m2 = shapes.get(j);
                            float dx = v.x-m.m03;
                            float dy = v.y-m.m13;
                            float tmpd = (float)Math.sqrt(dx*dx+dy*dy);
                            dx = v.x-m2.m03;
                            dy = v.y-m2.m13;
                            tmpd += (float)Math.sqrt(dx*dx+dy*dy);
                            if (tmpd < mind)
                            {
                                mind = tmpd;
                                sel = i+1;
                            }
                            j = (++j % shapes.size());
                        }
                        Matrix4 point = Matrix4.translation(v.x, v.y, v.z);
                        int prev = sel == 0 ? shapes.size()-1 : sel-1;
                        int next = sel == shapes.size() ? 0 : sel;
                        Matrix4[] newm = new Matrix4[] { point, bends.get(prev)[1] };
                        bends.get(prev)[1] = point;
                        bends.add(sel, newm);
                        shapes.add(sel, point);
                        pathPanel1.repaint();
                        updateTextFromModel();
                    }
                }
            }

        });

        FocusAdapter fa = new FocusAdapter()
        {
            public void focusGained(FocusEvent e)
            {
                javax.swing.ToolTipManager.sharedInstance().setDismissDelay(1000*30);
                Component c = (Component)e.getSource();
                c.dispatchEvent( new KeyEvent (c, KeyEvent.KEY_PRESSED, 0, KeyEvent.CTRL_MASK, KeyEvent.VK_F1) );
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                super.focusLost(e);
                javax.swing.ToolTipManager.sharedInstance().setDismissDelay(1000*3);
            }

        };
        MouseAdapter ma = new MouseAdapter()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                javax.swing.ToolTipManager.sharedInstance().setDismissDelay(1000*30);
                Component c = (Component)e.getSource();
                c.dispatchEvent( new KeyEvent (c, KeyEvent.KEY_PRESSED, 0, KeyEvent.CTRL_MASK, KeyEvent.VK_F1) );
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                super.mouseExited(e);
                javax.swing.ToolTipManager.sharedInstance().setDismissDelay(1000*3);
            }

        };
        jButton4.addMouseListener(ma);
    }

    public String getText()
    {
        return jTextArea1.getText();
    }

    public JButton getSaveButton()
    {
        return jButton3;
    }

    public PathPanel getPathPanel()
    {
        return pathPanel1;
    }

    private int lastFormSel = 1;
    public void changeToNewForm()
    {
        int ind = jComboBox1.getSelectedIndex();
        if (ind == 0)
            return;
        lastFormSel = ind;
        float[] obj = (float[]) defaultPaths[(ind-1)*2 + 1];
        try {
            Path path = (Path) PathRule.createUntransformable(obj);
            pathPanel1.setPath(path);
            pathPanel1.repaint();
            updateTextFromModel();
            jComboBox1.setSelectedIndex(0);
        }
        catch (ParseException ex)
        {
            ex.printStackTrace();
        }
    }

    public JComboBox getJComboBox1()
    {
        return jComboBox1;
    }

    private void importFromSvg()
    {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
                "Scalable Vector Graphics", "svg"));
        int retVal = fc.showOpenDialog(this);
        System.out.println(retVal);
        if (retVal == 0) { // ok
            File f = fc.getSelectedFile();
            if (f != null)
            {
                try {
                    SvgImport svg = new SvgImport();
                    svg.initDocument(f);

                    NodeList paths = svg.getPaths();
                    Object[] objs = new Object[paths.getLength()];
                    int sel = 0;
                    if (paths.getLength() > 0)
                    {
                        for (int i = 0; i < paths.getLength(); i++)
                        {
                            Node node = paths.item(i);
                            String id = node.getAttributes().getNamedItem("id").getTextContent();
                            objs[i] = id;
                        }
                        int sel2 = JOptionPane.showOptionDialog(this, "Select path by id", "Paths in " + f.getName(), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, objs, objs[0]);
                        sel = sel2;
                    }
                    Path p0 = svg.toKontePath(sel);
                    p0.name = paths.item(sel).getAttributes().getNamedItem("id").getTextContent();
                    pathPanel1.setPath(p0);
                    pathPanel1.repaint();
                    updateTextFromModel();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Float setGrid(String text)
    {
        try {
            float fl = Float.parseFloat(text);
            if (fl > 10f)
                fl = 10f;
            if (fl < 0.01f)
                fl = 0.01f;
            pathPanel1.setGrid(fl);
            pathPanel1.repaint();
            return fl;
        }
        catch (Exception e)
        {
        }
        return null;
    }

    private void setStretch(int i)
    {
        pathPanel1.setStretchAlong(i);
        pathPanel1.repaint();
    }

    private Float setZoomFactor(String text)
    {
        try {
            float fl = Float.parseFloat(text);
            if (fl > 10f)
                fl = 10f;
            if (fl < 0.1f)
                fl = 0.1f;
            pathPanel1.setZoomFactor(fl);
            pathPanel1.repaint();
            return fl;
        }
        catch (Exception e)
        {
        }
        return null;
    }


    private void showPathEditHelp()
    {

    }

    private void updateModelFromText()
    {
        try {
            String grammar = jTextArea1.getText();
            ArrayList<TokenizerString> tokens = null;
            try {
                tokens = Tokenizer.retrieveTokenStrings(new StringBuilder(grammar));
            }
            catch(ParseException pe)
            {
                pe.printStackTrace();
            }
            Parser parser = new Parser();
            Model model = parser.parse(tokens);
            model.initForGenerate();
            for (int i = 0; i < model.indexedRules.length; i++)
            {
                Rule rule = model.indexedRules[i];
                if (rule instanceof PathRule)
                {
                    PathRule pr = (PathRule)rule;
                    Path ph = (Path)pr.createUntransformable();
                    pathPanel1.setPath(ph);
                    pathPanel1.repaint();
                    break;
                }
            }
        }
        catch (IllegalArgumentException ex)
        {
            ex.printStackTrace();
        }
        catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
        }
        catch (ParseException ex)
        {
            ex.printStackTrace();
        }
    }

    private void updateTextFromModel()
    {
        Path path = pathPanel1.getPath();
        String res = ReverseParseTools.pathToScript(path);
        jTextArea1.setText(res);
    }

    private void changeName()
    {
        String gram = jTextArea1.getText();
        Pattern pattern = Pattern.compile("(path\\s+)([a-zA-Z0-9\\.\\-_]*)(\\s*\\{)");
        Matcher matcher = pattern.matcher(gram);
        String name = "";
        if (matcher.find())
        {
            name = matcher.group(2);
        }
        String name2 = JOptionPane.showInputDialog(null, "Type new name for the path", name);
        if (name2 != null && !name.isEmpty())
        {
            gram = matcher.replaceFirst(String.format("$1%s$3",name2));
            jTextArea1.setText(gram);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setBackground(new java.awt.Color(187, 203, 209));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setPreferredSize(new java.awt.Dimension(300, 376));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBackground(new java.awt.Color(187, 203, 209));

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 11));
        jTextArea1.setRows(5);
        jTextArea1.setText("path mypath {\n\n\n}");
        jTextArea1.setPreferredSize(new java.awt.Dimension(142, 382));
        jTextArea1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jTextArea1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setBackground(new java.awt.Color(187, 203, 209));
        jPanel2.setMinimumSize(new java.awt.Dimension(290, 60));
        jPanel2.setPreferredSize(new java.awt.Dimension(290, 70));

        jLabel1.setText("z dist:");
        jPanel2.add(jLabel1);

        jTextField1.setText("1.5");
        jTextField1.setToolTipText("Zoom factor");
        jTextField1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanel2.add(jTextField1);

        jLabel3.setText("grid:");
        jPanel2.add(jLabel3);

        jTextField2.setText("0.1");
        jTextField2.setToolTipText("Grid spacing (units)");
        jTextField2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jTextField2ActionPerformed(evt);
            }
        });
        jPanel2.add(jTextField2);

        jLabel2.setText("Stretch:");
        jPanel2.add(jLabel2);

        jRadioButton1.setBackground(new java.awt.Color(187, 203, 209));
        jRadioButton1.setText("x");
        jRadioButton1.setToolTipText("This will enable automatic horizontal scaling");
        jRadioButton1.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jRadioButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jRadioButton1);

        jRadioButton2.setBackground(new java.awt.Color(187, 203, 209));
        jRadioButton2.setText("y");
        jRadioButton2.setToolTipText("This will enable automatic vertical scaling");
        jRadioButton2.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jRadioButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jRadioButton2);

        jRadioButton3.setBackground(new java.awt.Color(187, 203, 209));
        jRadioButton3.setSelected(true);
        jRadioButton3.setText("none");
        jRadioButton3.setToolTipText("This will disable automatic scaling");
        jRadioButton3.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jRadioButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButton3ActionPerformed(evt);
            }
        });
        jPanel2.add(jRadioButton3);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pick form" }));
        jComboBox1.setToolTipText("Select new shape to start with");
        jComboBox1.setPreferredSize(new java.awt.Dimension(100, 22));
        jComboBox1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jComboBox1ActionPerformed(evt);
            }
        });
        jPanel2.add(jComboBox1);

        jButton1.setBackground(new java.awt.Color(187, 203, 209));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/applications.png"))); // NOI18N
        jButton1.setToolTipText("Change path name");
        jButton1.setBorder(null);
        jButton1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/applications.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        jButton2.setBackground(new java.awt.Color(187, 203, 209));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/clipping_unknow.png"))); // NOI18N
        jButton2.setToolTipText("Copy to clipboard");
        jButton2.setBorder(null);
        jButton2.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/clipping_unknow.png"))); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton3.setBackground(new java.awt.Color(187, 203, 209));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/floppy.png"))); // NOI18N
        jButton3.setToolTipText("Save path");
        jButton3.setBorder(null);
        jButton3.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/floppy.png"))); // NOI18N
        jPanel2.add(jButton3);

        jButton5.setBackground(new java.awt.Color(187, 203, 209));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/Inkscape.png"))); // NOI18N
        jButton5.setToolTipText("Import path from SVG");
        jButton5.setBorder(null);
        jButton5.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/Inkscape.png"))); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton5);

        jButton4.setBackground(new java.awt.Color(187, 203, 209));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/Help file.png"))); // NOI18N
        jButton4.setToolTipText("<html>\n<h3>Mouse help-chart: draw area</h3>\n<p><b>Left Double Click</b> insert node\n<p><b>Right Click+shift</b> remove node\n<p><b>Left Drag+ctrl</b> snap to grid\n<p><b>Left Drag+alt</b> move node and controls together\n<p>\n<h3>Mouse help-chart: text area</h3>\n<p><b>Left Click</b> redraw\n\n</html>");
        jButton4.setBorder(null);
        jButton4.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/Help file.png"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton4);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        add(jPanel1, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        changeName();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        ClipboardOwner owner = new ClipboardOwner()
        {
            public void lostOwnership(Clipboard clipboard, Transferable contents)
            {
            }
        };
        StringSelection contents = new StringSelection(jTextArea1.getText());
        cb.setContents(contents, owner);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        final Float fl = setZoomFactor(jTextField1.getText().replaceAll(",+", "."));
        if (fl != null)
        {
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    jTextField1.setText(String.format(Locale.ENGLISH, "%.1f", fl));
                }
            });
        }
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        changeToNewForm();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        setStretch(0);
        jRadioButton1.setSelected(false);
        jRadioButton2.setSelected(false);
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        setStretch(2);
        jRadioButton1.setSelected(false);
        jRadioButton3.setSelected(false);
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        setStretch(1);
        jRadioButton2.setSelected(false);
        jRadioButton3.setSelected(false);
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        final Float fl = setGrid(jTextField2.getText().replaceAll(",+", "."));
        if (fl != null)
        {
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    jTextField2.setText(String.format(Locale.ENGLISH, "%.1f", fl));
                }
            });
        }
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextArea1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextArea1MouseClicked
        updateModelFromText();
    }//GEN-LAST:event_jTextArea1MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        showPathEditHelp();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        importFromSvg();
    }//GEN-LAST:event_jButton5ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) throws ParseException 
    {
        JFrame test = new JFrame();
        test.setSize(800,600);
        PathEditPane pe = new PathEditPane();
        pe.pathPanel1.setCamera(new SimpleCamera());
        test.add(pe);
        test.setVisible(true);
        pe.pathPanel1.init();
        pe.getJComboBox1().setSelectedIndex(1);
        pe.changeToNewForm();
        pe.repaint();
        
    }
}
