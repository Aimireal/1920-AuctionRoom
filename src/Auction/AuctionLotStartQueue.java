/*
    Auction.AuctionLotStartQueue - Start up a connection to the JavaSpace (MUST BE RUN)
    ToDo: More connection options so it can hook up with Gary's JavaSpace server
 */

package Auction;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class AuctionLotStartQueue
{
    public static void main(String args[])
    {
        JavaSpace js = SpaceUtils.getSpace("localhost");
        if(js == null)
        {
            System.err.println("Failed to find JavaSpace");
            System.exit(1);
        }

        try
        {
            AuctionLotQueue queue = new AuctionLotQueue(0);
            js.write(queue, null, Lease.FOREVER);
            System.out.println("Queue made and written to space");
        } catch(Exception e)
        {
            System.err.println("Error. Unable to create Queue or write to space");
        }
    }
}