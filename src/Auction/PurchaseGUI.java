package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class PurchaseGUI extends JFrame implements RemoteEventListener
{
    private JPanel panelBuyLots;
    private JTextField txtFldBid;
    private JTextField txtFldBuyNowPrice;
    private JButton btnCancel;
    private JButton btnBuyNow;
    private JButton btnPlaceBid;

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;


    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new PurchaseGUI("AuctionRoom");
            frame.setVisible(true);
        });
    }


    public PurchaseGUI(String title)
    {
        super();

        //Basic setup
        setTitle("AuctionRoom: Sell Lots");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelBuyLots);
        this.pack();

        //Find TransactionManager
        tranMan = SpaceUtils.getManager("localhost");
        if (tranMan == null)
        {
            System.err.println("TransactionManager not found on LocalHost");
        } else
        {
            System.out.println("TransactionManager found");
        }

        //Find JavaSpace
        js = SpaceUtils.getSpace("localhost");
        if (js == null)
        {
            System.err.println("JavaSpace not found on LocalHost");
        } else
        {
            System.out.println("JavaSpace found");
        }

        setupGUI();
    }


    private void setupGUI()
    {
        try
        {

        } catch(Exception e)
        {

        }
    }


    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException
    {

    }
}
