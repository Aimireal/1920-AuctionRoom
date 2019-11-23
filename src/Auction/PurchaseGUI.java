package Auction;

import net.jini.core.entry.Entry;
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
import org.apache.river.api.security.DelegateSecurityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class PurchaseGUI extends JFrame implements RemoteEventListener
{
    private JPanel panelBuyLots;
    private JTextField txtFldBid;
    private JTextField txtFldBuyNowPrice;
    private JButton btnCancel;
    private JButton btnBuyNow;
    private JButton btnPlaceBid;

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private AuctionItem auctionLot;

    private static int FIVE_HUNDRED_MILLS = 500;
    private static int TWO_SECONDS = 2000;
    private static int FIVE_SECONDS = 5000;


    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new PurchaseGUI("AuctionRoom");
            frame.setVisible(true);
        });
    }


    public PurchaseGUI(String title)
    {
        super();

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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


    public void bidButton()
    {
        btnPlaceBid.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                //Create transaction and verify bid is valid
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

                //Place a bid
                Transaction txn = trc.transaction;
                try
                {
                    auctionLot = (AuctionItem)js.readIfExists(auctionLot, txn, FIVE_HUNDRED_MILLS);
                    double userBid = Double.parseDouble(txtFldBid.getText());
                    double lotPrice = Double.parseDouble(auctionLot.lotPrice);

                    if(lotPrice < userBid)
                    {
                        try
                        {
                            js.take(auctionLot, txn, FIVE_HUNDRED_MILLS);
                            auctionLot.lotHighestBidder = "PLACEHOLDER";
                            auctionLot.lotBids++;
                            auctionLot.lotPrice = String.valueOf(userBid);

                            js.write(auctionLot, txn, Lease.FOREVER);
                            JOptionPane.showMessageDialog(null, "You are the highest bidder");
                            txn.commit();
                        }catch (NumberFormatException e)
                        {
                            JOptionPane.showMessageDialog(null, "Something went wrong, details are right and try again");
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


    public void buyButton()
    {
        btnBuyNow.addActionListener(new ActionListener()
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


    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException
    {

    }
}
