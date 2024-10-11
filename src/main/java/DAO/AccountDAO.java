package DAO;

import Model.Account;

public interface AccountDAO {
    public Account createAccount( Account account);

    public Account findAccount(String username);
}
