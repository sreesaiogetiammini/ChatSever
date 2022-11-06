import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");

        ServerSocket serverSocket = new ServerSocket(8080);
        while (true){
        Socket client = serverSocket.accept();
        ClientConnectionHandler connectionHandler = new ClientConnectionHandler(client);
        Thread clientThread  = new Thread(connectionHandler);
        clientThread.start();
        }
    }
}