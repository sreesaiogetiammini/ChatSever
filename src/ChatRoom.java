import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatRoom {


    private String roomName_;
    private static  HashMap<String,ChatRoom> chatRoomMap_ = new HashMap<>();
    private ArrayList<ClientConnectionHandler> clientsConnectionHandlerList_;
    private ArrayList<String> chatRoomMessagesList_;


    private ChatRoom(String roomName){
        this.roomName_ = roomName;
        this.clientsConnectionHandlerList_ = new ArrayList<>();
        this.chatRoomMessagesList_ = new ArrayList<>();
    }


    //We are using Factory pattern in creating room for a client , so the word static will make sure it can be used without creating object and synchronized
    // is used for Thread safety to process one thread at a time.
    public synchronized  static ChatRoom getRoom(String  roomName){
        ChatRoom chatRoom = ChatRoom.chatRoomMap_.get(roomName);
        if(chatRoom!= null){
            chatRoom = new ChatRoom(roomName);
            ChatRoom.chatRoomMap_.put(roomName,chatRoom);
        }
        return chatRoom;
    }


    public synchronized void addClientToRoom(ClientConnectionHandler client) throws IOException {
        if(clientsConnectionHandlerList_.contains(client)){
            System.out.println("Client already present in a room");
            client.sendWebSocketMessage(client.getUserName()+" already present in a room");
        }
        else {
            clientsConnectionHandlerList_.add(client);
            client.sendWebSocketMessage(client.getUserName()+" Joined the Room");
        }
    }

    public synchronized void removeClientFromRoom(ClientConnectionHandler client) {
        clientsConnectionHandlerList_.remove(client);
    }


    public synchronized void sendMessageToAllClients(String message) {
        clientsConnectionHandlerList_.forEach(client->{
            try {
                client.sendWebSocketMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
