package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import DAO.AccountDAOImpl;
import DAO.MessageDAOImpl;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Model.Account;
import Util.ConnectionUtil;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */

//  ## 1: Our API should be able to process new User registrations.

// As a user, I should be able to create a new Account on the endpoint POST localhost:8080/register. The body will contain a representation of a JSON Account, but will not contain an account_id.

// - The registration will be successful if and only if the username is not blank, the password is at least 4 characters long, and an Account with that username does not already exist. If all these conditions are met, the response body should contain a JSON of the Account, including its account_id. The response status should be 200 OK, which is the default. The new account should be persisted to the database.
// - If the registration is not successful, the response status should be 400. (Client error)

// ## 2: Our API should be able to process User logins.

// As a user, I should be able to verify my login on the endpoint POST localhost:8080/login. The request body will contain a JSON representation of an Account, not containing an account_id. In the future, this action may generate a Session token to allow the user to securely use the site. We will not worry about this for now.

// - The login will be successful if and only if the username and password provided in the request body JSON match a real account existing on the database. If successful, the response body should contain a JSON of the account in the response body, including its account_id. The response status should be 200 OK, which is the default.
// - If the login is not successful, the response status should be 401. (Unauthorized)


// ## 3: Our API should be able to process the creation of new messages.

// As a user, I should be able to submit a new post on the endpoint POST localhost:8080/messages. The request body will contain a JSON representation of a message, which should be persisted to the database, but will not contain a message_id.

// - The creation of the message will be successful if and only if the message_text is not blank, is not over 255 characters, and posted_by refers to a real, existing user. If successful, the response body should contain a JSON of the message, including its message_id. The response status should be 200, which is the default. The new message should be persisted to the database.
// - If the creation of the message is not successful, the response status should be 400. (Client error)

// ## 4: Our API should be able to retrieve all messages.

// As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages.

// - The response body should contain a JSON representation of a list containing all messages retrieved from the database. It is expected for the list to simply be empty if there are no messages. The response status should always be 200, which is the default.

// ## 5: Our API should be able to retrieve a message by its ID.

// As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages/{message_id}.

// - The response body should contain a JSON representation of the message identified by the message_id. It is expected for the response body to simply be empty if there is no such message. The response status should always be 200, which is the default.

// ## 6: Our API should be able to delete a message identified by a message ID.

// As a User, I should be able to submit a DELETE request on the endpoint DELETE localhost:8080/messages/{message_id}.

// - The deletion of an existing message should remove an existing message from the database. If the message existed, the response body should contain the now-deleted message. The response status should be 200, which is the default.
// - If the message did not exist, the response status should be 200, but the response body should be empty. This is because the DELETE verb is intended to be idempotent, ie, multiple calls to the DELETE endpoint should respond with the same type of response.

// ## 7: Our API should be able to update a message text identified by a message ID.

// As a user, I should be able to submit a PATCH request on the endpoint PATCH localhost:8080/messages/{message_id}. The request body should contain a new message_text values to replace the message identified by message_id. The request body can not be guaranteed to contain any other information.

// - The update of a message should be successful if and only if the message id already exists and the new message_text is not blank and is not over 255 characters. If the update is successful, the response body should contain the full updated message (including message_id, posted_by, message_text, and time_posted_epoch), and the response status should be 200, which is the default. The message existing on the database should have the updated message_text.
// - If the update of the message is not successful for any reason, the response status should be 400. (Client error)

// ## 8: Our API should be able to retrieve all messages written by a particular user.

// As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/accounts/{account_id}/messages.

// - The response body should contain a JSON representation of a list containing all messages posted by a particular user, which is retrieved from the database. It is expected for the list to simply be empty if there are no messages. The response status should always be 200, which is the default.

// # Further guidance
public class SocialMediaController {
    private AccountService accountService = new AccountService(AccountDAOImpl.getInstance(ConnectionUtil.getConnection()));
    private MessageService messageService = new MessageService(MessageDAOImpl.getInstance(ConnectionUtil.getConnection()));
    private ObjectMapper om = new ObjectMapper();
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        
        Javalin app = Javalin.create();
        app.get("example-endpoint", this::exampleHandler);

        app.post("/register", this::registrationHandler);
        app.post("/login", this::loginHandler);
        app.post("/messages", this::createMessageHandler);
        app.get("/messages", this::getAllMessagesHandler);
        app.get("/messages/{message_id}", this::getMessageHandler);
        app.delete("/messages/{message_id}", this::deleteMessageHandler);
        app.patch("/messages/{message_id}", this::updateMessageHandler);
        app.get("/accounts/{account_id}/messages", this::findAllMessagesByUserHandler);


        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void exampleHandler(Context context) {
        context.json("sample text");
    }

    private void registrationHandler(Context context){
        String body = context.body();
        try{
            Account accAttempt = om.readValue(body, Account.class);
            Account res = accountService.createAccount(accAttempt.getUsername(), accAttempt.getPassword());
            if(res == null){
                context.status(400);
            }
            context.json(res);
            context.status(200);
            

        } catch(Exception e){
            context.status(400);
            e.printStackTrace();
        }
       
    }

    private void loginHandler(Context ctx){
        try{
            String body = ctx.body();
            Account acc = om.readValue(body, Account.class);
            Account res = accountService.login(acc.getUsername(), acc.getPassword());
            if(res == null){
                ctx.status(401);
            }
            else{
                ctx.json(res);
                ctx.status(200);
            }
        } catch(Exception e){
            ctx.status(401);
        }
    }

    private void createMessageHandler(Context ctx){
        Message res = null;
        try{
            String body = ctx.body();
            Message messageToCreate = om.readValue(body, Message.class);
            res = messageService.createMessage(messageToCreate.getPosted_by(), messageToCreate.getMessage_text(), messageToCreate.getTime_posted_epoch());
            if(res == null){
                ctx.status(400);
            }
            ctx.json(res);
            ctx.status(200);
            
        } catch (Exception e){
            ctx.status(400);
        }
    }

    private void getAllMessagesHandler(Context ctx){
        List<Message> res = messageService.findAllMessages(); 
        ctx.json(res);
        ctx.status(200);
    }

    private void getMessageHandler(Context ctx){
        try{
            String messageId = ctx.pathParam("message_id");
            Message res = messageService.findMessageById(Integer.parseInt(messageId));
            ctx.json(res);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(200);
        }
        
        
    }

    private void deleteMessageHandler(Context ctx){
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        Message res = messageService.deleteMessage(messageId);
        ctx.status(200);
        if(res != null){
            ctx.json(res);
        }
    }

    private void updateMessageHandler(Context ctx){
//         String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
// Map<String, Object> map 
//   = objectMapper.readValue(json, new TypeReference<Map<String,Object>>(){});
        try{
            int messageId = Integer.parseInt(ctx.pathParam("message_id"));
            String body = ctx.body();
            Map<String, String> map = om.readValue(body, new TypeReference<Map<String,String>>(){});
            Message res = messageService.updateMessage(messageId, map.get("message_text"));
            if(res == null){
                ctx.status(400);
            }
            else{
                ctx.status(200);
                ctx.json(res);
            }
        } catch(Exception e){
            ctx.status(400);
        }
    }

    private void findAllMessagesByUserHandler(Context ctx){
        List<Message> res = messageService.findAllMessagesByUser(Integer.parseInt(ctx.pathParam("account_id")));
        ctx.status(200);
        ctx.json(res);
    }


    


}