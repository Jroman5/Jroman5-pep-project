package Service;

import DAO.AccountDAO;
import Model.Account;

public class AccountService {

    private AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO){
        this.accountDAO = accountDAO;
    }

    public Account createAccount(String username, String password){
        if(username == null || username.isBlank()){
            return null;
        }
        if(password.strip().length() < 4){
            return null;
        }
        if(this.accountDAO.findAccount(username) != null){
            return null;
        }
        else{
            return this.accountDAO.createAccount(new Account(username, password));
        }

    }

    public Account login(String username, String password){
        Account acc = null;
        acc = this.accountDAO.findAccount(username);
        if(acc == null){
            return null;
        }
        if(!acc.getPassword().equals(password)){
            return null;
        }
        return this.accountDAO.findAccount(username);
    }
    
}
