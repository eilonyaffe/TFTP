package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpConnections<T> implements Connections<T> {

    protected ConcurrentHashMap<Integer, Pair<ConnectionHandler<T>, Boolean>> client_connections = new ConcurrentHashMap<>(); //neya changed
    
    @Override
    public boolean connect(int connectionId, ConnectionHandler<T> handler){ //eilon - kept generic, but will be blockingConnectionHandler
        //TODO implement
        if (!this.client_connections.containsKey(connectionId)){
            this.client_connections.get(connectionId).setSecond(true);
            return true;
        }
        //this.client_connections.remove(connectionId); //currently removes user with equal id to another user which is already logged in - should remove after sending him error pkg maybe?
        return false; //client is already exists in the network
        
    }

    public void basicConnect(int connectionId, ConnectionHandler<T> handler){
        this.client_connections.put(connectionId, new Pair<>(handler, false));
        
    }

    @Override
    public boolean send(int connectionId, T msg){
        //TODO implement
        this.client_connections.get(connectionId).getFirst().send(msg);
        return false; //eilon - change?
    }

    @Override
    public void disconnect(int connectionId){
        //TODO implement
        this.client_connections.remove(connectionId);
    }
}
