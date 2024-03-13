package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpClientKeyboardThread extends Thread {
    private final Object sharedLock;
    BufferedReader keyboardin = new BufferedReader(new InputStreamReader(System.in));
    //BufferedOutputStream outa;
    private final Socket socket;
    private final MessageEncoderDecoder<byte[]> encdec;
    protected volatile static int dirOrRRQinProcess; //indicates if there is a process of DIRQ (equals 0) or RRQ (equals 1) or none (-1)
    protected volatile static Path currentPathRRQ;
    protected volatile static String currentRRQfileName;
    protected volatile static boolean disconnectRequested = false;
    protected volatile static boolean closeFlag = false;
    private boolean sending_data = false;
    private int blockNumCounter;
    private boolean nextends = false;
    private FileInputStream fileoutstream;

    public TftpClientKeyboardThread(Socket sock, MessageEncoderDecoder<byte[]> encdec, Object lock) { //gets the socket of the client
        this.socket = sock;  
        this.encdec = encdec; 
        this.sharedLock = lock;
    }

    @Override
    public void run() {
        try {
            String userInput;
            //BufferedOutputStream out = new BufferedOutputStream(this.socket.getOutputStream());
            System.out.println("inside keboard thread run");
            while ((userInput = keyboardin.readLine()) != null) {
                System.out.println(userInput);
                String[] words = userInput.split("\\s+");
                String command = words[0];
                System.out.println(command);
                //keyboard thread commands
                byte[] suitedPacket;

                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < words.length; i++){
                    sb.append(words[i] + " ");
                }
                String word = sb.toString();
                word = word.substring(0, word.length() - 1); //remove last space
                System.out.println(word);

                if(command.equals("LOGRQ")){
                   suitedPacket = this.logrqPacketCreator(word);
                   this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                   this.socket.getOutputStream().flush();
                }

                else if(command.equals("DELRQ")){
                    suitedPacket = this.deleqPacketCreator(word);
                    this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                    this.socket.getOutputStream().flush();
                }

                else if(command.equals("RRQ")){
                    Path path = Paths.get(word); //constructs the path to the file
                    Path filePath = path.toAbsolutePath();
                    boolean isExist = false;
                    try {
                        isExist = Files.exists(filePath);
                    } catch (SecurityException e) {}

                    if (isExist)
                        System.out.println("file already exists");
                    else{
                        Files.createFile(filePath);
                        suitedPacket = this.rrqPacketCreator(word);
                        dirOrRRQinProcess = 1;
                        currentPathRRQ = filePath;
                        currentRRQfileName = word;
                        this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                        this.socket.getOutputStream().flush();
                    }
                }

                else if(command.equals("WRQ")){

                    Path path = Paths.get(word); //constructs the path to the file
                    Path filePath = path.toAbsolutePath();
                    boolean isExist = false;
                    System.out.println(filePath.toString());
                    try {
                        isExist = Files.exists(filePath);
                    } catch (SecurityException e) {}

                    if (!isExist)
                        System.out.println("file does not exists");
                    else{
                        //sends the file in data packets to server waits for ack
                        this.wrqPacketRequestCreator(word);

                        // synchronized (sharedLock) {
                        //     try {
                        //         sharedLock.wait();
                        //     } catch (InterruptedException e) {}
                        // }
                        this.wrqPacketDataCreator(word);

                    }
                }

                else if(command.equals("DIRQ")){
                    suitedPacket = this.dirqPacketCreator();
                    dirOrRRQinProcess = 0;
                    this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                    this.socket.getOutputStream().flush();

                }

                else if(command.equals("DISC")){
                    suitedPacket = this.discPacketCreator();
                    disconnectRequested = true;
                    this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                    this.socket.getOutputStream().flush();
                    break; //no need for keyboard thread from now on
                }

                else{
                    System.out.println("Invalid Command");
                }
                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("keyboard thread is closed");
    }

    private byte[] logrqPacketCreator(String username){
        short a = 7;
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        byte[] usernameInBytes = stringToBytes(username);
        int packetSize = usernameInBytes.length+3;
        byte[] ans = new byte[packetSize];
        ans[0] = a_bytes[0];
        ans[1] = a_bytes[1];
        for(int i=2;i<ans.length-1;i++){
            ans[i] = usernameInBytes[i-2];
        }
        ans[ans.length-1] = (byte)0;
        return ans;
    }

    private byte[] deleqPacketCreator(String filename){
        short a = 8;
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        byte[] filenameInBytes = stringToBytes(filename);
        int packetSize = filenameInBytes.length+3;
        byte[] ans = new byte[packetSize];
        ans[0] = a_bytes[0];
        ans[1] = a_bytes[1];
        for(int i=2;i<ans.length-1;i++){
            ans[i] = filenameInBytes[i-2];
        }
        ans[ans.length-1] = (byte)0;
        return ans;
    }

    private byte[] rrqPacketCreator(String filename){
        short a = 1;
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        byte[] filenameInBytes = stringToBytes(filename);
        int packetSize = filenameInBytes.length+3;
        byte[] ans = new byte[packetSize];
        ans[0] = a_bytes[0];
        ans[1] = a_bytes[1];
        for(int i=2;i<ans.length-1;i++){
            ans[i] = filenameInBytes[i-2];
        }
        ans[ans.length-1] = (byte)0;
        return ans;
    }

    protected byte[] wrqPacketRequestCreator(String filename){
        short a = 2;
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        byte[] filenameInBytes = stringToBytes(filename);
        int packetSize = filenameInBytes.length+3;
        byte[] ans = new byte[packetSize];
        ans[0] = a_bytes[0];
        ans[1] = a_bytes[1];
        for(int i=2;i<ans.length-1;i++){
            ans[i] = filenameInBytes[i-2];
        }
        ans[ans.length-1] = (byte)0;
        return ans;
    }


    private void wrqPacketDataCreator(String fileName){
        //TODO
        System.out.println("sends data");
        try{
            if(!this.sending_data){
                this.sending_data = true;
                this.blockNumCounter = 0;
                Path path = Paths.get(fileName); //constructs the path to the file
                Path filePath = path.toAbsolutePath();
                File fileToSend = new File(filePath.toString());
                this.fileoutstream = new FileInputStream(fileToSend);
            }
    
            byte[] slicedData;
    
            if(this.sending_data){
                if(this.fileoutstream.available()>=0){
                    if(this.fileoutstream.available()>=512){
                        slicedData = new byte[512];
                    }
                    else{
                        slicedData = new byte[this.fileoutstream.available()];
                        this.nextends = true;
                    }
                    this.fileoutstream.read(slicedData);
                    blockNumCounter++;
                    byte[] readyPacket = createDataPacket(slicedData, blockNumCounter);
                    //connectionsHolder.connectionsObj.send(this.connectionId, readyPacket);
                    this.socket.getOutputStream().write(encdec.encode(readyPacket));
                    this.socket.getOutputStream().flush();
                }
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
















    protected byte[] dirqPacketCreator(){
        short a = 6;
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        return a_bytes;
    }

    protected byte[] discPacketCreator(){
        short a = 10;
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        return a_bytes;
    }

    public static byte[] stringToBytes(String str){
        try{
            return str.getBytes("UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }
    }

    // public static void main(String[] args) throws IOException {
    //     TftpClientKeyboardThread kbthread = new TftpClientKeyboardThread();
    //     kbthread.start();

    // }

}
