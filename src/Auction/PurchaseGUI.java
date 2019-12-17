package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;
import org.apache.river.api.security.DelegateSecurityManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


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
    private JButton btnEndListing;
    private JList<String> JListBidHistory;
    private JScrollPane JScrPanHistory;

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;


    public AuctionItem auctionLot;
    public AuctionItem lotsTemplate = new AuctionItem();
    public BidItem bidsTemplate = new BidItem();

    public static int curLotNum = 0; //This might not be needed, or we can use this to pull details instead of passing on launch
    public static String curUser = "PLACEHOLDER"; //Currently logged in user
    public static String curLotTitle = "TITLE";
    public static String curLotDesc = "DESC";
    public String curLotBidPrice = "0";
    public String curLotBuyPrice = "0";

    public static int currentLotIndex;
    public static String currentLotInfo = "PLACEHOLDER";


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
        System.out.println(lotInfo);
        System.out.println(currentLotIndex);

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

        //Continue setup
        setupGUI();
        pullInformation();
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

        //Setup for button functions
        bidButton();
        buyButton();
        cancelButton();
        endButton();

        //Setup bid history list
        getBidHistory();
    }

    public void pullInformation()
    {
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

            lotsTemplate.lotNum = currentLotIndex;
            AuctionItem item = (AuctionItem)js.takeIfExists(lotsTemplate, txn, SpaceUtils.TWO_SECONDS);

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
                    trc = TransactionFactory.create(tranMan, SpaceUtils.TWO_SECONDS);
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

                try
                {
                    //Place a bid based on the index on the current lot number
                    Transaction txn = trc.transaction;

                    AuctionItem template = new AuctionItem();
                    template.lotNum = currentLotIndex;

                    auctionLot = (AuctionItem)js.readIfExists(template, txn, SpaceUtils.HALF_SECOND);
                    BigDecimal userBid = BigDecimal.valueOf(Long.parseLong(txtFldBid.getText()));
                    BigDecimal lotPrice = BigDecimal.valueOf(Long.parseLong(auctionLot.lotPrice));

                    if(userBid.compareTo(lotPrice) >= 1)
                    {
                        try
                        {
                            js.take(auctionLot, txn, SpaceUtils.HALF_SECOND);
                            auctionLot.lotHighestBidder = curUser;
                            auctionLot.lotPrice = String.valueOf(userBid);

                            js.write(auctionLot, txn, Lease.FOREVER);
                            JOptionPane.showMessageDialog(null, "You are the highest bidder");
                            txn.commit();
                            writeBidHistory();
                            dispose();
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
                    trc = TransactionFactory.create(tranMan, SpaceUtils.TWO_SECONDS);
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
                assert trc != null;
                Transaction txn = trc.transaction;
                try
                {
                    AuctionItem template = new AuctionItem();
                    template.lotNum = currentLotIndex;

                    auctionLot = (AuctionItem)js.readIfExists(template, txn, SpaceUtils.HALF_SECOND);
                    String cleaned = txtFldBuyNowPrice.getText().replaceAll("[^\\d.]", "");
                    System.out.println("Cleaned" + cleaned);
                    BigDecimal buyNowPrice = BigDecimal.valueOf(Long.parseLong(cleaned));
                    BigDecimal lotPrice = BigDecimal.valueOf(Long.parseLong(auctionLot.lotPrice));

                    if(buyNowPrice.compareTo(lotPrice) >= 0)
                    {
                        try
                        {
                            js.take(auctionLot, txn, SpaceUtils.HALF_SECOND);
                            auctionLot.lotHighestBidder = curUser;
                            auctionLot.lotPrice = String.valueOf(buyNowPrice);
                            auctionLot.lotExpired = true;

                            js.write(auctionLot, txn, Lease.FOREVER);
                            JOptionPane.showMessageDialog(null, "You have purchased this lot. Please pay as soon as possible");
                            txn.commit();

                            dispose();
                        }catch (NumberFormatException e)
                        {
                            JOptionPane.showMessageDialog(null, "Something went wrong. Please try again");
                        }
                    } else
                    {
                        JOptionPane.showMessageDialog(null, "Buy now too low. Transaction Aborted");
                        txn.abort();
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    private void endButton()
    {
        //Method to end listings early
        btnEndListing.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                //Get lot info and if current user and lot owner match enable End Listing button
                String[] parts = currentLotInfo.split("\\|");
                String trimmed = parts[3].trim();
                String verified = trimmed.substring(trimmed.indexOf(":")).replaceAll(":", "").trim();
                System.out.println("Owner of lot: " + verified);

                if(verified.equals(curUser))
                {
                    //Create transaction
                    Transaction.Created trc = null;
                    try
                    {
                        trc = TransactionFactory.create(tranMan, SpaceUtils.TWO_SECONDS);
                    }catch (Exception e)
                    {
                        System.err.println("Failed to create Transaction");
                    }

                    Transaction txn = trc.transaction;

                    //Attempt to remove the lot from view in transaction
                    AuctionItem template = new AuctionItem();
                    template.lotNum = currentLotIndex;
                    try
                    {
                        auctionLot = (AuctionItem)js.take(template, txn, SpaceUtils.HALF_SECOND);
                        auctionLot.lotExpired = true;
                        auctionLot.lotPrice = "0";
                        auctionLot.lotBuyNowPrice = "0";
                        auctionLot.lotHighestBidder = "";

                        js.write(auctionLot, txn, Lease.FOREVER);
                        JOptionPane.showMessageDialog(null, "This lot has been removed from auction");
                        txn.commit();

                        dispose();
                    }catch (Exception e)
                    {
                        System.err.println("Failed to remove the lot" + e);
                        try
                        {
                            txn.abort();
                        } catch (UnknownTransactionException | CannotAbortException | RemoteException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                } else
                {
                    JOptionPane.showMessageDialog(null, "You are not the owner of this lot.");
                    dispose();
                }
            }
        });
    }


    private void writeBidHistory()
    {
        try
        {
            //Make sure AuctionLotQueue object exists
            AuctionLotQueue aucTemplate = new AuctionLotQueue();
            AuctionLotQueue aucStatus = (AuctionLotQueue)js.read(aucTemplate, null, SpaceUtils.TWO_SECONDS);

            //If no queue found return else add bid to history
            if(aucStatus == null)
            {
                System.err.println("No AuctionLotQueue found");
                dispose();
            } else
            {
                try
                {
                    //Get bid DateTime
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-DD 'at' HH:mm:ss z");
                    Date date = new Date(System.currentTimeMillis());
                    String newDate = formatter.format(date);

                    //Place bid into space
                    BidItem newBid = new BidItem(currentLotIndex, curUser, txtFldBid.getText(), newDate);
                    js.write(newBid, null, Lease.FOREVER);

                    System.out.println("Added bid to history " + curUser + " " + curLotBuyPrice);
                }catch (Exception e)
                {
                    System.err.println("Failed to add bid to history");
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void getBidHistory()
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
                    bidsTemplate.lotNumber = currentLotIndex;
                    BidItem item = (BidItem)js.takeIfExists(bidsTemplate, txn, SpaceUtils.TWO_SECONDS);
                    if(item != null)
                    {
                        String bidInformation = "Bidder: " + item.bidAccount
                                +  " | " + "Amount: £" + item.bidAmount
                                +  " | " + "Time Bid Placed: " + item.bidDateTime;
                        listModel.addElement(bidInformation);
                        JListBidHistory.setModel(listModel);
                    } else
                    {
                        searching = false;
                    }
                }
            }catch (Exception e)
            {
                System.err.println("Failed to create bid history list: " + e);
            }
        } catch (Exception e)
        {
            System.err.println("Failed to run method: " + e);
        }
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
        /*
        AuctionItem template = new AuctionItem();
        template.lotHighestBidder = curUser;
        template.lotExpired = true;

        try
        {
            AuctionItem notifyLot = (AuctionItem)js.readIfExists(template, null, SpaceUtils.TWO_SECONDS);
            JOptionPane.showMessageDialog(null, "Well done " + curUser + " you won the auction for £" +
                    curLotBidPrice + ", please pay for the item as soon as possible");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
         */
    }
}
