package bgu.spl.net.impl.tftp;
import bgu.spl.net.srv.Server;


public class TftpServer {
    //TODO: Implement this
    public static void main(String[] args) {

        connectionsHolder.start(); //initializes the connections db. a static object.
       
        Server.threadPerClient(
                7777, //port
                () -> new TftpProtocol(), //protocol factory
                () -> new TftpEncoderDecoder() //message encoder decoder factory
        ).serve();
    }
}

