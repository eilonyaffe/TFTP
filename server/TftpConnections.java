package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpConnections<T> implements Connections<T> {

    protected ConcurrentHashMap<Integer, ConnectionHandler<T>> client_connections = new ConcurrentHashMap<>();
    
    @Override
    public boolean connect(int connectionId, ConnectionHandler<T> handler){ //eilon - kept generic, but will be blockingConnectionHandler
        //TODO implement

        if (!this.client_connections.containsKey(connectionId)){
            this.client_connections.put(connectionId, handler);
            return true;
        }
        return false; //client is already exists in the network
    }

    @Override
    public boolean send(int connectionId, T msg){
        //TODO implement
        this.client_connections.get(connectionId).send(msg);
        return false; //eilon - change?
    }

    @Override
    public void disconnect(int connectionId){
        //TODO implement
        this.client_connections.remove(connectionId);
    }
}