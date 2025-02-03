package de.igelstudios.substitution;

public class NetworkBuffer {
    private int pointer;
    private byte[] buffer;

    public NetworkBuffer(byte[] buffer){
        this.buffer = buffer;
    }

    public String readString(int length){
        byte[] buf = new byte[length];
        System.arraycopy(buffer,pointer,buf,0,length);
        pointer += length;
        return new String(buf);
    }

    public String readStringInc(int inc){
        int length = 0;
        while (buffer[pointer + length] != 0 && buffer[pointer + length] != 27)length++;
        String data = readString(length);
        pointer += inc - length;
        return data;
    }

    public String readString(){
        int length = 0;
        while (buffer[pointer + length] != 0 && buffer[pointer + length] != 27)length++;
        String data = readString(length);
        pointer++;
        return data;
    }

    public int readNumber(){
        int val = buffer[pointer];
        val += (buffer[pointer + 1] << 8);
        val += (buffer[pointer + 2] << 16);
        val += (buffer[pointer + 3] << 24);
        pointer += 4;
        return val;
    }

    public int getReadLength() {
        return pointer;
    }

    public int getLength(){
        return buffer.length;
    }
}
