package bgu.spl.net.impl.tftp;

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
    public void process(byte[] message) { //neya note: product of each msg sent by "send" method of connections, process is void because it is bidi
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

        else if (opcode == 7)
            logOperation(message);

        else if (opcode == 8)
            deleteFile(message);

        else if (opcode == 9)
            bcastOperation(message);

        else if (opcode == 10)
            disconnectOp(message);

    }

    //handles RRQ message sent from client to server
    private void readRequest(byte[] message){
        //TODO
    }

    //handles WRQ message sent from client to server
    private void writeRequest(byte[] message){
        //TODO
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
    private void logOperation(byte[] message){
        //TODO
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
