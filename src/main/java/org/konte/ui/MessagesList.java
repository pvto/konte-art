
package org.konte.ui;

import java.awt.event.MouseListener;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


public class MessagesList extends JScrollPane {

    JList list;
    DefaultListModel data;
    public MessagesList() {
        super();
        data = new DefaultListModel();
        
        list = new JList(data);
        list.setPrototypeCellValue(".................................................");
        list.setFont(new java.awt.Font("Monospaced", 0, 10));
        list.setFixedCellHeight(11);
        this.setAutoscrolls(true);
        this.getViewport().setView(list);
        
    }

    private class AddMessage implements Runnable {
        private String msg;
        private AddMessage(String msg) {
            this.msg = msg;
        }
        public void run() {
            synchronized(MessagesList.this) {
                data.addElement(msg);
                if (data.getSize() > 60)
                    data.remove(0);
            }
            scrollDown();
        }
    }
    public synchronized void addMessage(String msg) {
        if (data.size() > 0 && data.get(data.size()-1).equals(msg))
            return;
        SwingUtilities.invokeLater(new AddMessage(msg));
    }

    public DefaultListModel getData() {
        return data;
    }

    @Override
    public synchronized void addMouseListener(MouseListener l) {
        //super.addMouseListener(l);
        list.addMouseListener(l);
    }

    private Runnable r = new Runnable() {
        public void run() {
            JScrollBar bar = MessagesList.this.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
            MessagesList.this.repaint();
        }
    };
    public void scrollDown() {
        scrollDown(true);
    }
    public void scrollDown(boolean invokeLater) {
        if (invokeLater)
            SwingUtilities.invokeLater(r);
        else
            r.run();
    }


}
