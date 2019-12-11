package Auction;

import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

public class AccountLoginGUI extends JDialog
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
    private static int TWO_SECONDS = 2000;
    private static int FIVE_SECONDS = 5000;

    public boolean loggedIn = false;
    public String loggedAs;


    public static JDialog main()
    {
        SwingUtilities.invokeLater(() ->
        {
            JDialog dialog = new AccountLoginGUI("AuctionRoom", "AuctionRoom");
            dialog.setVisible(true);
        });
        return null;
    }


    public AccountLoginGUI(String auctionRoom, String title)
    {
        //super();
        super((Frame) null, true);
        setModalityType(ModalityType.APPLICATION_MODAL);

        //Basic setup
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.setContentPane(panelLogin);
        this.pack();
        this.setModalityType(DEFAULT_MODALITY_TYPE);

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
        //Disable and enable buttons plus refresh lots display on focus active
        ArrayList<JButton> allButtons = new ArrayList<>();
        allButtons.add(btnCancel);
        allButtons.add(btnCreateAccount);
        allButtons.add(btnLogin);

        this.addWindowFocusListener(new WindowFocusListener()
        {
            @Override
            public void windowGainedFocus(WindowEvent windowEvent)
            {
                for(JButton button : allButtons)
                {
                    button.setEnabled(true);
                }
            }

            @Override
            public void windowLostFocus(WindowEvent windowEvent)
            {
                for(JButton button : allButtons)
                {
                    button.setEnabled(false);
                }
            }
        });

        //Setup for button functions
        loginButton();
        createButton();
        cancelButton();
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
                            loggedIn = true;
                            loggedAs = userAccount.accountName;
                            JOptionPane.showMessageDialog(null, "Successfully logged in as " + loggedAs);
                            dispose();
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
            //Get the entered details
            userNamePassEntered();
            String username = txtfldUsername.getText();
            String password = txtfldPassword.getText();

            //Check if AuctionLotQueue object exists
            AuctionLotQueue accountTemplate = new AuctionLotQueue();
            AuctionLotQueue accountStatus = (AuctionLotQueue)js.read(accountTemplate, null, TWO_SECONDS);

            //If no AuctionLotQueue found return else create and add account
            if(accountStatus == null)
            {
                System.err.println("No AuctionLotQueue Found");
                dispose();
            } else
            {
                try
                {
                    //Attempt to add a new user to the Space
                    accountStatus.incrementCounter();
                    int counter = accountStatus.counter;
                    AccountItem newUser = new AccountItem(counter, username, password);
                    js.write(newUser, null, Lease.FOREVER);

                    System.out.println("Successfully created user");
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
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


}
