package Auction;
import net.jini.core.entry.*;


public class AuctionItem implements Entry
{
    //Variables
    public Integer lotNum; //Used for counter location when we read in the ShowLotsGUI
    public String lotTitle;
    public String lotDesc;
    public String lotPrice; //Set by lot author, then updated to reflect current bid
    public String lotBuyNowPrice; //Set by author, price for instantly buying lot

    public String lotSellerID; //Identification for Seller of Lot
    public String lotHighestBidder; //Current winning user, initialised as nothing

    public Long lotCreationTime;
    public Boolean lotExpired; //Boolean for active auction
    public Boolean lotNotified;


    public AuctionItem()
    {
        //No args constructor
    }


    public AuctionItem(int counter, String title, String description, String price, String buyNowPrice, String seller)
    {
        lotNum = counter;
        lotTitle = title;
        lotDesc = description;
        lotPrice = price;
        lotBuyNowPrice = buyNowPrice;

        lotSellerID = seller;
        lotHighestBidder = "";

        lotCreationTime = System.currentTimeMillis();
        lotExpired = false;
        lotNotified = false;
    }

}


