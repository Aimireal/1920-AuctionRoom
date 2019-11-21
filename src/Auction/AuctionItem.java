/*
    Auction.AuctionItem - Class for creating the lots and setting the values
 */

package Auction;
import net.jini.core.entry.*;

public class AuctionItem implements Entry
{
    //Variables
    public Integer lotNum; //Used for counter location when we read in the ShowLotsGUI
    public String lotTitle;
    public String lotDesc;
    public double lotPrice; //Set by lot author, then updated to reflect current bid
    public double lotBuyNowPrice; //Set by author, price for instantly buying lot
    public String lotSellerID; //Identification for Seller of Lot

    public Integer lotBids; //Number of bids placed
    public String lotHighestBidder; //Current winning user, initialised as nothing
    public Long lotCreationTime;
    public Long lotDuration; //Specified duration of Lot
    public Long lotEndTime;
    public Boolean lotExpired; //Boolean for active auction

    public AuctionItem()
    {
        //No args template
    }

    public void AuctionItem(int counter, String title, String description, Double price, Double buyNowPrice, String seller, Long duration)
    {
        lotNum = counter;
        lotTitle = title;
        lotDesc = description;
        lotPrice = price;
        lotBuyNowPrice = buyNowPrice;
        lotSellerID = seller;
        lotDuration = duration;

        lotHighestBidder = "";
        lotCreationTime = System.currentTimeMillis();
        lotEndTime = lotCreationTime + timeToMills(lotDuration);
        lotExpired = false;
    }

    public void addBid()
    {
        lotBids++;
    }

    public void newPrice(double newBid)
    {
        //Method for updating lot price when bids are placed
        lotPrice = newBid;
    }

    public long timeToMills(long t)
    {
        //Convert time for working out total time
        return t * 60 * 60 * 1000;
    }

}


