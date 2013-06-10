/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package css;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 *
 * @author crapson
 */
public class KeyFinder {
    
    int bits25=0;
    int bits17=0;
    int carry = 0;
    
    KeyFinder(String sourceFile)
    {
        System.out.println("File: " + sourceFile);
        
        String extension = sourceFile.substring(sourceFile.lastIndexOf(".") + 1);
        
        System.out.println("File extension: " + extension);
        
        byte[] header = getHeader(extension);
        
        System.out.println("File header to search for: " + byteArrayString(header));
        
        getKey(sourceFile, header);
    }
    
    private byte[] getKey(String sourceFile, byte[] header)
    {
        byte[] key = null;
        
        try{
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(sourceFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            byte[] decryptedHeader = new byte[header.length];
            byte b;
            int finalKey = 0;
            
            generateKeys((int)1);
            //Read File character By character
            for (int i = 0; i < header.length; i++)   {
                if(in.available() != 0)
                {
                    b = in.readByte();
                    finalKey = bit8adder(LFSR17(), LFSR25());
                    decryptedHeader[i] = (byte) (b ^ finalKey);
                }
            }
            System.out.println(byteArrayString(decryptedHeader));

        }
        catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e);
            System.err.println("Error: " + e.getMessage());
        }
        
        return key;
    }
    
    private byte[] getHeader(String extension)
    {
        byte[] header = null;
        
        if(extension.equals("txt"))
        {
        }
        else if(extension.equals("png"))
        {
            header = hexStringToByteArray("89504E470D0A1A0A");
        }
        
        return header;
    }
    
    private static String byteArrayString(byte[] b)
    {
        String byteString = "";
        
        for(int i = 0; i < b.length; i++)
        {
            byteString += String.format("%2s",Integer.toHexString(b[i] & 0xFF)).replace(' ','0');
        }
        
        return byteString;
    }
    
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    //Methods from encryption process used to find keys
    //THIS NEEDS TO BE CHANGED...
    void generateKeys(int key)
    {
        
        int bitMaskAll = 0xFF;
        
        bits25 = (int) (key % Math.pow(2, 25));
        bits17 = key >> 25;
        
        bits25 = bits25 << 1 | 0x01;
        bits17 = bits17 << 1 | 0x01;
        
        System.out.println("25 bit key: " + bitString(bits25));
        System.out.println("17 bit key: " + bitString(bits17));
        
        System.out.println();
    }
    
    private int LFSR17()
    {
        //System.out.println("LFSR:");
        //System.out.println(bitString(bits17) + "\n");
        
        int bitMask17 = 0x01 << 16;
        int bitMask2 = 0x01 << 1;
        
        int keyStreamByte = 0;
        
        for(int i = 0; i < 8; i++)
        {
            int lfsr = (((bits17 & bitMask17) >> 16) ^ ((bits17 & bitMask2) >> 1) & 1);
            bits17 = bits17 << 1 | lfsr;
            if(bits17 > 131072)
            {
                bits17 = bits17 - 131072;
                keyStreamByte = keyStreamByte << 1 | 0x01;
            }
            else
            {
                keyStreamByte = keyStreamByte << 1 | 0x00;
            }
            //System.out.println(bitString(bits17));
        }
        
        //System.out.println("\n"+bitString(keyStreamByte)+"\n");
        
        return keyStreamByte;
    }
    
    private int LFSR25()
    {
        //System.out.println("LFSR:");
        //System.out.println(bitString(bits25) + "\n");
        
        int bitMask25 = 0x01 << 24;
        int bitMask21 = 0x01 << 20;
        int bitMask20 = 0x01 << 19;
        int bitMask10 = 0x01 << 9;
        
        int keyStreamByte = 0;
        
        for(int i = 0; i < 8; i++)
        {
            int lfsr = (((bits25 & bitMask25) >> 24) ^ ((bits25 & bitMask21) >> 20) ^ ((bits25 & bitMask20) >> 19) ^ ((bits25 & bitMask10) >> 9) & 1);
            bits25 = bits25 << 1 | lfsr;
            if(bits25 > 33554432)
            {
                bits25 = bits25 - 33554432;
                keyStreamByte = keyStreamByte << 1 | 0x01;
            }
            else
            {
                keyStreamByte = keyStreamByte << 1 | 0x00;
            }
            //System.out.println(bitString(bits25));
        }
        
        //System.out.println("\n"+bitString(keyStreamByte)+"\n");
        
        return keyStreamByte;
    }
    
    private int bit8adder(int key17, int key25)
    {
        //System.out.println("bit8adder: ");
        int sum = key17 + key25 + carry;
        //System.out.println(key17 + " + " + key25 + " = " + sum);
        if(sum > 256)
        {
            carry = 1;
            return sum % 256;
        }
        else
        {
            return sum;
        }
    }
    
    static String bitString(int bits)
    {
        if(Integer.toBinaryString(bits).length() < 10)
        {
            return String.format("%8s",Integer.toBinaryString(bits)).replace(' ','0');
        }
        if(Integer.toBinaryString(bits).length() > 17)
        {
            return String.format("%25s",Integer.toBinaryString(bits)).replace(' ','0');
        }
        else
        {
            return String.format("%17s",Integer.toBinaryString(bits)).replace(' ','0');
        }
    }
}
