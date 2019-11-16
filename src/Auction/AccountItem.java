/*
    Auction.AccountItem - Class for creating the accounts username and password
 */

package Auction;
import net.jini.core.entry.*;

public class AccountItem
{
    //Variables
    public Integer accountNum;
    public String accountName;
    public String accountPassword;

    public AccountItem()
    {
        //No args template
    }

    public void AccountItem(int counter, String username, String password)
    {
        accountNum = counter;
        accountName = username;
        accountPassword = password;
    }
}
