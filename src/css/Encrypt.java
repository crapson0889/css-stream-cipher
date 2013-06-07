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
public class Encrypt {
           
    int bits25=0;
    int bits17=0;
    
    Encrypt(String sourceFile, String key, String destinationFile)
    {
        System.out.println(sourceFile);
        
        //Break up the key
        int bitCount = 0;
        
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
        
        int i = LFSR17(bits17);
        
        //Read in the file to encrypt
        try{
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(sourceFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            //Current character
            byte b;
            //Current index for encryption
            int keyIndex = 0;
            int count = 0;
            
            //Read File character By character
            while (in.available() != 0)   {
                b = in.readByte();
                System.out.print(b + " ");
                count++;
            }
            System.out.println("\n\n"+count);
            //Close the input stream
            in.close();
        }
        catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e);
            System.err.println("Error: " + e.getMessage());
        }
    }
    
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
    
    int LFSR17(int bits)
    {
        System.out.println("LFSR:");
        System.out.println(bitString(bits) + "\n");
        
        int bitMask17 = 0x100;
        int bitMask2 = 0x02;
        
        int carryKey = 0;
        
        for(int i = 0; i < 8; i++)
        {
            int lfsr = (((bits & bitMask17) >> 16) ^ ((bits & bitMask2) >> 1) & 1);
            bits = bits << 1 | lfsr;
            if(bits > 131072)
            {
                bits = bits - 131072;
                carryKey = carryKey << 1 | 0x01;
            }
            else
            {
                carryKey = carryKey << 1 | 0x00;
            }
            System.out.println(bitString(bits));
            
            System.out.println(((bits & bitMask17) >> 16));
        }
        
        System.out.println("\n"+bitString(carryKey)+"\n");
        
        return 0;
    }
    
    String bitString(int bits)
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
