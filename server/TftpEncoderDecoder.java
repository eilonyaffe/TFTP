package bgu.spl.net.impl.tftp;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder

    protected List<Byte> bytes = new LinkedList<Byte>(); //Eilon

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this
        if(bytes.size()>=2 && nextByte==0){ //ey- maybe change. need to make sure this is the only way in which a client message ends
            byte[] bytesArray = new byte[bytes.size()];
            for (int i = 0; i < bytesArray.length; i++) {
                bytesArray[i] = bytes.get(i);
            }
            return bytesArray;
        }
        else{
            bytes.add(nextByte);
        }
        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        //TODO: implement this
        return message; //eilon
    }
}