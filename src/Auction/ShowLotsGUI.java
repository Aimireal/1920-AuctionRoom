package Auction;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.transaction.*;

import net.jini.core.transaction.server.*;
import net.jini.core.lease.Lease;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;

import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;
import javax.swing.*;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.rmi.RemoteException;


public class ShowLotsGUI extends JFrame implements RemoteEventListener
{
    private JPanel panelShowLots;
    private JScrollPane scrPanLots;
    private JList<String> listLots;
    private JButton btnView;
    private JButton btnSellLot;
    private JButton btnLogin;
    private JButton btnRefresh;
    private JLabel lblAuctionRoom;

    public AuctionItem auctionLot;
    public AuctionItem lotsTemplate = new AuctionItem();
    public AuctionLotQueue queueTemplate = new AuctionLotQueue();

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private boolean loggedIn = false; //This can be used to check whether someone is logged in
    private String loggedUsrName; //Might add into UI to show who is logged in

    private String currentLotInfo; //String to store the currently selected lot information
    private int lotIndex = 0; //Int to store the index of the selected lot to pull into our buy dialog


    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new ShowLotsGUI("AuctionRoom");
            frame.setVisible(true);
        });
    }


    public ShowLotsGUI(String title)
    {
        super();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.setContentPane(panelShowLots);
        this.pack();

        //Refresh lotList on focus
        this.addWindowFocusListener(new WindowFocusListener()
        {
            @Override
            public void windowGainedFocus(WindowEvent windowEvent)
            {
                viewLots();
            }

            @Override
            public void windowLostFocus(WindowEvent windowEvent)
            {
                //Do nothing
            }
        });

        //Find TransactionManager
        tranMan = SpaceUtils.getManager(SpaceUtils.host);
        if (tranMan == null)
        {
            System.err.println("TransactionManager not found on " + SpaceUtils.host);
        } else
        {
            System.out.println("TransactionManager found");
        }

        //Find JavaSpace
        js = SpaceUtils.getSpace(SpaceUtils.host);
        if (js == null)
        {
            System.err.println("JavaSpace not found on " + SpaceUtils.host);
        } else
        {
            System.out.println("JavaSpace found");
        }
        setupGUI();

        //Button methods
        viewLotButton();
        sellButton();
        loginButton();
        refreshButton();
    }


    private void setupGUI()
    {
        try
        {
            //Create Stub
            Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(), false, true);
            try
            {
                stub = (RemoteEventListener) exporter.export(this);
                AuctionItem lotsTemplate = new AuctionItem();
                js.notify(lotsTemplate, null, this.stub, Lease.FOREVER, null);
            } catch (Exception e)
            {
                System.err.println("Failed to setup Exporter: " + e);
            }

            //Setting up lot display
            listLots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            //List Selection
            listLots.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent listSelectionEvent)
                {
                    if(!listSelectionEvent.getValueIsAdjusting())
                    {
                        //Get selected rows item number (First value before first pipe)
                        //ToDo: Explore possibility of making Array of values to use for getting ID, as we might not want to display lot numbers
                        String lotString = listLots.getSelectedValue();
                        if(lotString != null)
                        {
                            String[] parts = lotString.split("\\|");
                            String trimmed = parts[0].trim();
                            lotIndex = Integer.parseInt(trimmed);
                            currentLotInfo = listLots.getSelectedValue();
                        }

                    }
                }
            });
        } catch (Exception e)
        {
            System.err.println("Error setting up Lots display " + e);
        }
        viewLots();
    }


    public void viewLots()
    {
        //Create Transaction so we can pull all lots without committing
        try
        {
            Transaction.Created trc = null;
            try
            {
                trc = TransactionFactory.create(tranMan, SpaceUtils.TWO_SECONDS);
            }catch (Exception e)
            {
                System.out.print("Failed to create Transaction");
            }
            Transaction txn = trc.transaction;

            //Adding items to JList
            DefaultListModel<String> listModel = new DefaultListModel<>();
            try
            {
                boolean searching = true;
                while(searching)
                {
                    lotsTemplate.lotExpired = false;
                    AuctionItem item = (AuctionItem)js.takeIfExists(lotsTemplate, txn, SpaceUtils.TWO_SECONDS);
                    if(item != null && !item.lotExpired)
                    {
                        System.out.println("Found something: " + item.lotTitle);
                        String lotInformation = item.lotNum +
                                " | " + item.lotTitle +
                                " | Description: " + item.lotDesc +
                                " | Seller: " + item.lotSellerID +
                                " | Current Bid: £" + item.lotPrice +
                                " | Highest Bidder: " + item.lotHighestBidder +
                                " | Buy Now Price: £" + item.lotBuyNowPrice;
                        listModel.addElement(lotInformation);
                    } else
                    {
                        searching = false;
                    }
                }
                listLots.setModel(listModel);

                //Enable/Disable based on if we have any values returned
                if(listModel.isEmpty())
                {
                    btnView.setEnabled(false);
                } else
                {
                    btnView.setEnabled(true);
                }
            }catch (Exception e)
            {
                System.err.println("Unable to add items");
                txn.abort();
            }
        }catch (Exception e)
        {
            System.err.println("Transaction setup Failed " + e);
        }
    }


    private void viewLotButton()
    {
        btnView.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if(!loggedIn)
                {
                    JOptionPane.showMessageDialog(null, "You are not logged in, please log in first");
                } else
                {
                    //Run the SellGUI class passing in our selected item details and logged in username
                    JDialog dialog = PurchaseGUI.main(lotIndex, currentLotInfo, loggedUsrName);
                }
            }
        });
    }


    private void sellButton()
    {
        btnSellLot.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (!loggedIn)
                {
                    JOptionPane.showMessageDialog(null, "You are not logged in, please log in first");
                } else
                {
                    JDialog dialog = SellGUI.main(loggedUsrName);
                }
            }
        });
    }


    private void loginButton()
    {
        btnLogin.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if(loggedIn)
                {
                    JOptionPane.showMessageDialog(null, "You are already logged in, would you like to log out?");
                    loggedUsrName = "";
                    loggedIn = false;

                    lblAuctionRoom.setText("AuctionRoom");
                } else
                {
                    //Run the AccountLoginGUI class and return the loggedIn boolean from there using Modality
                    AccountLoginGUI dialog = new AccountLoginGUI("AuctionRoom", loggedUsrName);
                    dialog.setVisible(true);
                    loggedIn = dialog.loggedIn;
                    loggedUsrName = dialog.loggedAs;

                    lblAuctionRoom.setText("AuctionRoom - Logged in as " + loggedUsrName);
                }
            }
        });
    }


    private void refreshButton()
    {
        btnRefresh.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    viewLots();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void notify(RemoteEvent remoteEvent)
    {
        //Find lots to notify based on the user
        AuctionItem template = new AuctionItem();
        template.lotHighestBidder = loggedUsrName;
        template.lotExpired = true;
        template.lotNotified = false;

        try
        {
            AuctionItem notifyLot = (AuctionItem)js.readIfExists(template, null, SpaceUtils.FIVE_SECONDS);
            if(notifyLot != null)
            {
                try
                {
                    if(loggedUsrName.equals(notifyLot.lotHighestBidder))
                    {
                        JOptionPane.showMessageDialog(null, "Well done " + notifyLot.lotHighestBidder + " you won the auction '" +
                                notifyLot.lotTitle + "' for £" +
                                notifyLot.lotPrice + ", please pay for the item as soon as possible");
                        setNotified(notifyLot.lotNum);
                    } else
                    {
                        System.err.println("I'd let you know, but you didn't buy this one");
                    }
                }catch (Exception e)
                {
                    System.err.println("Failed to show message: " + e);
                }
            } else
            {
                System.err.println("NotifyLot returned null");
            }
        }catch (Exception e)
        {
            System.err.println("Lots notify failed: " + e);
        }
        //Refresh the list
        viewLots();
    }

    public void setNotified(int lotNum) throws TransactionException, UnusableEntryException, RemoteException, InterruptedException
    {
        //Set the notified on the auction
        Transaction.Created trc = null;
        try
        {
            trc = TransactionFactory.create(tranMan, SpaceUtils.TWO_SECONDS);
        } catch (Exception e)
        {
            System.err.println("Failed to create Transaction");
        }

        Transaction txn = trc.transaction;

        AuctionItem template = new AuctionItem();
        template.lotNum = lotNum;

        auctionLot = (AuctionItem) js.readIfExists(template, txn, SpaceUtils.ONE_SECOND);
        if (auctionLot != null)
        {
            js.take(auctionLot, txn, SpaceUtils.ONE_SECOND);
            auctionLot.lotNotified = true;
            js.write(auctionLot, txn, Lease.FOREVER);

            System.out.println("Successfully set auction to notified");
            txn.commit();
        } else
        {
            System.err.println("Failed to set lot " + lotNum + " to notified");
            txn.abort();
        }
    }


}