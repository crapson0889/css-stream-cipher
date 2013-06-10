/*
 * This file is used to encrypt and decrypt the files. With CSS the process to 
 * encrypt and decrypt is the same for both.
 */
package css;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 *
 * @author crapson
 */
public class Encrypt {
           
    int bits25=0;
    int bits17=0;
    int carry = 0;
    
    Encrypt(String sourceFile, String key, String destinationFile)
    {
        System.out.println(sourceFile);
        
        //Break up the key into a 17 bit key, and  a 25 bit key
        getKeys(key);
        
        //Read in the file to encrypt
        try{
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(sourceFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            OutputStream out = new FileOutputStream(destinationFile);
            
            //Current byte
            byte b;
            //Current index for encryption
            int keyIndex = 0;
            int count = 0;
            //Keys
            int finalKey = 0;
            int encryptionResult;
            
            //Read File character By character
            while (in.available() != 0)   {
                b = in.readByte();
                //System.out.print((byte)b+" ");
                finalKey = bit8adder(LFSR17(), LFSR25());
                encryptionResult = b ^ finalKey;
                //System.out.print((byte)encryptionResult+" ");
                System.out.println(bitString(encryptionResult) + " " + encryptionResult);
                
                out.write((int)encryptionResult);
                
                count++;
            }
            System.out.println("\n"+count);
            //Close the input stream
            in.close();
            out.close();
        }
        catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e);
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    void getKeys(String key)
    {
        for(int i = 0; i < key.length(); i ++)
        {
            System.out.println(key.charAt(i)+" "+Integer.toBinaryString((byte)key.charAt(i)));
        }
        
        int bitMaskAll = 0xFF;
        
        for(int i = 0; i < key.length(); i ++)
        {
            if(i < 3)
            {
                bits25 = bits25<< 8 | (key.charAt(i) & bitMaskAll);
            }
            if(i >= 3)
            {
                bits17 = bits17 << 8 | (key.charAt(i) & bitMaskAll);
            }
        }
        
        bits25 = bits25 << 1 | 0x01;
        bits17 = bits17 << 1 | 0x01;
        
        System.out.println(bitString(bits25));
        System.out.println(bitString(bits17));
        
        System.out.println();
    }
    
    /*
     * This is old code for making sure my LFSR function worked...
     * */
    int LFSR(int bits)
    {
        System.out.println("LFSR:");
        System.out.println(bitString(bits));
        
        int bitMask4 = 0x08;
        int bitMask3 = 0x04;
        
        for(int i = 0; i < 8; i++)
        {
            int lfsr = (((bits & bitMask4) >> 3) ^ ((bits & bitMask3) >> 2) & 1);
            bits = bits << 1 | lfsr;
            if(bits > 16)
            {
                bits = bits - 16;
            }
            System.out.println(bitString(bits));
        }
        
        return 0;
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
