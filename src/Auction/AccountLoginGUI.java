package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class AccountLoginGUI extends JDialog implements RemoteEventListener
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


    public static JDialog main()
    {
        SwingUtilities.invokeLater(() ->
        {
            JDialog dialog = new AccountLoginGUI("AuctionRoom");
            dialog.setVisible(true);
        });
        return null;
    }


    private AccountLoginGUI(String title)
    {
        super();

        //Basic setup
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.setContentPane(panelLogin);
        this.pack();
        this.setModalityType(DEFAULT_MODALITY_TYPE);

        //Find TransactionManager
        tranMan = SpaceUtils.getManager("waterloo");
        if (tranMan == null)
        {
            System.err.println("TransactionManager not found on LocalHost");
        } else
        {
            System.out.println("TransactionManager found");
        }

        //Find JavaSpace
        js = SpaceUtils.getSpace("waterloo");
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
                stub = (RemoteEventListener) exporter.export((Remote) this);
                AccountItem accountTemplate = new AccountItem();
                js.notify(accountTemplate, null, this.stub, Lease.FOREVER, null);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    private void loginButton()
    {
        btnLogin.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    boolean detailsEntered = userNamePassEntered();
                    if(detailsEntered)
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


    private void cancelButton()
    {
        btnCancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                dispose();
            }
        });
    }


    private void createButton()
    {
        btnCreateAccount.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    boolean detailsEntered = userNamePassEntered();
                    if(detailsEntered)
                    {
                        //Get account details and try getting a matching object
                        AccountItem accountTemplate = new AccountItem();
                        accountTemplate.accountName = txtfldUsername.getText();

                        userAccount = (AccountItem)js.readIfExists(accountTemplate, null, FIVE_HUNDRED_MILLS);
                        if(userAccount == null)
                        {
                            createUser();
                        } else
                        {
                            JOptionPane.showMessageDialog(null, "An account with this name exists, please choose another");
                        }
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    private void createUser()
    {
        try
        {
            //Create Transaction
            Transaction.Created trc = null;
            try
            {
                trc = TransactionFactory.create(tranMan, Lease.FOREVER);
            }catch (Exception e)
            {
                System.err.println("Failed to create Transaction");
            }

            Transaction txn = trc.transaction;
            AccountItem template = new AccountItem();
            try
            {
                //Add a new user to the space
                AccountQueue queue = (AccountQueue)js.take(template, txn, FIVE_SECONDS);
                int counter = queue.counter;
                String username = txtfldUsername.getText();
                String password = txtfldPassword.getText();

                AccountItem newUser = new AccountItem(counter, username, password);
                js.write(newUser, txn, Lease.FOREVER);
                queue.incrementCounter();
                txn.commit();

                System.out.print("Successfully Created User");
            }catch (Exception e)
            {
                txn.abort();
                System.err.println("Failed to Create User");
                System.err.println("Username " + txtfldUsername.getText() + " Password " + txtfldPassword.getText());
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private boolean userNamePassEntered()
    {
        //Check that a username and password was entered
        String userSpecName = txtfldUsername.getText();
        String userSpecPass = txtfldPassword.getText();

        if(userSpecName == null || userSpecName.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "You must enter in a username");
            return false;
        } else
        {
            if(userSpecPass == null || userSpecPass.isEmpty())
            {
                JOptionPane.showMessageDialog(null, "You must enter in a password");
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
        //ToDo: See if needed, otherwise remove RemoteEventListener. Might be handy to send the login event to main form?
    }
}
