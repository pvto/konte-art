
package org.konte.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.konte.export.AbstractExporterBase;
import org.konte.generate.ImageAPI.RenderType;
import org.konte.generate.RandomFeed;
import org.konte.lang.help.Help;
import org.konte.misc.Readers;
import org.konte.generate.Runtime;
import org.konte.image.SeqRecorder;
import org.konte.imprt.SvgImport;
import org.konte.lang.Language;

/**<a>A swing-based interface to the rendering engine.
 *
 * @author  pto
 */
public class Ui extends MyJFrame {

    private GenerateDialog generDia;
    private FileDialog fileDialog;
    private ColSpaceEditFrame colSpaceEditFrame;
    private MessagesList messagesList1;
    private RndCharSliderBar rndCharSliderBar1;
    private RandomFeed rndFeed;
    static Ui instance;
    private java.util.ArrayDeque<String> deq = new ArrayDeque<String>(10)
    {

        public boolean add(String e)
        {
            super.add(e);
            if (this.size() > 10)
            {
                this.removeFirst();
            }
            return true;
        }
    };

    private KeyEventPostProcessor pp = new KeyEventPostProcessor()
    {

        public boolean postProcessKeyEvent(KeyEvent evt)
        {
//            System.out.println(evt.paramString());
            if (!Ui.this.isFocused())
            {
//                System.out.println("Ui.java - no focus");
                return false;
            }
            if (evt.getID() == evt.KEY_RELEASED)
            {

                if (evt.isControlDown())
                {
                    switch(evt.getKeyCode())
                    {
                        case 70:   // Ctrl+F
                            replaceItemActionPerformed(null);
                            repDi.focus('f');
                            break;
                        case 72:   // Ctrl+H
                            replaceItemActionPerformed(null);
                            repDi.focus('h');
                            break;
                    }

                }
            }
            return true;
        }

    };
    private Controller controller;
    private PathEditFrame pathEditFrame;
//    KeyComboHandler keyComboHandler = new KeyComboHandler(2);
    /** Creates new form Ui */
    public Ui()
    {
        super(new FileNameExtensionFilter(
                "cfdg and c3dg grammar files", "cfdg", "c3dg"));
        initComponents();
        this.setTitle(String.format(Locale.ENGLISH, "konte %.2f", Language.version));
        Color bgc = new Color(187,203,209);
        rndCharSliderBar1 = new RndCharSliderBar();
        rndCharSliderBar1.setBackground(bgc);
        rndCharSliderBar1.setAlignmentY(0.5f);
        
        jToolBar1.add(rndCharSliderBar1);
        messagesList1 = new MessagesList();
        messagesList1.setBackground(bgc);
        jToolBar1.add(messagesList1);
        messagesList1.setPreferredSize(jToolBar1.getSize());
        this.setIconImage(new ImageIcon(getClass().
                getResource("/org/konte/resources/images/k_icon.png")).getImage());
        initTutorials();
        //shortcuts = 
        addUntitledTab();
        rndFeed = new RandomFeed();
        instance = this;

        controller = new Controller();
        this.addWindowListener(new WindowAdapter()
        {

            @Override
            public void windowOpened(WindowEvent e)
            {
                String svgD = props.getProperty("SvgPath");
                if (svgD != null)
                    svgDir = new File(svgD);
            }
        });
        openTut("/org/konte/resources/exmpl/k_logo");
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(pp);
    }
    
    protected void finalizeProps()
    {
        super.finalizeProps();
        for (int i = 10; i > 0; i--)
        {
            String s = props.getProperty("M" + i);
            if (s != null)
                addLatest(s);
        }
        updateLatestMenu();                
        
    }

    public void makeOpenDialog()
    {
        setFileChooser();

        int retVal = getFc().showOpenDialog(this);
        System.out.println(retVal);
        if (retVal == 0) { // ok
            File f = getFc().getSelectedFile();
            if (f != null)
            {
                open(f);
            }
        }
    }

    private void addLatest(String s)
    {
        if (deq.contains(s))
            deq.remove(s);
        deq.addFirst(s);
    }

    JFrame helpFrame;
    JEditorPane helpPane;

    private File svgDir = null;

    private void exportSvg()
    {
        controller.addTask(new Controller.TaskInfo(
                Controller.Task.EXPORT_SCENE, new Object[]{AbstractExporterBase.Type.SVG}));
    }

    private void importFromSvg()
    {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
                "Scalable Vector Graphics", "svg"));
        if (svgDir != null)
            fc.setCurrentDirectory(svgDir);
        int retVal = fc.showOpenDialog(this);
        System.out.println(retVal);
        if (retVal == 0) { // ok
            File f = fc.getSelectedFile();
            if (f != null)
            {
                try {
                    svgDir = new File(f.getParent());
                    props.setProperty("SvgPath", f.getParent());
                    saveProps();
                } catch(Exception ex) { }
                try {
                    SvgImport svg = new SvgImport();
                    svg.initDocument(f);
                   
                    String prefix = JOptionPane.showInputDialog("Enter path prefix:");
                    if (prefix != null)
                    {
                        addUntitledTab();
                        EditViewCombo ev = (EditViewCombo)tabs.getSelectedComponent();
                        HashMap<String,String> props = new HashMap<String,String>();
                        props.put("fill", "R G B");
                        String text = svg.allPathsToScript(prefix, props);
                        ev.setText(text);
                    }

                } catch(Exception ex)
                {
                    ex.printStackTrace();
                    addMessage(ex.getMessage());
                }

            }
        }
    }

    private void openColSpaceEditFrame()
    {
        if (colSpaceEditFrame == null)
        {
            colSpaceEditFrame = new ColSpaceEditFrame();
            colSpaceEditFrame.setSize(640,600);
        }
        colSpaceEditFrame.setVisible(true);
        colSpaceEditFrame.toFront();
    }

    private void openPathEditFrame()
    {
        if (pathEditFrame == null)
        {
            try {
                pathEditFrame = new PathEditFrame();
            } catch(Exception ex)
            {
                ex.printStackTrace();
            }
            pathEditFrame.setSize(800,600);
        }
        pathEditFrame.setVisible(true);
        pathEditFrame.toFront();
    }

    private void pickColor()
    {
        Color color = JColorChooser.showDialog(this, "Select color to clipboard", Color.BLACK);
        if (color != null)
        {
            String rgb = String.format(Locale.ENGLISH, "RGB %.3f %.3f %.3f",
                    (float)color.getRed() / 255f,
                    (float)color.getGreen() / 255f,
                    (float)color.getBlue() / 255f
                    );
            addMessage("\"" + rgb + "\" copied to clipboard");
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            ClipboardOwner owner = new ClipboardOwner()
            {
                public void lostOwnership(Clipboard clipboard, Transferable contents)
                {
                }
            };
            StringSelection contents = new StringSelection(rgb);
            cb.setContents(contents, owner);
        }
        else
        {

        }
    }
    
    private void showHelp(String string)
    {
        if (helpFrame==null)
        {
            helpFrame = new JFrame();            
            helpPane = new JEditorPane();
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.CENTER));
            p.add(new JButton("Close")
            {

                @Override
                protected void processMouseEvent(MouseEvent e)
                {
                    super.processMouseEvent(e);
                    helpFrame.setVisible(false);
                }
                
            });
            helpPane.setContentType("text/html");            

            helpFrame.add(new JScrollPane(helpPane), BorderLayout.CENTER);
            helpFrame.add(p,BorderLayout.SOUTH);
            helpFrame.setSize(1024,768);
        }
        helpFrame.setVisible(true);
        helpPane.setText(Help.help(string, false));
        helpPane.setCaretPosition(0);
    }
    private void updateLatestMenu()
    {
        //this.latestMenu.removeAll();
        for (int i = this.fileMenu.getItemCount()-1; i >= 0; i--)
        {
            Component comp = (fileMenu.getItem(i));
            if (comp==null)
                continue;
            String s = ((JMenuItem)comp).getText();
             if (s != null && s.startsWith("  "))
                 fileMenu.remove(i);
             else
                 break;
        }
        int count = 1;
        for (String s : deq)
        {
            props.setProperty("M" + count, s);            
            final JMenuItem item = new JMenuItem();
            item.setText(s);
            item.addActionListener(new java.awt.event.ActionListener()
            {

                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                    open(new File(item.getText().substring(2)));
                }
            });
            if (count < 9)
                item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_0+count, 
                    java.awt.event.InputEvent.ALT_MASK));
            fileMenu.add(item);
            count++;
        }
    }
    
    private void open(File f)
    {
        setPath(f);
        try {
            String text = Readers.fillStringBuilder(f).toString();
            EditViewCombo ev = newEditViewCombo(f.getName(), this.getSize());
            ev.setFile(f);
            ev.edit.setText(text);
            openTab(ev);
            addLatest("  " + f.getAbsolutePath());
            updateLatestMenu();
            saveProps();
        } catch(Exception e)
        {
            this.addMessage(e.getMessage());
        }
    }

    private void openTab(EditViewCombo ev)
    {
        tabs.add(ev, ev.getName());
        tabs.setSelectedComponent(ev);
        ev.edit.requestFocus();
        ev.edit.setCaretPosition(0);
    }

    void addUntitledTab()
    {
        EditViewCombo evc = newEditViewCombo("Untitled", this.getSize());
        openTab(evc);
    }

    void closeTab()
    {
        if (tabs.getSelectedComponent() != null)
        {
            tabs.remove(tabs.getSelectedComponent());
        }
    }

    public void save()
    {
        EditViewCombo ev = (EditViewCombo) tabs.getSelectedComponent();
        if (ev.getFile() != null)
        {
            FileWriter fw = null;
            try {
                fw = new FileWriter(ev.getFile());
                fw.write(ev.getScriptText());
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally {
                try {
                    fw.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            saveWithName();
        }
    }

    public void saveWithName()
    {
        setFileChooser();
        if (getPath() != null)
        {
            getFc().setCurrentDirectory(new File(getPath()));
        }
        int retVal = getFc().showSaveDialog(this);
        if (retVal == 0) { // ok
            File f = getFc().getSelectedFile();
            getFc().isAcceptAllFileFilterUsed();
            if (f != null)
            {
                if (getFc().getFileFilter() != getFc().getAcceptAllFileFilter() &&
                        !f.getName().toLowerCase().endsWith(".c3dg"))
                        {
                    f = new File(f.getAbsolutePath() + ".c3dg");
                }
                EditViewCombo ev = (EditViewCombo) tabs.getSelectedComponent();
                ev.setFile(f);
                this.setPath(f.getAbsolutePath().replaceFirst(f.getName() + "$", ""));
                save();
                tabs.setTitleAt(tabs.getSelectedIndex(), f.getName());
                addLatest("  " + f.getAbsolutePath());
                updateLatestMenu();
                saveProps();
            }
        }

    }

    void addMessage(String string)
    {
        messagesList1.addMessage(string);
    }

    void incrementVariationKey()
    {
        EditViewCombo ev = (EditViewCombo) tabs.getSelectedComponent();
        if (ev.lastRenderKey != null && ev.lastRenderKey.equals(rndCharSliderBar1.getKeyCode()))
            rndCharSliderBar1.addToSlider(1);
    }

    RandomFeed synchronizeVariationKey()
    {
        rndFeed.setKey(rndCharSliderBar1.getKeyCode());
        return rndFeed;
    }

    public void export()
    {
        EditViewCombo ev = (EditViewCombo) tabs.getSelectedComponent();
        controller.addTask(new Controller.TaskInfo(Controller.Task.EXPORT, new Object[]{ev, getPath()}));

    }

    private void initTutorials()
    {
        final String pth = "/org/konte/resources/exmpl/";
        String res = pth + "__list";
        BufferedReader br = null;
        try {

            br = new BufferedReader(new InputStreamReader(Ui.class.getResource(res).openStream()));
            String s;
            while ((s = br.readLine()) != null)
            {
                final JMenuItem item = new JMenuItem();
                item.setText(s.replaceAll("_", " "));
                item.addActionListener(new java.awt.event.ActionListener()
                {

                    public void actionPerformed(java.awt.event.ActionEvent evt)
                    {
                        if (!item.getText().matches("^-+$"))
                            openTut(pth + item.getText().replaceAll(" ", "_"));
                    }
                });
                this.tutMenu.add(item);
            }
        } catch (IOException ex)
        {
            Runtime.sysoutln("Can't find resource " + res, 5);
        } finally {
            try {
                br.close();
            } catch (IOException ex)
            {

            }
        }
    }

    private void openTut(String res)
    {
        BufferedReader br = null;
        try {

            StringBuilder bd = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(Ui.class.getResource(res).openStream()));
            String s;
            while ((s = br.readLine()) != null)
            {
                bd.append(s).append("\r\n");
            }

            EditViewCombo ev = newEditViewCombo("? " + res.substring(res.lastIndexOf("/") + 1).replaceAll("_", " "), this.getSize());
            ev.edit.setText(bd.toString());
            openTab(ev);
            new Thread(new Runnable()
            {
                public void run()
                {
                    generatePic();
                }
            }).start();
        } catch (IOException ex)
        {
            Runtime.sysoutln("Can't find resource " + res, 5);
        } finally {
            try {
                br.close();
            } catch (IOException ex)
            {

            }
        }
    }

    private EditViewCombo newEditViewCombo(String h, Dimension dim)
    {
        EditViewCombo evc = new EditViewCombo(h, dim)
        {
            @Override
            void finishRender()
            {
                messagesList1.scrollDown();
            }
        };
        evc.setRandomFeed(rndFeed);
        return evc;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        tabs = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jPanel3 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        newMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        editMenu = new javax.swing.JMenu();
        undoItem = new javax.swing.JMenuItem();
        redoItem = new javax.swing.JMenuItem();
        replaceItem = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        generateMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        tutMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("konte");
        setBackground(new java.awt.Color(187, 203, 209));

        jSplitPane1.setBackground(new java.awt.Color(187, 203, 209));
        jSplitPane1.setDividerLocation(40);
        jSplitPane1.setDividerSize(3);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBackground(new java.awt.Color(187, 203, 209));

        tabs.setBackground(new java.awt.Color(187, 203, 209));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
        );

        jSplitPane1.setBottomComponent(jPanel1);

        jPanel2.setFocusable(false);
        jPanel2.setLayout(new java.awt.BorderLayout());

        jToolBar1.setBackground(new java.awt.Color(187, 203, 209));
        jToolBar1.setRollover(true);

        jPanel3.setBackground(new java.awt.Color(187, 203, 209));
        jPanel3.setAlignmentY(0.0F);
        jPanel3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel3.setMaximumSize(new java.awt.Dimension(260, 64));
        jPanel3.setPreferredSize(new java.awt.Dimension(200, 44));
        jPanel3.setVerifyInputWhenFocusTarget(false);
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        jButton6.setBackground(new java.awt.Color(187, 203, 209));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/document.png"))); // NOI18N
        jButton6.setToolTipText("New file");
        jButton6.setAlignmentY(1.0F);
        jButton6.setBorder(null);
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/document.png"))); // NOI18N
        jButton6.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton6cancelMM(evt);
            }
        });
        jPanel3.add(jButton6);

        jButton4.setBackground(new java.awt.Color(187, 203, 209));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/floppy.png"))); // NOI18N
        jButton4.setToolTipText("Save");
        jButton4.setAlignmentY(0.0F);
        jButton4.setBorder(null);
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/floppy.png"))); // NOI18N
        jButton4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton4cancelMM(evt);
            }
        });
        jPanel3.add(jButton4);

        jButton5.setBackground(new java.awt.Color(187, 203, 209));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/png.png"))); // NOI18N
        jButton5.setToolTipText("Export png");
        jButton5.setAlignmentY(0.0F);
        jButton5.setBorder(null);
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/png.png"))); // NOI18N
        jButton5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton5cancelMM(evt);
            }
        });
        jPanel3.add(jButton5);

        jButton3.setBackground(new java.awt.Color(187, 203, 209));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/preview.png"))); // NOI18N
        jButton3.setToolTipText("Fast preview");
        jButton3.setAlignmentY(0.0F);
        jButton3.setBorder(null);
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/preview.png"))); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton3cancelMM(evt);
            }
        });
        jPanel3.add(jButton3);

        jButton1.setBackground(new java.awt.Color(187, 203, 209));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/niceplayer.png"))); // NOI18N
        jButton1.setToolTipText("Generate");
        jButton1.setAlignmentY(0.0F);
        jButton1.setBorder(null);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/niceplayer.png"))); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                generateMM(evt);
            }
        });
        jPanel3.add(jButton1);

        jButton2.setBackground(new java.awt.Color(187, 203, 209));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/Stop.png"))); // NOI18N
        jButton2.setToolTipText("Stop");
        jButton2.setAlignmentY(0.0F);
        jButton2.setBorder(null);
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/Stop.png"))); // NOI18N
        jButton2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelMM(evt);
            }
        });
        jPanel3.add(jButton2);

        jToolBar1.add(jPanel3);

        jPanel2.add(jToolBar1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel2);

        menuBar.setBackground(new java.awt.Color(187, 203, 209));

        fileMenu.setBackground(new java.awt.Color(187, 203, 209));
        fileMenu.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                openM(evt);
            }
        });
        fileMenu.add(openMenuItem);

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                newM(evt);
            }
        });
        fileMenu.add(newMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveMM(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveAsM(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setText("Close");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                closeM(evt);
            }
        });
        fileMenu.add(jMenuItem6);

        jMenuItem12.setText("Inkscape/svg path import");
        jMenuItem12.setToolTipText("Import available paths from a svg file");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem12ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem12);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        exitMenuItem.setText("Quit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);
        fileMenu.add(jSeparator1);

        menuBar.add(fileMenu);

        editMenu.setBackground(new java.awt.Color(187, 203, 209));
        editMenu.setText("Edit");

        undoItem.setText("Undo");
        undoItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                undoItemActionPerformed(evt);
            }
        });
        editMenu.add(undoItem);

        redoItem.setText("Redo");
        redoItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                redoItemActionPerformed(evt);
            }
        });
        editMenu.add(redoItem);

        replaceItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        replaceItem.setText("Find/Replace");
        replaceItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                replaceItemActionPerformed(evt);
                deleteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(replaceItem);

        jMenuItem10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem10.setText("Shading editor");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem10ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem10);

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem11.setText("Path editor");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem11ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem11);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("Pick Color");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem5ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem5);

        menuBar.add(editMenu);

        generateMenu.setBackground(new java.awt.Color(187, 203, 209));
        generateMenu.setText("Generate");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Generate");
        jMenuItem1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                generate(evt);
            }
        });
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                generateMM(evt);
            }
        });
        jMenuItem1.addMenuKeyListener(new javax.swing.event.MenuKeyListener()
        {
            public void menuKeyPressed(javax.swing.event.MenuKeyEvent evt)
            {
                generateM(evt);
            }
            public void menuKeyReleased(javax.swing.event.MenuKeyEvent evt)
            {
            }
            public void menuKeyTyped(javax.swing.event.MenuKeyEvent evt)
            {
            }
        });
        generateMenu.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Generate to Size");
        jMenuItem2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                generSM(evt);
            }
        });
        jMenuItem2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                generSizeM(evt);
            }
        });
        generateMenu.add(jMenuItem2);

        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem8.setText("Stop");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelMM(evt);
            }
        });
        generateMenu.add(jMenuItem8);

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LESS, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setText("Fast preview");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                previewM(evt);
            }
        });
        generateMenu.add(jMenuItem7);

        jMenuItem4.setText("Settings");
        generateMenu.add(jMenuItem4);

        jMenuItem14.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem14.setText("Generate Sequence");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem14ActionPerformed(evt);
            }
        });
        generateMenu.add(jMenuItem14);

        menuBar.add(generateMenu);

        jMenu2.setBackground(new java.awt.Color(187, 203, 209));
        jMenu2.setText("Export");

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Export image");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportM(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem13.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem13.setText("Export svg");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem13ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem13);

        jMenu1.setText("Scene export");

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setText("Sunflow");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sceneExportM(evt);
            }
        });
        jMenu1.add(jMenuItem9);

        jMenu2.add(jMenu1);

        menuBar.add(jMenu2);

        tutMenu.setBackground(new java.awt.Color(187, 203, 209));
        tutMenu.setText("Tutorials");
        menuBar.add(tutMenu);

        helpMenu.setBackground(new java.awt.Color(187, 203, 209));
        helpMenu.setText("Help");

        contentsMenuItem.setText("Contents");
        contentsMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                contentsMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showAbout(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
        );

        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }// </editor-fold>//GEN-END:initComponents
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Quit Konte?", "", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION)
                {
            this.dispose();
            System.exit(0);
        }
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void generate(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_generate
        generatePic();
    }//GEN-LAST:event_generate

    private void generateM(javax.swing.event.MenuKeyEvent evt) {//GEN-FIRST:event_generateM
        generatePic();
    }//GEN-LAST:event_generateM

    private void generSM(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_generSM
        generateToSize();
    }//GEN-LAST:event_generSM

    private void saveAsM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsM
        saveWithName();
    }//GEN-LAST:event_saveAsM

    private void closeM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeM
        closeTab();
    }//GEN-LAST:event_closeM

    private void exportM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportM
        export();
    }//GEN-LAST:event_exportM

    private void openM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openM
        makeOpenDialog();
    }//GEN-LAST:event_openM

    private void newM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newM
        addUntitledTab();
    }//GEN-LAST:event_newM

    private void saveMM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMM
        save();
    }//GEN-LAST:event_saveMM

    private void generateMM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateMM
        generatePic();
    }//GEN-LAST:event_generateMM

    private void generSizeM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generSizeM
        generateToSize();
    }//GEN-LAST:event_generSizeM

    private void previewM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewM
        generatePic(-1, -1, 500,RenderType.IMAGE, null);
    }//GEN-LAST:event_previewM

    private void cancelMM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelMM
        controller.addTask(new Controller.TaskInfo(
                Controller.Task.CANCEL, new Object[]{}));
    }//GEN-LAST:event_cancelMM

    private void showAbout(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAbout
        showAbout();
    }//GEN-LAST:event_showAbout

    private void sceneExportM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sceneExportM
        controller.addTask(new Controller.TaskInfo(
                Controller.Task.EXPORT_SCENE, new Object[]{AbstractExporterBase.Type.SUNFLOW}));
    }//GEN-LAST:event_sceneExportM

    private void messagesList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_messagesList1MouseClicked
        if (evt.getClickCount() < 2)
        {
            return;
        }
        System.out.println("double");
        JFrame d = new JFrame();

        JList l = new JList(((JList) evt.getSource()).getModel());
        d.add(new JScrollPane(l));
        d.setVisible(true);
        d.setSize(500, 600);
        d.pack();
    }//GEN-LAST:event_messagesList1MouseClicked

    private void undoItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoItemActionPerformed
        EditViewCombo ev = (EditViewCombo) tabs.getSelectedComponent();
        ev.undo();
    }//GEN-LAST:event_undoItemActionPerformed

    private void redoItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoItemActionPerformed
        EditViewCombo ev = (EditViewCombo) tabs.getSelectedComponent();
        ev.redo();
    }//GEN-LAST:event_redoItemActionPerformed
    private ReplaceDialog repDi;

    private void replaceItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceItemActionPerformed
        if (repDi == null)
        {
            repDi = new ReplaceDialog();
        }
        repDi.setTarget(((EditViewCombo) tabs.getSelectedComponent()).edit);
        repDi.setVisible(true);
}//GEN-LAST:event_replaceItemActionPerformed

    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
    // TODO add your handling code here:
    }//GEN-LAST:event_deleteMenuItemActionPerformed

    private void contentsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentsMenuItemActionPerformed
        showHelp("all");
    }//GEN-LAST:event_contentsMenuItemActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        openColSpaceEditFrame();
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        openPathEditFrame();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jButton3cancelMM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3cancelMM
        generatePic(-1, -1, 500,RenderType.IMAGE, null);
    }//GEN-LAST:event_jButton3cancelMM

    private void jButton4cancelMM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4cancelMM
        save();
    }//GEN-LAST:event_jButton4cancelMM

    private void jButton5cancelMM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5cancelMM
        export();
    }//GEN-LAST:event_jButton5cancelMM

    private void jButton6cancelMM(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6cancelMM
        addUntitledTab();
    }//GEN-LAST:event_jButton6cancelMM

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        pickColor();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        importFromSvg();
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        exportSvg();
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        generateSequence();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void showAbout()
    {
        new KonteAboutDialog().setVisible(true);

    }

    void generateToSize()
    {
        if (generDia == null)
        {
            generDia = new GenerateDialog(props);
            generDia.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (evt.getPropertyName().equals("GenerateToSize"))
                    {
                        props = (Properties)evt.getNewValue();
                        generateToSize0();
                    }
                }
            });
        }
        this.generDia.setVisible(true);
    }
    private void generateToSize0()
    {
        int width = Integer.parseInt(props.getProperty("png-width"));
        int height = Integer.parseInt(props.getProperty("png-height"));
        generatePic(width, height,-1,RenderType.IMAGE, null);
    }

    void generatePic()
    {
        generatePic(-1, -1, -1, RenderType.IMAGE, null);
    }

    void generatePic(int width, int height, long maxtime, RenderType imgTyp, SeqRecorder seq)
    {
        EditViewCombo ev = ((EditViewCombo)tabs.getSelectedComponent());
        if (ev.getView().getIcon() != null)
        {
            if (width == -1 || (height == -1))
                incrementVariationKey();
            if (width == -1) width = ev.getView().getIcon().getIconWidth();
            if (height == -1) height = ev.getView().getIcon().getIconHeight();
        }
        else
        {
            while(ev.getView().getWidth() <= 0)
            {
                org.konte.misc.Func.sleep(10);
            }
            if (width == -1) width = ev.getView().getWidth();
            if (height == -1) height = ev.getView().getHeight();
        }
        ev.setRandomFeed(synchronizeVariationKey());
        controller.addTask(new Controller.TaskInfo(
                Controller.Task.RENDER,
                new Object[]{width, height, tabs.getSelectedComponent(), maxtime,
                imgTyp, seq}));
    }

    void generateSequence()
    {
        SeqDialogFrame seqfr = new SeqDialogFrame(props);
        seqfr.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equals("GenerateSequence"))
                {
                    props = (Properties)evt.getNewValue();
                    generateSequence0();
                }
            }
        });
        seqfr.setVisible(true);
    }

    void generateSequence0()
    {
        int width = Integer.parseInt(props.getProperty("seq_width"));
        int height = Integer.parseInt(props.getProperty("seq_height"));
        int freq = 1000/Integer.parseInt(props.getProperty("seq_frequency"));
        int flags = 0;
        Object[] sett = new Object[] {
            1, "seq_is_phase_generate",
            2, "seq_is_phase_final"
        };
        for(int i = 0; i < sett.length; i+=2)
        {
            String prop = props.getProperty((String)sett[i+1]);
            if (prop != null && prop.toUpperCase().equals("TRUE"))
            {
                flags |= (Integer)sett[i];
            }
        }
        String prefix = props.getProperty("seq_prefix");
        SeqRecorder seqReq = new SeqRecorder(prefix);
        seqReq.setFlags(flags);
        seqReq.setFrequency(freq);
        File file = new File(seqReq.getPrefix());
        file.delete();
        if (file.mkdir() || file.isDirectory())
            seqReq.setPrefix(seqReq.getPrefix() + File.separator + file.getName());
        Runtime.sysoutln("Saving sequence as: " + seqReq.getPrefix());
        generatePic(width, height, -1, RenderType.SEQUENCE, seqReq);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                final Ui ui = new Ui();
                ui.setVisible(true);
                Runtime.stateServer.setListener(new Observer()
                {
                    public void update(Observable o, Object arg)
                    {
                        ui.addMessage((String)arg);
                    }
                });
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu generateMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem redoItem;
    private javax.swing.JMenuItem replaceItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JMenu tutMenu;
    private javax.swing.JMenuItem undoItem;
    // End of variables declaration//GEN-END:variables
}
