package bgu.spl.net.impl.tftp;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder

    protected List<Byte> bytes = new LinkedList<Byte>(); //Eilon

    protected boolean awaitUTFEnd = false;
    protected boolean nextIsOpIndicator = false;
    protected boolean ongoing = false; //false if this is the very first byte from that message
    protected int packetSizeAccepted = -1;
    protected byte[] packetSizeArray = new byte[2];
    protected int messageType = -1;
    protected int counter = -1;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this
        bytes.add(nextByte);

        if(nextByte==0 && !this.ongoing){
            this.ongoing = true;
            this.nextIsOpIndicator = true;
        }

        else if(nextIsOpIndicator){
            this.messageType = nextByte;
            this.nextIsOpIndicator = false;

            if(this.messageType == 1 || this.messageType == 2 ||this.messageType == 7 ||this.messageType == 8){
                this.awaitUTFEnd = true;
            }
            else if(this.messageType==4 || this.messageType==5){
                this.counter = 2;
            }
            else if(this.messageType==9){
                this.counter = 1;
            }

            if(this.messageType==6 || this.messageType==10){
                return this.returnDecodedBytes();
            }
        }

        else{ //i.e, we are after the opcode prefix

            if(this.awaitUTFEnd && nextByte==0) //assuming zero byte won't appear in UTF. TODO make sure this assumption is correct
                return this.returnDecodedBytes();
            
            else if(this.messageType==9 && this.counter==1){
                this.counter = -1;
                this.awaitUTFEnd = true;
            }

            else if((this.messageType==4 || this.messageType==5) && this.counter==2)
                this.counter = 1;   

            else if((this.messageType==4 || this.messageType==5) && this.counter==1){
                if(this.messageType==4){
                    return this.returnDecodedBytes();
                }
                else{ //messageType==5
                    this.counter = -1;
                    this.awaitUTFEnd = true;
                }
            }
            else if(this.messageType==3){
                if(this.packetSizeAccepted<1){
                    this.packetSizeAccepted++;
                    this.packetSizeArray[this.packetSizeAccepted]=nextByte;
                }
                if(this.packetSizeAccepted==1){
                    this.counter = ( short ) ((( short ) packetSizeArray [0]) << 8 | ( short ) ( packetSizeArray [1]) ) +2; //plus two for incoming block number
                }
                else if(this.counter!=-1){
                    this.counter--;
                }
                if(this.counter==0){
                    return this.returnDecodedBytes();
                }
            }
        }
        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        //TODO: implement this
        return message; //eilon
    }


    public byte[] returnDecodedBytes() {
        byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytesArray.length; i++) {
            bytesArray[i] = bytes.get(i);
        }

        //resets all boolean indicators
        this.awaitUTFEnd = false;
        this.nextIsOpIndicator = false;
        this.ongoing = false; 
        this.packetSizeAccepted = -1;
        this.packetSizeArray = new byte[2];
        this.messageType = -1;
        this.counter = -1;

        return bytesArray;
    }

    public static void main(String[] args) { //delete later, just testing

        TftpEncoderDecoder encdec = new TftpEncoderDecoder();
        byte[] b = new byte[12];
        for(int i=0; i<=10; i++){
            b[i] = (byte) i;
        }
        b[11] = 0;
        for(byte bytebyte: b){
            byte[] ans = encdec.decodeNextByte(bytebyte);
            System.out.println(ans);
        }
    }
}