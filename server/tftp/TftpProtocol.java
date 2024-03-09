package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.*;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate; //eliya - is it necessary to make it volatile
    private int connectionId;
    private Connections<byte[]> connections;
    private boolean logged_in = false;
    private boolean handling_data = false;
    private String incomingFileName = null;
    private ArrayList<byte[]> incomingData = new ArrayList<>();
    //private int currentBlockNumCounter = 0; - used for synchronized ACK & RRQ - meantime we don't need it - neya
    private Object lock = new Object();

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
        System.out.println("got msg with opcode: "+opcode);
        if(!logged_in){
            if(opcode == 7){
                logOperation(message);            
            }
            else{          
                //TODO eliya insert error of user that wasn't logged in, and made non-LOGRQ request (he is in inactive_connections) - done
                connectionsHolder.connectionsObj.sendInactive(this.connectionId, this.errorOperation(6));
            }
        }

        else{ //user is logged in
            //TODO should also add check if handling_data? can packets other than data be sent by client in that time?
            if (opcode == 1)
                readRequest(message);

            else if (opcode == 2){
                writeRequest(message);
            }

            else if (opcode == 3){
                System.out.println("got data");
                dataPacketIn(message);
            }
            else if (opcode == 4)
                acceptAckOperation(message);

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
        byte[] filenameInBytes = Arrays.copyOfRange(message,2, message.length - 1); //was message.length - 2 , eilon changed to -1, this is exclusive

        // Convert byte array to string using UTF-8
        String filename = new String(filenameInBytes, StandardCharsets.UTF_8);
        Path path = Paths.get("server", "Files", filename); //constructs the path to the file
        Path filePath = path.toAbsolutePath();
        System.out.println(filePath); //to delete
        boolean isExist = false;
        try {
            isExist = Files.exists(filePath);
        } catch (SecurityException e) { connectionsHolder.connectionsObj.send(this.connectionId, this.errorOperation(2)); } //needed?? maybe not? - neya

        //TODO
        if(isExist){
            System.out.println("FILE EXIST"); //todelete
            //send DATA pkg with the claimed file
            this.dataFileOut(filename);
        }

        else{
            //send error pkg - done
            connectionsHolder.connectionsObj.send(this.connectionId, this.errorOperation(1));
        }
    }

    //handles WRQ message sent from client to server
    private void writeRequest(byte[] message){
        //TODO

        byte[] filenameInBytes = Arrays.copyOfRange(message,2, message.length - 1);

        // Convert byte array to string using UTF-8
        String filename = new String(filenameInBytes, StandardCharsets.UTF_8);
        Path path = Paths.get("server", "Files", filename); //constructs the path to the file
        Path filePath = path.toAbsolutePath();
        //System.out.println(filePath); //to delete

        boolean isExist = false;
        try {
            isExist = Files.exists(filePath);
        } catch (SecurityException e) {connectionsHolder.connectionsObj.send(this.connectionId, this.errorOperation(2));} //needed?? maybe not? - neya

        if(isExist){
            connectionsHolder.connectionsObj.send(this.connectionId, this.errorOperation(5)); //the file already exists in the server
        }
        else{
            this.dataInPrep(message); //prepares for data packets to be sent by the client
            connectionsHolder.connectionsObj.send(this.connectionId, this.ackOperation(0)); 
        }
    }

    //handles ACK message sent from server to client
    private void acceptAckOperation(byte[] message){ //needed??
        short blockNumber = (short) (((short) message[2]) << 8 | (short) (message[3]) & 0x00ff);
        System.out.println(blockNumber + "client sent");
        // if (blockNumber == currentBlockNumCounter){ 
        //     synchronized (lock) {
        //         // notify to send next packet
        //         this.notify();
        //     }
        // }
    }

    //sends ACK 0 when needed
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
    private void logOperation(byte[] message){
        ConnectionHandler<byte[]> BCH = connectionsHolder.connectionsObj.inactive_connections.get(connectionId);
        String name = "";
        try{
            name = new String(message, "UTF-8"); //should check if legal?
        } catch(UnsupportedEncodingException e){}
        System.out.println("name entered was: "+name);
        int checkUserName = name.hashCode(); //assuming valid input from user

        if(connectionsHolder.connectionsObj.active_connections.containsKey(checkUserName)){
            connectionsHolder.connectionsObj.sendInactive(this.connectionId, this.errorOperation(7)); //TODO correct error code?
        }

        else{ //was inactive, has legal unique name, now should be activated
            connectionsHolder.connectionsObj.inactive_connections.remove(this.connectionId);
            this.connectionId = checkUserName;
            connectionsHolder.connectionsObj.connect(this.connectionId, BCH); //will insert to active connections
            this.logged_in = true;
            connectionsHolder.connectionsObj.send(this.connectionId, this.ackOperation(0));
            System.out.println("LOGRQ was completed successfully!");
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

    //handles DATA message sent from client to server
    private void dataPacketIn(byte[] message){
        //TODO
        short packetSize = (short) (((short) message[2]) << 8 | (short) (message[3]) & 0x00ff);
        short packetBlockNum = (short) (((short) message[4]) << 8 | (short) (message[5]) & 0x00ff);
        System.out.println("packet block number: " + packetBlockNum + " packet size is: " + packetSize);
        this.incomingData.add(Arrays.copyOfRange(message, 6, message.length));

        if(packetSize<518){ //+6 because always includes 6 prefix bytes. 518 is a full packet
            this.saveFile(this.incomingFileName);
        }
        connectionsHolder.connectionsObj.send(this.connectionId, this.ackOperation(packetBlockNum));
        //TODO maybe wait until client sends another packet?
    }

    //makes preliminary steps for handling dataIn
    private void dataInPrep(byte[] message){
        byte[] filenameInBytes = Arrays.copyOfRange(message,2, message.length - 1);
        String filename = new String(filenameInBytes, StandardCharsets.UTF_8);
        this.incomingFileName = filename;
        this.handling_data = true; //need this?

        //TODO eliya create a file by that name in the server - done
        Path path = Paths.get("server", "Files", filename); //constructs the path to the file
        Path filePath = path.toAbsolutePath();

        try {
            // Create the file
            Files.createFile(filePath);
            System.out.println("File created successfully at " + filePath); //to delete
        } catch (IOException e) {
            connectionsHolder.connectionsObj.sendInactive(this.connectionId, this.errorOperation(2));
        }

        this.incomingData.clear();
    }

    //save file after finished dataIn
    private void saveFile(String fileName){
        int totalLength = 0;
        for(byte[] b: this.incomingData){
            totalLength += b.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        for(byte[] d: this.incomingData){
            buffer.put(d);
        }
        byte[] file = buffer.array(); //TODO eliya make sure this won't be a too big length. maybe adding straight to the file for every byte is safer

        //TODO eliya saves the data "file" to the file in the name this.fileName, that was created before

        //TODO need to do BCAST here to notify about file added to the server, to all active clients

            //keep last
        this.handling_data = false;
        this.incomingData.clear();
        this.incomingFileName = null; 

    }

    //handles sending packets to client following an RRQ by the client
    private void dataFileOut(String fileName){
        //TODO
        Path path = Paths.get("server", "Files", fileName); //constructs the path to the file
        Path filePath = path.toAbsolutePath();
        System.out.println(filePath); //to delete

        File fileToSend = new File(filePath.toString());
        byte[] slicedData;
        int blockNumCounter = 0;

        try(FileInputStream fis = new FileInputStream(fileToSend)){
            while(fis.available()>0){
                if(fis.available()>=512){
                    slicedData = new byte[512];
                }
                else{
                    slicedData = new byte[fis.available()];
                }
                fis.read(slicedData);
                blockNumCounter++;
                //this.currentBlockNumCounter = blockNumCounter;
                byte[] readyPacket = createDataPacket(slicedData, blockNumCounter);
                connectionsHolder.connectionsObj.send(this.connectionId, readyPacket);
                //TODO need to make him wait from here, until the client sent suitable ack
                // synchronized (lock) { 
                //     try {
                //         // wait for acknowledgement
                //         lock.wait();
                //     } catch (InterruptedException e) {
                //         e.printStackTrace();
                //     }
                // }
            }

        } catch(IOException e){}
    }

    private byte[] createDataPacket(byte[] rawData, int blockNumber){
        short a = 3;
        byte[] opdcodeBytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        byte[] packetSizeBytes = new byte []{( byte ) ( rawData.length >> 8) , ( byte ) ( rawData.length & 0xff ) };
        byte[] blockNumBytes = new byte []{( byte ) ( blockNumber >> 8) , ( byte ) ( blockNumber & 0xff ) };


        int totalLength = 6 + rawData.length;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        buffer.put(opdcodeBytes);
        buffer.put(packetSizeBytes);
        buffer.put(blockNumBytes);
        buffer.put(rawData);

        byte[] dataPacket = buffer.array(); 
        return dataPacket;
    }


    //handles DATA message sent from server to client
    private void dataDirOut(){
        //TODO eliya- implement similiarly to dataFileOut
        ArrayList<String> fileNames = new ArrayList<>();
        File directory = new File("C:\\Users\\User\\projects\\Skeleton\\server\\Files"); //eliya how to access correctly?
        File[] files = directory.listFiles();
        for(File f: files){
            fileNames.add(f.getName());
        }
        byte[] zero = new byte[]{0};
        int blocksCounter = 0;
        while(!fileNames.isEmpty()){
            ByteBuffer buffer = ByteBuffer.allocate(512);
            while(fileNames.get(0).getBytes().length<buffer.remaining() && !fileNames.isEmpty()){
                buffer.put(fileNames.get(0).getBytes());
                buffer.put(zero);
                fileNames.remove(0);
            }
            blocksCounter++;
            //eliya- need to slice the buffer to call buffer.array() on it. there might be unfilled items at the end of the buffer.
            byte[] fullBuf = buffer.array();
            // byte[] slicedbuff
            // byte[] readyPacket = createDataPacket(slicedbuff, blocksCounter);
            // connectionsHolder.connectionsObj.send(this.connectionId, readyPacket);
            //TODO need to make him wait from here, until the client sent suitable ack
        }
    }


    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        return shouldTerminate;
    } 

    // public static void main(String[] args) { //testing, delete later
    //     TftpProtocol p = new TftpProtocol();
    //     p.dataDirOut();
    // }
}
