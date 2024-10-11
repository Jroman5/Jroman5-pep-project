package DAO;
import Model.Message;
import java.util.List;

public interface MessageDAO {
    
    public Message createMessage(Message message);

    public List<Message> findAllMessages();
    
    public Message findMessageById(int messageId);

    public Message deleteMessage(int messageId);

    public Message updateMessage(int messageId, String messageBody);

    public List<Message> findAllMessagesByUser(int userId);
}
