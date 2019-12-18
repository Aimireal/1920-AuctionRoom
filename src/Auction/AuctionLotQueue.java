package Auction;
import net.jini.core.entry.Entry;


public class AuctionLotQueue implements Entry
{
    public Integer counter;


    public AuctionLotQueue()
    {
        //No args constructor
    }

    public AuctionLotQueue(int i)
    {
        counter = i;
    }

    public void incrementCounter()
    {
        counter++;
    }

}
