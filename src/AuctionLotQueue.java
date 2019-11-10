/*
    AuctionLotQueue - Used for AuctionLotStartQueue to initialise and increment auction lots
 */

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
        counter = counter.intValue() + 1;
    }
}
