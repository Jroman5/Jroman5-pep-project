package DAO;

import java.util.ArrayList;
import java.util.List;

import Model.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
