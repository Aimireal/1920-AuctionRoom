package Auction;
import net.jini.space.JavaSpace;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.server.TransactionManager;


public class SpaceUtils
{
    //This code is from Gary, unchanged other than the variables below
    public static String host = "waterloo";
    public static int HALF_SECOND = 500;
    public static int ONE_SECOND = 1000;
    public static int TWO_SECONDS = 2000;
    public static int FIVE_SECONDS = 5000;
    //----------------------------------------------------------------

    public static JavaSpace getSpace(String hostname)
    {
        if(System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }

        JavaSpace js = null;
        try
        {
            LookupLocator l = new LookupLocator("jini://" + hostname);
            ServiceRegistrar sr = l.getRegistrar();

            Class c = Class.forName("net.jini.space.JavaSpace");
            Class[] classTemplate = {c};

            js = (JavaSpace) sr.lookup(new ServiceTemplate(null, classTemplate, null));
        } catch (Exception e)
        {
            System.err.println("Error; " + e);
        }
        return js;
    }

    public static JavaSpace getSpace()
    {
        return getSpace("waterloo");
    }

    public static TransactionManager getManager(String hostname)
    {
        if(System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }

        TransactionManager tm = null;
        try
        {
            LookupLocator l = new LookupLocator("jini://" + hostname);
            ServiceRegistrar sr = l.getRegistrar();

            Class c = Class.forName("net.jini.core.transaction.server.TransactionManager");
            Class[] classTemplate = {c};

            tm = (TransactionManager) sr.lookup(new ServiceTemplate(null, classTemplate, null));
        } catch (Exception e)
        {
            System.err.println("Error: " + e);
        }
        return tm;
    }

    public static TransactionManager getManager()
    {
        return getManager("waterloo");
    }
}
