package bgu.spl.net.impl.tftp;

public class connectionsHolder {
    // public static TftpConnections<byte[]> activeConnections = new TftpConnections<byte[]>();
    public static TftpConnections<byte[]> connectionsObj;

    public static void start(){
        connectionsObj =  new TftpConnections<byte[]>();
    }

    public static TftpConnections<byte[]> get(){ //TODO maybe delete later
        return connectionsObj;
    }

    public static int getUniqueID(){
        return connectionsObj.getUniqueInactiveID();
    }
}
