import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

public class ClientConnectionHandler implements Runnable{

    private final Socket client_;
    private ChatRoom chatRoom_;
    private String userName_;

    ClientConnectionHandler(Socket client){

        this.client_ = client;
    }

    public String getUserName(){
        return this.userName_;
    }



    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        System.out.println("Thread got created"+Thread.currentThread().getName());
        try {
            InputStream inputStream = client_.getInputStream();
            HashMap<String,String> requestMap = new HashMap<>();
            Scanner scanner = new Scanner(inputStream);
            String line = scanner.nextLine();
            String fileName = null;
            if(line.contains("GET") && line.contains("HTTP/1.1")){
                fileName = getFileName(line);
            }
            while(!line.equals("")){
                String[] list = line.split(":");
                if (list.length == 2) {
                    requestMap.put(list[0], list[1]);
                }
                line = scanner.nextLine();
            }

            if(requestMap.containsKey("Sec-WebSocket-Key")){
                System.out.println("I  should handle WebSocket Request");
                handleWebSocketRequest(requestMap.get("Sec-WebSocket-Key"));
            }

            else if(!fileName.equals(null)){
                System.out.println("Handling AJAX call request");
                handleAjaxRequest(fileName);
            }

            client_.close();

        }



        catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public String getFileName(String inputLine){
        String[] stringList = inputLine.split(" ");
        return stringList[1];
    }

    public void handleAjaxRequest(String fileName) {
        String path = null;
        try {
            System.out.println("File Name: " +fileName);
            OutputStream outputStream = client_.getOutputStream();
            path = "resources" + fileName;
            if(path.equals("resources/")){
                path = "resources/index.html";
            }
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Checking file in resources folder");
                file = new File("resources/errorTest.html");
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            String headLine1 = "HTTP/1.1 200 OK";
            String headLine2 ;
            if (path.contains(".html")) {
                headLine2 = "Content-Type: text/html";
            } else if (path.contains(".css")) {
                headLine2 = "Content-Type: text/css";
            } else if (path.contains(".js")) {
                headLine2 = "Content-Type: text/javascript";
            } else {
                headLine2 = "Content-Type: image/x-icon";
            }
            outputStream.write(headLine1.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write(headLine2.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.write("\n".getBytes());
            for (int i = 0; i < file.length(); i++) {
                outputStream.write(fileInputStream.read());
                outputStream.flush();
            }
            System.out.println("AjaX file transferred successfully "+path);
        }
        catch (FileNotFoundException e){
            System.out.println("File Not found Exception"+path);
        }
        catch (IOException e) {
            System.out.println("IOException "+path);
            throw new RuntimeException(e);
        }
    }


    public void handleWebSocketRequest(String key) {
        try {
            OutputStream outputStream = client_.getOutputStream();
            System.out.println("Handling the WebSocket Request from client " + key);
            String newKey = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

            System.out.println(newKey);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] data = newKey.getBytes();
            String keyResponse = Base64.getEncoder().encodeToString(messageDigest.digest(data));

            System.out.println(" response: " + keyResponse );
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println("HTTP/1.1 101 Switching Protocols\r\n");
            printWriter.print("Upgrade: websocket\r\n");
            printWriter.print("Connection: Upgrade\r\n");
            printWriter.print("Sec-WebSocket-Accept: " + keyResponse + "\r\n");
            printWriter.print("\r\n");
            printWriter.flush();
            System.out.println("Handshake completed");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException for ");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }




    public synchronized void sendWebSocketMessage(String message) throws IOException {

        DataOutputStream dataOutputStream = new DataOutputStream(client_.getOutputStream());

        //Sending the message in a form of ascii values
        //First two bytes has opcode and server message with header and payload
        //1st byte written as 1000 0001
        dataOutputStream.writeByte(0x81);
        int length = message.length();

        if(length>125){
            if(length>65535)
            {
                dataOutputStream.writeByte(126);
                dataOutputStream.writeLong(length);
            }
            else
            {
                dataOutputStream.writeByte(127);
                dataOutputStream.writeShort(length);
            }

        }
        else
        {
            dataOutputStream.writeByte(length);
        }
        dataOutputStream.writeBytes(message);
    }


    public void readWebSocketMessage() throws IOException {
        DataInputStream dataInputStream = new DataInputStream(client_.getInputStream());
        byte[] firstTwoBytes = dataInputStream.readNBytes(2);
        boolean isMasked = (firstTwoBytes[0]&0x80)>0;
        long byteLength = firstTwoBytes[1]& 0x7F;
        if(isMasked)
        {
            byte[] maskingKey = dataInputStream.readNBytes(4);
        }
        System.out.println(byteLength);
        if (byteLength == 126) {
            byteLength = dataInputStream.readUnsignedShort();
        }
        else if (byteLength == 127) {
            byteLength = dataInputStream.readLong();
        }
        byte[] maskingKey = dataInputStream.readNBytes(4);
        byte[] encodedMessage = dataInputStream.readNBytes((int) byteLength);
        byte[] decodedMessageInBytes = "".getBytes();
        for(int i =0; i<encodedMessage.length;i++){
            decodedMessageInBytes[i] = (byte) (encodedMessage[i]^maskingKey[i%4]);
        }
        String decodedMessage = new String(decodedMessageInBytes);
        System.out.println("Decoded Message is : "+decodedMessage);
    }

//    private void readWebSocketMessage() throws Exception {
//        try {
//            final DataInputStream dataInputStream = new DataInputStream(this.client_.getInputStream());
//            final byte[] nBytes = dataInputStream.readNBytes(2);
//            final boolean b = (nBytes[0] & 0x80) > 0;
//            if ((nBytes[0] & 0xF) == 0x8) {
//                throw new Exception("Connection Closed");
//            }
//            final boolean b2 = (nBytes[1] & 0x80) != 0x0;
//            long long1 = nBytes[1] & 0x7F;
//            if (long1 == 126L) {
//                long1 = dataInputStream.readUnsignedShort();
//            }
//            else if (long1 == 127L) {
//                long1 = dataInputStream.readLong();
//            }
//            final byte[] nBytes2 = dataInputStream.readNBytes(4);
//            final byte[] nBytes3 = dataInputStream.readNBytes((int)long1);
//            for (int n = 0; n < long1; ++n) {
//                final byte[] array = nBytes3;
//                final int n2 = n;
//                array[n2] ^= nBytes2[n % 4];
//            }
//            final String[] split = new String(nBytes3).split(" ", 2);
//            if (split[0].equals("join")) {
//                if (this.userName_ != null) {
//                    System.out.println("WARNING: client is re-joining...");
//                }
//                final String[] split2 = split[1].split(" ", 2);
//                this.userName_ = split2[0];
//                (this.chatRoom_ = ChatRoom.getRoom(split2[1])).addClient(this);
//                if (this.userName_.equals("Server")) {
//                    System.out.println("WARNING: Client tried to name themself 'Server'");
//                }
//            }
//            else {
//                final String s = split[0];
//                if (!s.equals(this.userName_)) {
//                    // invokedynamic(makeConcatWithConstants:(Ljava/lang/String;)Ljava/lang/String;, s)
//                    this.chatRoom_.sendMessage(MyJsonCreator.createMessage("Server:", this.room_.getName(), invokedynamic(makeConcatWithConstants:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;, this.userName_, s)), false);
//                }
//                else {
//                    this.chatRoom_.sendMessage(MyJsonCreator.createMessage(this.userName_, this.room_.getName(), split[1]), true);
//                }
//            }
//        }
//        catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
}
