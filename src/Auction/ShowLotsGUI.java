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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.rmi.RemoteException;


public class ShowLotsGUI extends JFrame implements RemoteEventListener
{
    private JPanel panelShowLots;
    private JScrollPane scrpanLots;
    private JList listLots;
    private JButton btnView;
    private JButton btnSellLot;
    private JButton btnLogin;

    public AuctionItem lotsTemplate = new AuctionItem();
    public AuctionLotQueue queueTemplate = new AuctionLotQueue();

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private static int TWENTYFIVE_MILLS = 250;
    private static int TWO_SECONDS = 2000;
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

        //Basic setup
        setTitle("AuctionRoom");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        //Methods for specific stuff on GUI components
        setupGUI();
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
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            getContentPane().setLayout(null);
            {
                scrpanLots = new JScrollPane();
                getContentPane().add(scrpanLots);
                scrpanLots.setBounds(12, 12, 900, 500);
                {
                    ListModel lotModel = new DefaultComboBoxModel(new String[]{"No Lots Found"});
                    listLots = new JList();
                    scrpanLots.setViewportView(listLots);
                    listLots.setModel(lotModel);
                    listLots.setBounds(89, -31, 300, 400);
                }
            }

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
            System.err.println("Could not create Transaction " + e);
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
                    if (currentLot.lotExpired == true)
                    {
                        System.err.println(currentLot.lotTitle + " has expired.");
                        lotModel.addElement("[Expired] Items");
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
        listLots.setModel(lotModel);
    }


    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException
    {
        try
        {
            viewLots();
        } catch (CannotAbortException e)
        {
            e.printStackTrace();
        } catch (UnknownTransactionException e)
        {
            e.printStackTrace();
        }
    }

}