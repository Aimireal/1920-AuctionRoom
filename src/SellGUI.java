/*
    SellGUI - Creates and interface and allows user to sell lots
    ToDo: Most of this needs doing. Just using Gary's as a Template currently
 */

import net.jini.space.*;
import net.jini.core.lease.*;
import java.awt.*;
import javax.swing.*;

public class SellGUI extends JFrame
{
    private JavaSpace js;

    public SellGUI()
    {
        js = SpaceUtils.getSpace();
        if(js == null)
        {
            System.err.println("Failed to find JavaSpace");
            System.exit(1);
        }

        initComponents();
        this.setSize(300, 150);
    }

    private void initComponents()
    {
        setTitle("AuctionRoom: Sell lots");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
    }

    private void exitForm(java.awt.event.WindowEvent evt)
    {
        System.exit(0);
    }

    public static void main(String[] args)
    {
        new SellGUI().setVisible(true);
    }

}
