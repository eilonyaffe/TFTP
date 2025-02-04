package bgu.spl.net.impl.tftp;

import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpConnections<T> implements Connections<T> {

    protected ConcurrentHashMap<Integer, ConnectionHandler<T>> active_connections = new ConcurrentHashMap<>(); //neya changed
    protected ConcurrentHashMap<Integer, ConnectionHandler<T>> inactive_connections = new ConcurrentHashMap<>(); //neya changed
    protected int inactive_counter = -1;

    
    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler){ //eilon - kept generic, but will be blockingConnectionHandler
        //TODO implement
        this.active_connections.put(connectionId, handler);
        System.out.println("made connection to active!");
    }

    public void basicConnect(int connectionId, ConnectionHandler<T> handler){
        this.inactive_connections.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg){
        //TODO implement
        this.active_connections.get(connectionId).send(msg);
        return false; //eilon - change?
    }

    public boolean sendInactive(int connectionId, T msg){
        //TODO implement
        this.inactive_connections.get(connectionId).send(msg);
        return false; //eilon - change?
    }

    @Override
    public void disconnect(int connectionId){
        //TODO implement
        this.active_connections.remove(connectionId);
    }

    public void disconnectInactive(int connectionId){
        //TODO implement
        this.inactive_connections.remove(connectionId);
    }

    public int getUniqueInactiveID(){
        //TODO implement
        this.inactive_counter++;
        return this.inactive_counter;
    }
}
