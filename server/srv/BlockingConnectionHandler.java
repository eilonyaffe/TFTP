package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.BidiMessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import bgu.spl.net.impl.tftp.connectionsHolder;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            //new from here
            int connectionId = connectionsHolder.getUniqueID();
            protocol.start(connectionId ,(Connections<T>) connectionsHolder.activeConnections);
            System.out.println("started protocol, id is: "+connectionId);

            connectionsHolder.activeConnections.basicConnect(connectionId, (ConnectionHandler) this); //TODO check if works. need to change from 1967!! just placeholder
            System.out.println("inserted to connections, not activated yet");
            //until here

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        //IMPLEMENT IF NEEDED
        try{
            if(msg != null){
                out.write(encdec.encode(msg));
                out.flush();
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
        //eilon- need to also handle concurrency issues!
    }
}
