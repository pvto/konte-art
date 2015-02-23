
package org.konte.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author pvto
 */
public abstract class EditFrame extends MyJFrame {

    protected File file;
    public abstract String getText0();

    public EditFrame(FileNameExtensionFilter fname)
    {
        super(fname);
    }
    
    public void save()
    {

        setFileChooser();
        if (getPath() != null)
        {
            getFc().setCurrentDirectory(new File(getPath()));
        }
        if (getFile() != null)
        {
            getFc().setSelectedFile(file);
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
                setFile(f);
                this.setPath(f.getAbsolutePath().replaceFirst(f.getName() + "$", ""));
                FileWriter fw = null;
                try {
                    fw = new FileWriter(getFile());
                    fw.write(getText0());
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
                this.setTitle(f.getName());
            }
        }

    }

    private File getFile()
    {
        return file;
    }

    private void setFile(File f)
    {
        file = f;
    }
}
