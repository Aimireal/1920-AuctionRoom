package Auction;

import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SellGUI extends JDialog
{
    private JPanel panelSellLots;
    private JTextField txtFldTitle;
    private JTextField txtFldDesc;
    private JTextField txtFldStartBid;
    private JTextField txtFldBuyNow;
    private JButton btnCancel;
    private JButton btnSubmitSale;

    private JavaSpace js;
    private TransactionManager tranMan;
    private RemoteEventListener stub;

    private String lotTitle;
    private String lotDesc;
    private String lotPrice;
    private String lotBuyNowPrice;
    public static String lotSeller;

    private static int TWO_SECONDS = 2000;
    private static int FIVE_SECONDS = 5000;


    public static JDialog main(String loggedInUsr)
    {
        SwingUtilities.invokeLater(() ->
        {
            JDialog dialog = new SellGUI();
            dialog.setVisible(true);
        });
        lotSeller = loggedInUsr;
        return null;
    }


    private SellGUI()
    {
        super();

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
        //Setup for buttons and anything else for the class
        cancelButton();
        sellButton();
    }


    private void sellButton()
    {
        btnSubmitSale.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                ListLot();
            }
        });
    }


    private void ListLot()
    {
        try
        {
            boolean detailsEntered = verifyDetails();
            if(detailsEntered)
            {
                //Check if AuctionLotQueue object exists
                AuctionLotQueue auctionTemplate = new AuctionLotQueue();
                AuctionLotQueue auctionStatus = (AuctionLotQueue)js.read(auctionTemplate, null, TWO_SECONDS);

                //If no AuctionLotQueue found return else add lot for sale
                if(auctionStatus == null)
                {
                    System.err.println("No AuctionLotQueue found");
                    dispose();
                } else
                {
                    try
                    {
                        //Attempt to add new lot to space
                        auctionStatus.incrementCounter();
                        int counter = auctionStatus.counter;
                        AuctionItem newLot = new AuctionItem(counter, lotTitle, lotDesc, lotPrice, lotBuyNowPrice, lotSeller);
                        js.write(newLot, null, Lease.FOREVER);

                        System.out.println("Successfully added lot");
                        dispose();
                    }catch (Exception e)
                    {
                        System.err.println("Failed to add lot");
                        e.printStackTrace();
                    }
                }
            } else
            {
                JOptionPane.showMessageDialog(null, "Please ensure you have entered all required information");
            }
        }catch (Exception e)
        {
            e.printStackTrace();
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


    private boolean verifyDetails()
    {
        //Get details of the lot entered and verify required info
        String title = txtFldTitle.getText();
        String description = txtFldDesc.getText();
        String price = txtFldStartBid.getText();
        String buyNowPrice = txtFldBuyNow.getText();

        if(title == null || title.isEmpty())
        {
            System.err.println("You need a title");
            return false;
        } else
        {
            if(description == null || description.isEmpty())
            {
                System.err.println("You need a description");
                return false;
            } else
            {
                if(price == null || price.isEmpty())
                {
                    System.err.println("You need a starting bid");
                    return false;
                } else
                {
                    if(!price.matches("[0-9.]*"))
                    {
                        System.err.println("Enter numbers");
                        return false;
                    } else
                    {
                        if(!buyNowPrice.matches("[0-9.]*"))
                        {
                            System.err.println("Enter numbers");
                            return false;
                        } else
                        {
                            {
                                //Values verified
                                lotTitle = title;
                                lotDesc = description;
                                lotPrice = price;
                                lotBuyNowPrice = buyNowPrice;
                                return true;
                            }
                        }
                    }
                }
            }
        }
    }


}
