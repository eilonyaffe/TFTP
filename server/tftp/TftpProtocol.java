package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.nio.file.*;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate; //eliya - is it necessary to make it volatile
    private int connectionId;
    private  Connections<byte[]> connections;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        // TODO implement this
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(byte[] message) { 
        // TODO implement this
        byte opcode = message[1];

        if (opcode == 1)
            readRequest(message);

        else if (opcode == 2)
            writeRequest(message);

        else if (opcode == 3)
            dataPacketOp(message);

        else if (opcode == 4)
            ackOperation(message);

        else if (opcode == 5)
            errorOperation(message);

        else if (opcode == 6)
            listingRequest(message);

        else if (opcode == 7){
            boolean successfulLogIn = this.logOperation(message);
            if(successfulLogIn){
                //TODO: return ACK
            }
            else{
                //TODO: return ERROR
            }
            System.out.println("LOGRQ was completed successfully: "+successfulLogIn);
        }
            

        else if (opcode == 8)
            deleteFile(message);

        else if (opcode == 9)
            bcastOperation(message);

        else if (opcode == 10)
            disconnectOp(message);

    }

    //handles RRQ message sent from client to server
    private void readRequest(byte[] message){ //assumes there is no zero byte in the filename
        //TODO
        byte[] filenameInBytes = Arrays.copyOfRange(message,2, message.length - 2);

        // Convert byte array to string using UTF-8
        String filename = new String(filenameInBytes, StandardCharsets.UTF_8);
        Path filePath = Paths.get("Files", filename); //constructs the path to the file

        try {
            boolean isExist = Files.exists(filePath);
        } catch (SecurityException e) {} //needed?? maybe not? - neya

        //read block former should come here

        //TODO
        // if(isExist)
        //     //dataPacketOp() - change get filePath and opcode - i want to send the file to the client - using DATA pkg
        // else
        //     //send ERROR pkg
    }

    //handles WRQ message sent from client to server
    private void writeRequest(byte[] message){
        //TODO

        //write block former should come here
        
    }

    //handles DATA message sent from server to client
    private void dataPacketOp(byte[] message){
        //TODO
    }

    //handles ACK message sent from server to client
    private void ackOperation(byte[] message){
        //TODO
    }

    //handles ERROR message sent from server to client
    private void errorOperation(byte[] message){
        //TODO
    }

    //handles DIRQ message sent from client to server
    private void listingRequest(byte[] message){
        //TODO
    }

    //handles LOGRQ message sent from client to server
    private boolean logOperation(byte[] message){
        if(connectionsHolder.connectionsObj.inactive_connections.containsKey(connectionId)){
            ConnectionHandler<byte[]> BCH = connectionsHolder.connectionsObj.inactive_connections.get(connectionId);
            connectionsHolder.connectionsObj.inactive_connections.remove(connectionId);
            String name = "";
            try{
                name = new String(message, "UTF-8"); //should check if legal?
            } catch(UnsupportedEncodingException e){}
            System.out.println("name entered was: "+name);
            this.connectionId = name.hashCode(); //assuming valid input from user
            if(connectionsHolder.connectionsObj.inactive_connections.containsKey(connectionId)) return false; //there's already a client with that name. TODO also need to terminate client? probably not
            else{ //was inactive, now should be activated
                connectionsHolder.connectionsObj.connect(this.connectionId, BCH); //will insert to active connections
                return true;
            }
        }
        return false;
    }

    //handles DELRQ message sent from client to server
    private void deleteFile(byte[] message){
        //TODO
    }

    //handles ERROR message sent from server to all logged clients
    private void bcastOperation(byte[] message){
        //TODO
    }

    //handles DISC message sent from client to server  //maybe no need of this method
    private void disconnectOp(byte[] message){
        //TODO
    }



    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    } 


    
}
