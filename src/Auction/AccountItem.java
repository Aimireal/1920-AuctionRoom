package Auction;
import net.jini.core.entry.*;


public class AccountItem implements Entry
{
    //Variables
    public Integer accountNum;
    public String accountName;
    public String accountPassword;


    public AccountItem()
    {
        //No args constructor
    }


    public AccountItem(int counter, String username, String password)
    {
        accountNum = counter;
        accountName = username;
        accountPassword = password;
    }
}
