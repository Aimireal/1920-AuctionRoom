package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class PurchaseGUI extends JFrame implements RemoteEventListener
{
    private JButton btnCancel;
    private JButton btnBuyNow;
    private JTextField txtFldBid;
    private JButton btnPlaceBid;
    private JLabel lblTitle;
    private JLabel lblDescription;
    private JLabel lblPrice;
    private JLabel lblBuyNowPrice;
    private JTextField txtFldBuyNowPrice;

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
