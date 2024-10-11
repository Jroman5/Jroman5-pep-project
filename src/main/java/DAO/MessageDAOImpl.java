package DAO;

import java.util.ArrayList;
import java.util.List;

import Model.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

// Some classes are already complete and SHOULD NOT BE CHANGED - Integration tests, Model classes for Account and Message, a ConnectionUtil class. Changing any of these classes will likely result in the test cases being impossible to pass.

// The .sql script found in src/main/resources is already complete and SHOULD NOT BE CHANGED. Changing this file will likely result in the test cases being impossible to pass.

// You SHOULD be changing the SocialMediaController class to add endpoints to the StartAPI method. A main method in Main.java is also provided to allow you to run the entire application and manually play or test with the app. Changing that class will not affect the test cases at all. You could use it to perform any manual unit testing on your other classes.

// You SHOULD be creating and designing DAO and Service class to allow you to complete the project. In theory, you could design the project however you like, so long as the functionality works and you are somehow persisting data to the database - but a 3-layer architecture is a robust design pattern and following help you in the long run. You can refer to prior mini-projects and course material for help on designing your application in this way.

// # Good luck!
public class MessageDAOImpl implements MessageDAO{

    private Connection dbConn;
    private static MessageDAOImpl instance = null;

    private MessageDAOImpl(Connection conn){
        this.dbConn = conn;
    }

    public static MessageDAOImpl getInstance(Connection conn){
        if(instance == null){
            instance = new MessageDAOImpl(conn);
        }
        return instance;

    }

    @Override
    public Message createMessage(Message message) {
        String sql = "Insert into message(posted_by, message_text, time_posted_epoch) Values(?,?,?)";
        Message res = null;
        try{
            PreparedStatement ps = this.dbConn.prepareStatement(sql);
            ps.setInt(1, message.getPosted_by());
            ps.setString(2,message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch()); 
            ps.executeUpdate();

            res = findMessageByUserIdAndMessageTextAndTimePosted(message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());

        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
        
    }

    @Override
    public List<Message> findAllMessages() {
        List<Message> res = null;
        String sql = "Select * from message"; 
        try{
            PreparedStatement ps = dbConn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            res = new ArrayList<>();
            while(rs.next()){
                // int message_id, int posted_by, String message_text, long time_posted_epoch
                res.add(new Message(
                    rs.getInt("message_id"), 
                    rs.getInt("posted_by"), 
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")));
            } 

        } catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Message findMessageById(int message_id) {
        String sql = "Select * from message where message_id = ?";
        Message res = null;
        try{
            PreparedStatement ps = dbConn.prepareStatement(sql);
            ps.setInt(1, message_id);
            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                res = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                    );
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }
    // message_id int primary key auto_increment,
    // posted_by int,
    // message_text varchar(255),
    // time_posted_epoch bigint,
    @Override
    public Message deleteMessage(int message_id) {
        String sql = "Delete from message where message_id = ?";
        int change = 0;
        Message res = findMessageById(message_id);
        try{
            PreparedStatement ps = dbConn.prepareStatement(sql);
            ps.setInt(1, message_id);
            change = ps.executeUpdate();
        } catch(Exception e){
            e.printStackTrace();
        }
        return (change > 0) ? res : null;
    }

    @Override
    public Message updateMessage(int messageId, String messageBody) {
        String sql = "Update message set message_text = ? where message_id = ?";
        Message res = null;
        try{
            int result = 0;
            PreparedStatement ps = dbConn.prepareStatement(sql);
            ps.setInt(2, messageId);
            ps.setString(1, messageBody);
            result = ps.executeUpdate();
            if(result > 0){
                res = findMessageById(messageId);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<Message> findAllMessagesByUser(int userId) {
        String sql = "Select * from message where posted_by = ?";
        List<Message> res = new ArrayList<>();
        try{
            PreparedStatement ps = dbConn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                res.add(new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                    )
                );
            }
        }catch(Exception e){
            e.printStackTrace();
        } 
        return res;
    }

    private Message findMessageByUserIdAndMessageTextAndTimePosted(int posted_by, String messageText ,long time_posted_epoch){
        String sql = "Select * from message where posted_by = ? AND message_text = ? AND time_posted_epoch = ?";
        ResultSet rs = null;
        Message res = null;
        try{
            PreparedStatement st = dbConn.prepareStatement(sql);
            st.setInt(1, posted_by);
            st.setString(2, messageText);
            st.setLong(3, time_posted_epoch);
            rs = st.executeQuery();
            if(rs.first()){
                res =  new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch"));
            }
            return res;
        } catch(Exception e){
            e.printStackTrace();
        }
        return res;

    }
    
}
