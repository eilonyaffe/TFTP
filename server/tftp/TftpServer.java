package bgu.spl.net.impl.tftp;

import java.util.function.Supplier;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Server;
import bgu.spl.net.impl.tftp.connectionsHolder;


public class TftpServer {
    //TODO: Implement this
    public static void main(String[] args) {

        connectionsHolder.start(); //initializes the connections db. a static object.
        
        // you can use any server... 
        Server.threadPerClient(
                7777, //port
                () -> new TftpProtocol(), //protocol factory
                () -> new TftpEncoderDecoder() //message encoder decoder factory
        ).serve();
    }
}

