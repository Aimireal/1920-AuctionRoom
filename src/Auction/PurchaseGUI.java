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
import java.rmi.RemoteException;


public class PurchaseGUI extends JDialog implements RemoteEventListener
{
    private JPanel panelBuyLots;
    private JTextField txtFldBid;
    private JTextField txtFldBuyNowPrice;
    private JButton btnCancel;
    private JButton btnBuyNow;
    private JButton btnPlaceBid;
    private JLabel lblLotDesc;
    private JLabel lblLotTitle;
    private JLabel lblCurrentBid;

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private AuctionItem auctionLot;
    public AuctionItem lotsTemplate = new AuctionItem();

    public static int curLotNum = 0; //This might not be needed, or we can use this to pull details instead of passing on launch
    public static String curUser = "PLACEHOLDER"; //Currently logged in user
    public static String curLotTitle = "TITLE";
    public static String curLotDesc = "DESC";
    public String curLotBidPrice = "0";
    public String curLotBuyPrice = "0";

    public static int currentLotIndex;
    public static String currentLotInfo = "PLACEHOLDER";

    private static int FIVE_HUNDRED_MILLS = 500;
    private static int TWO_SECONDS = 2000;
    private static int FIVE_SECONDS = 5000;


    public static JDialog main(int lotIndex, String lotInfo, String loggedUser)
    {
        SwingUtilities.invokeLater(() ->
        {
            JDialog dialog = new PurchaseGUI();
            dialog.setVisible(true);
        });

        //Pull through the lot information
        currentLotIndex = lotIndex;
        currentLotInfo = lotInfo;
        System.err.println(currentLotIndex);

        curUser = loggedUser;
        return null;
    }


    public PurchaseGUI()
    {
        super();

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);
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
        pullInformation();

        //Setup buttons and functions
        bidButton();
        buyButton();
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
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void pullInformation()
    {
        try
        {
            Transaction.Created trc = null;
            try
            {
                trc = TransactionFactory.create(tranMan, TWO_SECONDS);
            }catch (Exception e)
            {
                System.out.print("Failed to create Transaction");
            }
            Transaction txn = trc.transaction;

            lotsTemplate.lotNum = currentLotIndex;
            AuctionItem item = (AuctionItem)js.takeIfExists(lotsTemplate, txn, TWO_SECONDS);

            //Set local info
            curLotNum = currentLotIndex;
            curLotTitle = item.lotTitle;
            curLotDesc = item.lotDesc;
            curLotBidPrice = item.lotPrice;
            curLotBuyPrice = item.lotBuyNowPrice;

            //Set GUI info
            lblLotTitle.setText(curLotTitle);
            lblLotDesc.setText(curLotDesc);
            lblCurrentBid.setText("£" + curLotBidPrice);
            txtFldBuyNowPrice.setText("£" + curLotBuyPrice);

            txn.abort();
        }catch (Exception e)
        {
            System.err.println("Unable to Search");
        }
    }


    private void bidButton()
    {
        btnPlaceBid.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                //Create transaction
                Transaction.Created trc = null;
                try
                {
                    trc = TransactionFactory.create(tranMan, FIVE_SECONDS);
                }catch (Exception e)
                {
                    System.err.println("Failed to create Transaction");
                }

                //Check if value has been given for bid
                if(txtFldBid.getText()== null || txtFldBid.getText().isEmpty())
                {
                    System.err.println("You must enter a bid");
                    System.exit(1);
                }

                //Place a bid based on the index on the current lot number
                Transaction txn = trc.transaction;
                try
                {
                    AuctionItem template = new AuctionItem();
                    template.lotNum = currentLotIndex;

                    auctionLot = (AuctionItem)js.readIfExists(template, txn, FIVE_HUNDRED_MILLS);
                    double userBid = Double.parseDouble(txtFldBid.getText());
                    double lotPrice = Double.parseDouble(auctionLot.lotPrice);

                    if(lotPrice < userBid)
                    {
                        try
                        {
                            js.take(auctionLot, txn, FIVE_HUNDRED_MILLS);
                            auctionLot.lotHighestBidder = curUser;
                            auctionLot.lotBids++;
                            auctionLot.lotPrice = String.valueOf(userBid);

                            js.write(auctionLot, txn, Lease.FOREVER);
                            JOptionPane.showMessageDialog(null, "You are the highest bidder");
                            txn.commit();
                        }catch (NumberFormatException e)
                        {
                            JOptionPane.showMessageDialog(null, "Something went wrong, check bids value is right and try again");
                        }
                    } else
                    {
                        JOptionPane.showMessageDialog(null, "Bid too low, please increase bid value");
                        txn.abort();
                    }
                }catch (Exception e)
                {
                   e.printStackTrace();
                }
            }
        });
    }


    private void buyButton()
    {
        btnBuyNow.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                //Create transaction
                Transaction.Created trc = null;
                try
                {
                    trc = TransactionFactory.create(tranMan, FIVE_SECONDS);
                }catch (Exception e)
                {
                    System.err.println("Failed to create Transaction");
                }

                //Check if value has been given for buy-it-now
                if(txtFldBuyNowPrice.getText()== null || txtFldBuyNowPrice.getText().isEmpty())
                {
                    System.err.println("You must enter a bid");
                    return;
                }

                //Try purchase
                Transaction txn = trc.transaction;
                try
                {
                    AuctionItem template = new AuctionItem();
                    template.lotNum = currentLotIndex;

                    auctionLot = (AuctionItem)js.readIfExists(template, txn, FIVE_HUNDRED_MILLS);
                    double buyNowPrice = Double.parseDouble(txtFldBuyNowPrice.getText());
                    double lotPrice = Double.parseDouble(auctionLot.lotPrice);

                    if(lotPrice <= buyNowPrice)
                    {
                        try
                        {
                            js.take(auctionLot, txn, FIVE_HUNDRED_MILLS);
                            auctionLot.lotHighestBidder = curUser;
                            auctionLot.lotPrice = String.valueOf(buyNowPrice);
                            auctionLot.lotExpired = true;

                            js.write(auctionLot, txn, Lease.FOREVER);
                            JOptionPane.showMessageDialog(null, "You have purchased this lot. Please pay as soon as possible");
                            txn.commit();
                        }catch (NumberFormatException e)
                        {
                            JOptionPane.showMessageDialog(null, "Something went wrong. Please try again");
                        }
                    } else
                    {
                        JOptionPane.showMessageDialog(null, "Buy now too low. This shouldn't happen");
                        txn.abort();
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


    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException
    {
        AuctionItem template = new AuctionItem();
        template.lotHighestBidder = curUser;
        template.lotExpired = true;

        try
        {
            AuctionItem notifyLot = (AuctionItem)js.readIfExists(template, null, FIVE_SECONDS);
            JOptionPane.showMessageDialog(null, "Well done " + curUser + " you won the auction for £" +
                    curLotBidPrice + ", please pay for the item as soon as possible");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
