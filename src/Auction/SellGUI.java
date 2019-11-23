package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class SellGUI extends JFrame implements RemoteEventListener
{
    private JButton btnCancel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JButton btnSubmitSale;
    private JLabel lblSellTitle;
    private JLabel lblLotTitle;
    private JLabel lblLotSale;
    private JLabel lblStartingBid;
    private JLabel lblBuyNowPrice;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new SellGUI("AuctionRoom");
            frame.setVisible(true);
        });
    }


    public SellGUI(String title)
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
