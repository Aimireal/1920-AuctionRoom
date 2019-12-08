package Auction;

import net.jini.core.entry.Entry;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;


public class ShowLotsGUI extends JFrame implements RemoteEventListener
{
    private JPanel panelShowLots;
    private JScrollPane scrPanLots;
    private JList listLots;
    private JButton btnView;
    private JButton btnSellLot;
    private JButton btnLogin;

    public AuctionItem lotsTemplate = new AuctionItem();
    public AuctionLotQueue queueTemplate = new AuctionLotQueue();

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private boolean loggedIn = false; //This can be used to check whether someone is logged in
    private String loggedUsrName; //Might add into UI to show who is logged in
    private String currentLotInfo; //String to store the currently selected lot information

    private static int TWENTYFIVE_MILLS = 250;
    private static int FIVE_SECONDS = 5000;


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

        //Button methods
        viewLotButton();
        sellButton();
        loginButton();
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
                js.notify((Entry) lotsTemplate, null, this.stub, Lease.FOREVER, null);
            } catch (Exception e)
            {
                System.err.println("Failed to setup Notify: " + e);
            }

            //Setting up lots display stuff
            listLots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            getContentPane().setLayout(null);
            {
                /*
                //Removed due to scrollPane being in GUI designer instead now
                scrPanLots = new JScrollPane();
                getContentPane().add(scrpanLots);
                scrPanLots.setBounds(12, 12, 900, 500);
                */
                {
                    ListModel lotModel = new DefaultComboBoxModel(new String[]{"No Lots Found"});
                    listLots = new JList();
                    scrPanLots.setViewportView(listLots);
                    listLots.setModel(lotModel);
                    listLots.setBounds(89, -31, 300, 400);
                }
            }

            //List Selection
            listLots.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent listSelectionEvent)
                {
                    if(!listSelectionEvent.getValueIsAdjusting())
                    {
                        currentLotInfo = listLots.getSelectedValue().toString();
                        System.out.println("Selected " + listLots.getSelectedValue().toString());
                    }
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void viewLots() throws CannotAbortException, RemoteException, UnknownTransactionException
    {
        //Create Transaction object
        Transaction.Created trc = null;
        try
        {
            trc = TransactionFactory.create(tranMan, FIVE_SECONDS);
        } catch (Exception e)
        {
            System.err.println("Failed to create Transaction");
        }
        Transaction txn = trc.transaction;

        //Add lots to the list in the GUI
        DefaultListModel lotModel = new DefaultListModel();
        try
        {
            AuctionLotQueue queue = (AuctionLotQueue) js.read(queueTemplate, txn, TWENTYFIVE_MILLS);
            for (int i = 0; i < queue.counter; i++)
            {
                lotsTemplate.lotNum = i;
                AuctionItem currentLot = (AuctionItem) js.readIfExists(lotsTemplate, txn, TWENTYFIVE_MILLS);

                if (currentLot == null)
                {
                    System.err.println("No lot found");
                } else
                {
                    if (currentLot.lotExpired)
                    {
                        System.err.println(currentLot.lotTitle + " has expired.");
                        lotModel.addElement("[Expired] " + currentLot.lotTitle);
                    } else
                    {
                        System.out.println(currentLot.lotTitle + " added to JList");
                        String addLotList =
                                "Lot Title: " + currentLot.lotTitle +
                                        "Lot Description: " + currentLot.lotDesc +
                                        " | Lot Seller: " + currentLot.lotSellerID +
                                        " | Current Bid: £" + currentLot.lotPrice +
                                        " | Highest Bidder: " + currentLot.lotHighestBidder +
                                        " | Buy it Now Price: £" + currentLot.lotBuyNowPrice;
                        lotModel.addElement(addLotList);
                    }
                }
            }
            txn.commit();
        } catch (Exception e)
        {
            System.err.println("Could not read Queue " + e);
            txn.abort();
        }
        if (lotModel.getSize() == 0)
        {
            lotModel.add(0, "No Lots Returned. Sell something or wait");
            System.err.println("No lots returned. Make sure at least one exists");
        }
    }


    private void viewLotButton()
    {
        btnView.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                JDialog dialog = PurchaseGUI.main(0, loggedUsrName);
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
                    JOptionPane.showMessageDialog(null, "You must be logged in to do this");
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
                } else
                {
                    //Run the AccountLoginGUI class and return the loggedIn boolean from there using Modality
                    AccountLoginGUI dialog = new AccountLoginGUI("AuctionRoom", loggedUsrName);
                    dialog.setVisible(true);
                    loggedIn = dialog.loggedIn;
                    loggedUsrName = dialog.loggedAs;
                }
            }
        });
    }


    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException
    {
        try
        {
            viewLots();
        } catch (CannotAbortException | UnknownTransactionException e)
        {
            e.printStackTrace();
        }
    }

}