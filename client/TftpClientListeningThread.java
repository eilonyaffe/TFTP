package bgu.spl.net.impl.tftp;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpClientListeningThread extends Thread {

    private final Socket socket;
    private final MessageEncoderDecoder<byte[]> encdec;
    private ByteArrayOutputStream  bufferForFileFromClient = new ByteArrayOutputStream ();

    public TftpClientListeningThread(Socket sock, MessageEncoderDecoder<byte[]> encdec) { //gets the socket of the client
        this.socket = sock;
        this.encdec = encdec;
    }

    @Override
    public void run() {
        BufferedInputStream in;
        try {
            in = new BufferedInputStream(this.socket.getInputStream());

            MessageEncoderDecoder<byte[]> encdec = new TftpEncoderDecoder();
            int read;

            while ((read = in.read()) >= 0) {
                byte[] msg = encdec.decodeNextByte((byte) read);
                if (msg != null) {
                    System.out.println(msg.toString());
                    byte opcode = msg[1];

                    if (opcode == 3){
                        dataPacketHandling(msg);
                    }

                    else if (opcode == 4){
                        short blockNumber = (short) (((short) msg[2]) << 8 | (short) (msg[3]) & 0x00ff);
                        System.out.println("ACK " + blockNumber);
                    }

                    else if(opcode == 5){
                        errorHandling(msg);
                    }

                    else if (opcode == 9){
                        bcastHandling(msg);
                    }

                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    // dont forget dirOrRRQinProcess = -1; currentPath = null;

    private void dataPacketHandling(byte[] msg){
        if (TftpClientKeyboardThread.dirOrRRQinProcess == 1){ //RRQ data packet
            short packetBlockNum = (short) (((short) msg[4]) << 8 | (short) (msg[5]) & 0x00ff);
            short packetSize = (short) (((short) msg[2]) << 8 | (short) (msg[3]) & 0x00ff);
            System.out.print("Handling DATA\nReceived: [");
            
            for (byte b : msg){
                System.out.print();
            }
            //System.out.println("packet block number: " + packetBlockNum + " packet size is: " + packetSize);
            byte[] onlyInfo = Arrays.copyOfRange(msg, 6, msg.length);
            

            try (FileOutputStream fos = new FileOutputStream(TftpClientKeyboardThread.currentPathRRQ.toString(), true)) {
                    fos.write(onlyInfo);

                if(packetSize<512){ //in case RRQ is fully done
                    String nameFile = TftpClientKeyboardThread.currentRRQfileName;
                    System.out.println("RRQ " + nameFile + " compleate");
                    TftpClientKeyboardThread.dirOrRRQinProcess = -1;
                    TftpClientKeyboardThread.currentPathRRQ = null;
                    TftpClientKeyboardThread.currentRRQfileName = null;
                }
                sendACK(packetBlockNum);
            } catch (IOException e) {}
        }

    }

    private void errorHandling(byte[] msg){
        byte[] errInBytes = Arrays.copyOfRange(msg,4, msg.length - 1); 

        // Convert byte array to string using UTF-8
        String errString = new String(errInBytes, StandardCharsets.UTF_8);
        short errNumber = (short) (((short) msg[2]) << 8 | (short) (msg[3]) & 0x00ff);
        System.out.println("Error " + errNumber + " " + errString);
    }

    private void bcastHandling(byte[] msg){
        String delOrAdd;

        if (msg[2] == 1)
            delOrAdd = "add";
        else
            delOrAdd = "del";

        byte[] filenameInBytes = Arrays.copyOfRange(msg, 3, msg.length - 1); 

        // Convert byte array to string using UTF-8
        String filename = new String(filenameInBytes, StandardCharsets.UTF_8);
        System.out.println("BCAST " + delOrAdd + " " + filename);
    }

    //sends ACK 0 when needed
    private void sendACK(int blockNum){
        short a = 4;    
        byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
        byte[] b_bytes = new byte []{( byte ) ( blockNum >> 8) , ( byte ) ( blockNum & 0xff ) };
        byte[] ack = new byte[]{a_bytes[0], a_bytes[1], b_bytes[0], b_bytes[1]};
        
        try {
            this.socket.getOutputStream().write(encdec.encode(ack));
            this.socket.getOutputStream().flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
