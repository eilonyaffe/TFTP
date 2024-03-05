package bgu.spl.net.impl.tftp;

public class connectionsHolder {
    // public static TftpConnections<byte[]> activeConnections = new TftpConnections<byte[]>();
    public static TftpConnections<byte[]> activeConnections;
    public int userID = 0;

    public static void start(){
        activeConnections =  new TftpConnections<byte[]>();
    }

    public static TftpConnections<byte[]> get(){ //TODO maybe delete later
        return activeConnections;
    }

    public static int getUniqueID(){
        return activeConnections.client_connections.size();
    }
}
