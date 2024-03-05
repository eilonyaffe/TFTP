package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.nio.file.*;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate; //eliya - is it necessary to make it volatile
    private int connectionId;
    private Connections<byte[]> connections;
    private boolean logged_in = false;


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

        if(!logged_in){
            if(opcode == 7){
                boolean successfulLogIn = this.logOperation(message);
                if(successfulLogIn){
                    //TODO: return ACK
                    this.logged_in = true;
                    connectionsHolder.connectionsObj.send(this.connectionId, this.ackOperation(0));
                    System.out.println("LOGRQ was completed successfully: " + successfulLogIn);
                }
                else{
                    //TODO: return ERROR
                    connectionsHolder.connectionsObj.sendInactive(this.connectionId, this.errorOperation(7)); //when to use error 6:User not logged in - Any opcode received before Login completes?
                }
            }

            else{          
                //TODO insert error of user that wasn't logged in, and made non-LOGRQ request (he is in inactive_connections)
            }
        }

        else{ //user is logged in
            if (opcode == 1)
                readRequest(message);

            else if (opcode == 2)
                writeRequest(message);

            else if (opcode == 3)
                dataPacketOp(message);

            // else if (opcode == 4)
                // ackOperation(message);

            // else if (opcode == 5)
            //     //errorOperation(message);

            else if (opcode == 6)
                listingRequest(message);

            else if (opcode == 7){ //logged in, and wanted to do another LOGRQ. will send error
                connectionsHolder.connectionsObj.send(this.connectionId, this.errorOperation(7)); //is error 7 correct for this case?
            }
            
            else if (opcode == 8)
                deleteFile(message);

            else if (opcode == 9)
                bcastOperation(message);

            else if (opcode == 10)
                disconnectOp(message);

        }//end of else
    }

    //handles RRQ message sent from client to server
    private void readRequest(byte[] message){ //assumes there is no zero byte in the filename
        //TODO
        byte[] filenameInBytes = Arrays.copyOfRange(message,2, message.length - 2);

        // Convert byte array to string using UTF-8
        String filename = new String(filenameInBytes, StandardCharsets.UTF_8);
        Path filePath = Paths.get("Files", filename); //constructs the path to the file
        boolean isExist = false;
        try {
            isExist = Files.exists(filePath);
        } catch (SecurityException e) {} //needed?? maybe not? - neya

        //TODO
        if(isExist){
            //send DATA pkg with the claimed file
        }

        else{
            //send error pkg
        }
    }

    //handles WRQ message sent from client to server
    private void writeRequest(byte[] message){
        //TODO

        byte[] filenameInBytes = Arrays.copyOfRange(message,2, message.length - 2);

        // Convert byte array to string using UTF-8
        String filename = new String(filenameInBytes, StandardCharsets.UTF_8);
        Path filePath = Paths.get("Files", filename); //constructs the path to the file
        boolean isExist = false;
        try {
            isExist = Files.exists(filePath);
        } catch (SecurityException e) {} //needed?? maybe not? - neya

        //TODO
        if(isExist){
            //get DATA pkg with the claimed file
        }

        else{
            //send error pkg
        }  
    }

    //handles DATA message sent from server to client
    private void dataPacketOp(byte[] message){
        //TODO
    }

    //handles ACK message sent from server to client
    private byte[] ackOperation(int blockNum){
        short a = 4;
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        byte[] b_bytes = new byte []{( byte ) ( blockNum >> 8) , ( byte ) ( blockNum & 0xff ) };
        byte[] ack = new byte[]{a_bytes[0], a_bytes[1], b_bytes[0], b_bytes[1]};
        return ack;
    }

    //handles ERROR message sent from server to client
    private byte[] errorOperation(int errorCode){
        ErrorsHolderDict errorsDict = ErrorsHolderDict.getInstance();
        short opcode = 5;
        short eCode = (short)errorCode;
        byte[] op_bytes = new byte []{( byte ) ( opcode >> 8) , ( byte ) ( opcode & 0xff ) };
        byte[] eNum_bytes = new byte []{( byte ) ( eCode >> 8) , ( byte ) ( eCode & 0xff ) };
        byte[] eStr_bytes = errorsDict.getErrorByNumber(errorCode).getBytes();
        
        int totalLength = op_bytes.length + eNum_bytes.length + eStr_bytes.length + 1; //+1 for the 0 last byte
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        buffer.put(op_bytes);
        buffer.put(eNum_bytes);
        buffer.put(eStr_bytes);
        buffer.put((byte) 0);

        return buffer.array(); 
    }

    //handles DIRQ message sent from client to server
    private void listingRequest(byte[] message){
        //TODO
    }

    //handles LOGRQ message sent from client to server
    private boolean logOperation(byte[] message){
        ConnectionHandler<byte[]> BCH = connectionsHolder.connectionsObj.inactive_connections.get(connectionId);
        String name = "";
        try{
            name = new String(message, "UTF-8"); //should check if legal?
        } catch(UnsupportedEncodingException e){}
        System.out.println("name entered was: "+name);
        int checkUserName = name.hashCode(); //assuming valid input from user
        if(connectionsHolder.connectionsObj.active_connections.containsKey(checkUserName)) return false; //there's already a client with that name. TODO also need to terminate client? probably not
        else{ //was inactive, has legal unique name, now should be activated
            connectionsHolder.connectionsObj.inactive_connections.remove(this.connectionId);
            this.connectionId = checkUserName;
            connectionsHolder.connectionsObj.connect(this.connectionId, BCH); //will insert to active connections
            return true;
        }
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
