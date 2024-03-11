package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpClientKeyboardThread extends Thread {
    BufferedReader keyboardin = new BufferedReader(new InputStreamReader(System.in));
    //BufferedOutputStream outa;
    private final Socket socket;
    private final MessageEncoderDecoder<byte[]> encdec;
    protected volatile static int dirOrRRQinProcess; //indicates if there is a process of DIRQ (equals 0) or RRQ (equals 1) or none (-1)
    protected volatile static Path currentPathRRQ;
    protected volatile static String currentRRQfileName;

    public TftpClientKeyboardThread(Socket sock, MessageEncoderDecoder<byte[]> encdec) { //gets the socket of the client
        this.socket = sock;  
        this.encdec = encdec; 
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
              

                if(command.equals("LOGRQ")){
                   suitedPacket = this.logrqPacketCreator(words[1]);
                   this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                   this.socket.getOutputStream().flush();
                }

                else if(command.equals("DELRQ")){
                    suitedPacket = this.deleqPacketCreator(words[1]);
                    this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                    this.socket.getOutputStream().flush();
                }

                else if(command.equals("RRQ")){
                    Path path = Paths.get(words[1]); //constructs the path to the file
                    Path filePath = path.toAbsolutePath();
                    boolean isExist = false;
                    try {
                        isExist = Files.exists(filePath);
                    } catch (SecurityException e) {}

                    if (isExist)
                        System.out.println("file already exists");
                    else{
                        Files.createFile(filePath);
                        suitedPacket = this.rrqPacketCreator(words[1]);
                        dirOrRRQinProcess = 1;
                        currentPathRRQ = filePath;
                        currentRRQfileName = words[1];
                        this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                        this.socket.getOutputStream().flush();
                    }
                }

                else if(command.equals("WRQ")){

                    Path path = Paths.get(words[1]); //constructs the path to the file
                    Path filePath = path.toAbsolutePath();
                    boolean isExist = false;
                    try {
                        isExist = Files.exists(filePath);
                    } catch (SecurityException e) {}

                    if (!isExist)
                        System.out.println("file does not exists");
                    else{
                        Files.createFile(filePath);
                        suitedPacket = this.wrqPacketCreator(words[1]);
                        this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                        this.socket.getOutputStream().flush();
                    }
                }

                else if(command.equals("DIRQ")){
                    suitedPacket = this.dirqPacketCreator();
                    this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                    this.socket.getOutputStream().flush();

                }

                else if(command.equals("DISC")){
                    suitedPacket = this.discPacketCreator();
                    dirOrRRQinProcess = 0;
                    this.socket.getOutputStream().write(encdec.encode(suitedPacket));
                    this.socket.getOutputStream().flush();
                }
                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected byte[] logrqPacketCreator(String username){
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

    protected byte[] deleqPacketCreator(String filename){
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

    protected byte[] rrqPacketCreator(String filename){
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

    protected byte[] wrqPacketCreator(String filename){
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
