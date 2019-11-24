/*
    Auction.AccountQueue - Used for Auction.AuctionLotStartQueue to initialise and increment auction lots
 */

package Auction;
import net.jini.core.entry.Entry;

public class AccountQueue implements Entry
{
    public Integer counter;

    public AccountQueue()
    {
        //No args constructor
    }

    public AccountQueue(int i)
    {
        counter = i;
    }

    public void incrementCounter()
    {
        counter++;
    }
}
