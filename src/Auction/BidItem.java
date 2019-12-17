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
    public String bidDateTime;

    public BidItem()
    {
        //No args constructor
    }

    public BidItem(Integer lotCounter, String bidderAccount, String bidderAmount, String newDateTime)
    {
        lotNumber = lotCounter;
        bidAccount = bidderAccount;
        bidAmount = bidderAmount;
        bidDateTime = newDateTime;
    }
}
