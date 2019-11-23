package Auction;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class SellGUI extends JFrame implements RemoteEventListener
{
    private JPanel panelSellLots;
    private JTextField txtFldTitle;
    private JTextField txtFldDesc;
    private JTextField txtFldStartBid;
    private JTextField txtFldBuyNow;
    private JButton btnCancel;
    private JButton btnSubmitSale;
    private JTextField txtFldDuration;

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private static int FIVE_SECONDS = 5000;


    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new SellGUI("AuctionRoom");
            frame.setVisible(true);
        });
    }


    public SellGUI(String title)
    {
        super();

        //Basic setup
        setTitle("AuctionRoom: Sell Lots");

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.setContentPane(panelSellLots);
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
    }


    private void setupGUI()
    {
        try
        {
            btnSubmitSale.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent actionEvent)
                {
                    try
                    {
                        //Get details of the lot entered and verify required info
                        String title = txtFldTitle.getText();
                        String description = txtFldDesc.getText();
                        String price = txtFldDesc.getText();
                        String buyNowPrice = txtFldBuyNow.getText();
                        String seller = "PLACEHOLDER"; //ToDo: Method to extract current logged in user name here
                        String durString = txtFldDuration.getText();
                        long duration = 0;

                        if(title == null || title.isEmpty())
                        {
                            System.err.println("You need a title");
                            System.exit(1);
                        }

                        if(description == null || description.isEmpty())
                        {
                            System.err.println("You need a description");
                            System.exit(1);
                        }

                        if(price == null || price.isEmpty())
                        {
                            System.err.println("You need a starting bid");
                            System.exit(1);
                        } else
                        {
                            if(!price.matches("[0-9.]*"))
                            {
                                System.err.println("Enter numbers");
                                System.exit(1);
                            }
                        }

                        if(durString == null || durString.isEmpty())
                        {
                            System.err.println("You to give a duration");
                            System.exit(1);
                        } else 
                        {
                            if(Long.parseLong(durString) < 0)
                            {
                                System.err.println("You must give a positive duration");
                                System.exit(1);
                            } else 
                            {
                                duration = Long.parseLong(durString);
                            }
                        }

                        //Create Transaction
                        Transaction.Created  trc = null;
                        try
                        {
                            trc = TransactionFactory.create(tranMan, Lease.FOREVER);
                        }catch(Exception e)
                        {
                            System.err.println("Failed to create Transaction");
                        }

                        Transaction txn = trc.transaction;
                        AuctionLotQueue template = new AuctionLotQueue();
                        try
                        {
                            AuctionLotQueue queue = (AuctionLotQueue)js.take(template, txn, FIVE_SECONDS);
                            int counter = queue.counter;

                            AuctionItem newLot =  new AuctionItem(counter, title, description, price, buyNowPrice, seller, duration);
                            js.write(newLot, txn, Lease.FOREVER);
                            queue.incrementCounter();
                            txn.commit();
                        } catch(Exception e)
                        {
                            txn.abort();
                            e.printStackTrace();
                        }
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            btnCancel.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent actionEvent)
                {
                    //ToDo: Code to move back to main form. Eventually will remove code to manually launch this I guess
                }
            });
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException
    {

    }
}
