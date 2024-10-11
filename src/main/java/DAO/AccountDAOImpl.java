package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Model.Account;

// ## 1: Our API should be able to process new User registrations.

// As a user, I should be able to create a new Account on the endpoint POST localhost:8080/register. The body will contain a representation of a JSON Account, but will not contain an account_id.

// - The registration will be successful if and only if the username is not blank, the password is at least 4 characters long, and an Account with that username does not already exist. If all these conditions are met, the response body should contain a JSON of the Account, including its account_id. The response status should be 200 OK, which is the default. The new account should be persisted to the database.
// - If the registration is not successful, the response status should be 400. (Client error)

// ## 2: Our API should be able to process User logins.

// As a user, I should be able to verify my login on the endpoint POST localhost:8080/login. The request body will contain a JSON representation of an Account, not containing an account_id. In the future, this action may generate a Session token to allow the user to securely use the site. We will not worry about this for now.

// - The login will be successful if and only if the username and password provided in the request body JSON match a real account existing on the database. If successful, the response body should contain a JSON of the account in the response body, including its account_id. The response status should be 200 OK, which is the default.
// - If the login is not successful, the response status should be 401. (Unauthorized)



public class AccountDAOImpl implements AccountDAO{

    private Connection dbConnection;
    private static AccountDAOImpl instance = null;

    private AccountDAOImpl(Connection dbConnection){
        this.dbConnection = dbConnection;
    }
    public static AccountDAOImpl getInstance(Connection conn){
        if(instance == null){
            instance = new AccountDAOImpl(conn);
        }
        return instance;

    }

    @Override
    public Account createAccount(Account account) {

        String sql = "INSERT INTO account(username, password) values(?,?)";
        int res = 0;
        try{
            PreparedStatement query = this.dbConnection.prepareStatement(sql);

            query.setString(1, account.getUsername());
            query.setString(2,account.getPassword());

            res = query.executeUpdate();
        } catch(Exception e){
            e.printStackTrace();
        }
        
        if(res > 0){
            return findAccount(account.getUsername());
        }
        return null;
    }

    @Override
    public Account findAccount(String username) {
        String sql = "Select * from account WHERE username = ?";
        Account acc = null;
        try{
            PreparedStatement query = this.dbConnection.prepareStatement(sql);
            query.setString(1, username);
            ResultSet res = query.executeQuery();
            if(res.first()){
                acc =  new Account(res.getInt("account_id"),res.getString("username"), res.getString("password"));
            }   
        } catch(Exception e){
            e.printStackTrace();
        }
        return acc;
    }
}


    
