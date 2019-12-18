package Auction;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;


public class AuctionLotStartQueue
{
    public static void main(String args[])
    {
        JavaSpace js = SpaceUtils.getSpace(SpaceUtils.host);
        if(js == null)
        {
            System.err.println("Failed to find JavaSpace");
            System.exit(1);
        }


        try
        {
            AuctionLotQueue queue = new AuctionLotQueue(0);
            js.write(queue, null, Lease.FOREVER);
            System.out.println("AuctionLotQueue made and written to space");
        } catch(Exception e)
        {
            System.err.println("Error. Unable to create AuctionLotQueue or write to space");
        }
    }
}