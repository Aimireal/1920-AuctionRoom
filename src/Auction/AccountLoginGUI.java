package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class AccountLoginGUI extends JFrame implements RemoteEventListener
{
    private JButton btnCreateAcccount;
    private JButton btnCancel;
    private JButton btnLogin;
    private JTextField txtfldUsername;
    private JTextField txtfldPassword;
    private JLabel lblUsername;
    private JLabel lblPassword;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new AccountLoginGUI("AuctionRoom");
            frame.setVisible(true);
        });
    }


    public AccountLoginGUI(String title)
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
