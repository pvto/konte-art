
package org.konte.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author pt
 */
public class MyJFrame extends JFrame {
    private FileNameExtensionFilter fname;
    protected Properties props;

    public void setPath(File f) {
        this.setPath(f.getAbsolutePath().replaceFirst(f.getName() + "$", ""));
    }
    public void setPath(String path) { 
        props.setProperty("dir", path);
        System.setProperty("konte.workdir", path);
    }

    public String getPath() {
        return props.getProperty("dir", "./");
    }
    
    private JFileChooser fc;    
    public void setFileChooser() {
        if (fc == null) {
            File file = new File(getPath().replaceFirst("/$", ""));
            if (file == null)
                file = new File(".");
            fc = new JFileChooser(file);
            fc.setFileFilter(fname);
            
        }        
    }    
    public JFileChooser getFc() { return fc; }
    
    public MyJFrame(FileNameExtensionFilter fname) {
        this.fname = fname;

        
        this.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                //System.out.println(e);
            }
        });
        this.addWindowListener(new WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                props = new Properties();
                String pname = getPropertiesName();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(new File(pname)));
                    props.load(br);
                    br.close();
                } catch(Exception ex) {
                    System.out.println("Properties not found: " + pname);
//                    ex.printStackTrace();
                }
                String w = props.getProperty("w-width");
                if (w == null) {
                    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                    props.put("w-width", d.width+"");
                    props.put("w-height", d.height+"");
                    props.put("w-x", "0");
                    props.put("w-y", "0");
                }
                MyJFrame.this.setBounds(Integer.parseInt(props.get("w-x").toString()), Integer.parseInt(props.get("w-y").toString()),
                        Integer.parseInt(props.get("w-width").toString()), Integer.parseInt(props.get("w-height").toString()));
                setFileChooser();    
                finalizeProps();
            }

            public void windowClosing(WindowEvent e) {
                saveProps();
            }

        });
    }
    protected void finalizeProps() {
    }
    
    public String getPropertiesName() {
        return this.getClass().getName()+".properties";
    }

    public void setProps(Properties props) {
        this.props = props;
    }
    
    public void saveProps() {
        String pname = getPropertiesName();
        FileWriter wr = null;
        Rectangle r = this.getBounds();
        props.put("w-width", r.width+"");
        props.put("w-height", r.height+"");
        props.put("w-x", r.x+"");
        props.put("w-y", r.y+"");
        try {
            wr = new FileWriter(new File(getPropertiesName()));
            props.store(wr,"konte ui properties file");
            wr.close();
        } catch(Exception ex) {
            System.out.println("Properties not written: " + pname);
            ex.printStackTrace();
        }
    }
}
