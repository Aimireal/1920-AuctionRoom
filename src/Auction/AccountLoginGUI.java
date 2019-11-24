package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class AccountLoginGUI extends JFrame implements RemoteEventListener
{
    private JPanel panelLogin;
    private JTextField txtfldUsername;
    private JTextField txtfldPassword;
    private JButton btnCreateAccount;
    private JButton btnCancel;
    private JButton btnLogin;

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private AccountItem userAccount;

    private static int FIVE_HUNDRED_MILLS = 500;
    private static int FIVE_SECONDS = 5000;


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

        //Basic setup
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.setContentPane(panelLogin);
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

        //Account login and creation methods
        loginButton();
        createButton();
        cancelButton();
    }


    private void setupGUI()
    {
        try
        {
            //Create Stub/Exporter
            Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(), false, true);
            try
            {
                stub = (RemoteEventListener) exporter.export(this);
                AuctionItem lotsTemplate = new AuctionItem();
                js.notify(lotsTemplate, null, this.stub, Lease.FOREVER, null);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    public void loginButton()
    {
        btnLogin.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    boolean verifiedDetails = userNamePassEntered();
                    if(verifiedDetails)
                    {
                        //Get account details and try getting a matching object
                        AccountItem accountTemplate = new AccountItem();
                        accountTemplate.accountName = txtfldUsername.getText();
                        accountTemplate.accountPassword = txtfldPassword.getText();

                        userAccount = (AccountItem)js.readIfExists(accountTemplate, null, FIVE_HUNDRED_MILLS);
                        if(userAccount == null)
                        {
                            JOptionPane.showMessageDialog(null, "No account found or wrong details");
                        } else
                        {
                            //ToDo: Send a boolean of true for usrLoggedIn if we are successful to ShowLotsGUI and usrID/name
                            boolean loggedIn = true;
                            int usrID = userAccount.accountNum;
                            String usrName = userAccount.accountName;
                        }
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    public void createButton()
    {
        btnCreateAccount.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });
    }


    public void cancelButton()
    {
        btnCancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });
    }


    public boolean userNamePassEntered()
    {
        //Check that a username and password was entered
        String userSpecName = txtfldUsername.getText();
        String userSpecPass = txtfldPassword.getText();

        if(userSpecName == null || userSpecName.isEmpty())
        {
            System.err.println("You must enter in a username to continue");
            return false;
        } else
        {
            if(userSpecPass == null || userSpecPass.isEmpty())
            {
                System.err.println("You must enter in a password to continue");
                return false;
            } else
            {
                return true;
            }
        }
    }


    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException
    {

    }
}
