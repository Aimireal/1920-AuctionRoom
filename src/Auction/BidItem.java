/*
    Auction.BidItem - Class for creating the bid history information
 */

package Auction;
import net.jini.core.entry.Entry;


public class BidItem implements Entry
{
    //Variables
    public Integer lotNumber;
    public String bidAccount;
    public String bidAmount;

    public BidItem()
    {
        //No args constructor
    }

    public BidItem(Integer lotCounter, String bidderAccount, String bidderAmount)
    {
        lotNumber = lotCounter;
        bidAmount = bidderAmount;
        bidAmount = bidderAccount;
    }
}
