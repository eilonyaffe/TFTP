// package bgu.spl.net.impl.tftp;
// public class TftpClient {
//     //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
//     public static void main(String[] args) {
//         System.out.println("implement me!");
//     }
// }

package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;



// // package bgu.spl.net.impl.tftp;
// public class TftpClient {
//     //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
//     public static void main(String[] args) {
//         System.out.println("implement me!");
//         try {
//             Socket sock = new Socket("127.0.0.1", 7777);
//             short a = 7;
//             byte[] a_bytes = new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
//             byte[] usernameInBytes = stringToBytes("neya");
//             int packetSize = usernameInBytes.length+3;
//             byte[] ans = new byte[packetSize];
//             ans[0] = a_bytes[0];
//             ans[1] = a_bytes[1];
//             for(int i=2;i<ans.length-1;i++){
//                 ans[i] = usernameInBytes[i-2];
//             }
//             ans[ans.length-1] = (byte)0;

//             sock.getOutputStream().write(ans);
            

//         } catch (UnknownHostException e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         } catch (IOException e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         }
        

//     }

//        public static byte[] stringToBytes(String str){
//         try{
//             return str.getBytes("UTF-8");
//         }catch(UnsupportedEncodingException e){
//             e.printStackTrace();
//             return null;
//         }
//     }
// }


// package bgu.spl.net.impl.tftp;
public class TftpClient {
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    public static void main(String[] args) {
        try {
            Socket sock = new Socket("127.0.0.1", 7777);
            System.out.println("Connected to the server!");
            TftpClientKeyboardThread keyboardThread = new TftpClientKeyboardThread(sock, new TftpEncoderDecoder());
            Thread thread = new Thread(keyboardThread);
            thread.start();
            
        //TODO listening thread 
            

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

    }

       public static byte[] stringToBytes(String str){
        try{
            return str.getBytes("UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }
    }
}







// public class TftpClient {
//     //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    

//     public static void main(String[] args) {
//         System.out.println("implement me!");

//         if (args.length == 0) {
//             args = new String[]{"localhost", "hello"};
//         }

//         if (args.length < 2) {
//             System.out.println("you must supply two arguments: host, message");
//             System.exit(1);
//         }

//         //BufferedReader and BufferedWriter automatically using UTF-8 encoding
//         try (Socket sock = new Socket("127.0.0.1", 7777); //change port from args too
//             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//             BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream())) {

//             if (!sock.isClosed())
//                 System.out.println("connected to server" + args[0]);
//             else{
//                 System.out.println("closed");
//             }
            

//             TftpClientKeyboardThread keyboardThread = new TftpClientKeyboardThread(sock, out, new TftpEncoderDecoder());
//             Thread thread = new Thread(keyboardThread);
//             thread.start();

//             // System.out.println("sending message to server");
//             // out.write(args[1]);
//             // out.newLine();
//             // out.flush();

//             // System.out.println("awaiting response");
//             // String line = in.readLine();
//             // System.out.println("message from server: " + line);
//         } catch (IOException ex){}
        
//     }
// }