package Service;
import DAO.MessageDAO;
import Model.Message;
import java.util.List;


public class MessageService {
    private MessageDAO messageDAO;

    public MessageService(MessageDAO messageDAO){
        this.messageDAO = messageDAO;
    }
    
    public Message createMessage(int posted_by, String message_text, long time_posted_epoch){
        if(message_text.length() > 255){
            return null;
        }
        if(message_text.isBlank()){
            return null;
        }
        Message post = new Message(posted_by, message_text, time_posted_epoch);
        return this.messageDAO.createMessage(post);
    }

    public Message updateMessage(int message_id, String message_text){
        if(message_text.length() > 255){
            return null;
        }
        if(message_text.isBlank()){
            return null;
        }
        return this.messageDAO.updateMessage(message_id, message_text);
    }

    public Message deleteMessage(int message_id){
        return this.messageDAO.deleteMessage(message_id);

    }
    public List<Message> findAllMessages(){

        return this.messageDAO.findAllMessages();
    }
    public List<Message> findAllMessagesByUser(int userId){
        return this.messageDAO.findAllMessagesByUser(userId);
    }
    public Message findMessageById(int message_id){
        return this.messageDAO.findMessageById(message_id);
    }    
}
