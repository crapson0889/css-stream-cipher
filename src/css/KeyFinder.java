/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package css;

import static css.Encrypt.bitString;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 *
 * @author crapson
 */
public class KeyFinder {
    
    int bits24=0;
    int bits16=0;
    int bits25=0;
    int bits17=0;
    int lfsrBits25;
    int lfsrBits17;
    int carry = 0;
    
    NotificationFrame nf = new NotificationFrame();
    
    KeyFinder(String sourceFile, boolean englishKey)
    {
        System.out.println("File: " + sourceFile);
        
        String extension = sourceFile.substring(sourceFile.lastIndexOf(".") + 1);
        
        System.out.println("File extension: " + extension);
        
        byte[] header = getHeader(extension);
        
        System.out.println("File header to search for: " + byteArrayString(header));
        
        getKey(sourceFile, header, englishKey);
    }
    
    private byte[] getKey(String sourceFile, byte[] header, boolean englishKey)
    {
        long startTime = System.nanoTime();
        byte[] key = null;
        
        if(header.length != 0)
        {
            try{
                // Open the file that is the first 
                // command line parameter
                FileInputStream fstream = new FileInputStream(sourceFile);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                byte[] decryptedHeader = new byte[header.length];
                byte[] encryptedHeader = new byte[header.length];
                byte b;
                int finalKey = 0;

                generateKeys();         
                //Testing code
                if(englishKey == false)
                {
                    bits24 = 0;
                    bits16 = 0;
                }
                else
                {
                    bits24 = Integer.parseInt("010000010100000101000001", 2); //AAA
                    bits16 = Integer.parseInt("0100000101000001", 2); //AA
                }
                //bits24 = bits24 - 10;
                bits25 = bits24 << 1 | 0x01;
                bits17 = bits16 << 1 | 0x01;
                System.out.println(bitString(bits17));
                System.out.println(">>"+charStringFromBits(bits24)+charStringFromBits(bits16));

                for(int i =0; i < header.length; i++)
                {
                    if(in.available() != 0)
                    {
                        encryptedHeader[i] = in.readByte();
                    }
                }

                boolean found = false;
                do
                {
                    carry = 0;
                    lfsrBits25 = bits25;
                    lfsrBits17 = bits17;
                    //Read File character By character
                    for (int i = 0; i < header.length; i++)   {
                        finalKey = bit8adder(LFSR17(), LFSR25());
                        decryptedHeader[i] = (byte) (encryptedHeader[i] ^ finalKey);

                        if(decryptedHeader[i] != header[i])
                        {
                            //System.out.println("Header check: " + decryptedHeader[i] + " " + header[i]);
                            break;
                        }
                        else if(i == header.length - 1)
                        {
                            found = true;
                        }
                    }
                    if(found == true)
                    {
                        break;
                    }
                    if(englishKey == true)
                    {
                        if(bits24 == 8026746 && bits16 == 31354)
                        {
                            bits25 = 33554432;
                        }
                        else
                        {
                            incrementEnglishKeys();
                        }
                    }
                    else
                    {
                        incrementKeys();  
                    }
                } while (bits25 < 33554431);
                long endTime = System.nanoTime();
                if(found)
                {
                    String notification = "Completed\nFound key for encrypted file: " + sourceFile + "\n";
                    notification = notification + "Key: " + charStringFromBits(bits24)+charStringFromBits(bits16) + "\n";
                    notification = notification + "bits16: " + bitString(bits16) + "\n";
                    notification = notification + "bits24: " + bitString(bits24) + "\n";     
                    notification = notification + "Execution time: " + ((endTime - startTime)/ 1000000000.0) + " seconds\n";
                    System.out.println("\n" + notification);
                    nf.setNotification(notification);
                    nf.setTitle("Notification");
                }
                else
                {
                    String notification = "Key not found";
                    nf.setNotification(notification);
                    nf.setTitle("Notification");
                }
                //System.out.println(byteArrayString(decryptedHeader));

            }
            catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e);
                System.err.println("Error: " + e.getMessage());
            }
        }
        else
        {
           String notification = "File type not supported";
           nf.setNotification(notification);
           nf.setTitle("Notification"); 
        }
        
        return key;
    }
    
    private byte[] getHeader(String extension)
    {
        byte[] header = null;
        
        if(extension.equalsIgnoreCase("txt"))
        {
            header = hexStringToByteArray("");
        }
        else if(extension.equalsIgnoreCase("png"))
        {
            header = hexStringToByteArray("89504E470D0A1A0A");
        }
        else if(extension.equalsIgnoreCase("gif"))
        {
            header = hexStringToByteArray("474946383761474946383961");
        }
        else if(extension.equalsIgnoreCase("tif") || extension.equalsIgnoreCase("tiff"))
        {
            header = hexStringToByteArray("49492A004D4D002A");
        }
        else if(extension.equalsIgnoreCase("pdf"))
        {
            header = hexStringToByteArray("25504446");
        }
        else if(extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg"))
        {
            header = hexStringToByteArray("FFD8FFE0"); //Header is longer, but it has variable values. Not sure how to handle values that change
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
    void generateKeys()
    {
        
        int bitMaskAll = 0xFF;
        
        bits24 = 0;
        bits16 = 0;
        bits25 = bits24 << 1 | 0x01;
        bits17 = bits16 << 1 | 0x01;
        
        //bits25 = bits25 << 1 | 0x01;
        //bits17 = bits17 << 1 | 0x01;
        
        System.out.println("25 bit key: " + bitString(bits25));
        System.out.println("17 bit key: " + bitString(bits17));
        
        System.out.println();
    }
    
    void incrementKeys()
    {
        bits16 = bits16 + 1;
        if(bits16 > 65535)
        {
            bits16 = 0;
            bits24 = bits24 + 1;
            //System.out.println(bitString(bits24) + " - " + bitString(bits16));
        }
        bits17 = bits16 << 1 | 0x01;
        bits25 = bits24 << 1 | 0x01;
        //System.out.println(charStringFromBits(bits24)+charStringFromBits(bits16));
        //System.out.println(bitString(bits24) + " - " + bitString(bits16));
        //System.out.println(bitString(bits25) + " + " + bitString(bits17));
    }
    
    void incrementEnglishKeys()
    {
        bits16 = bits16 + 1;
        if(bits16 == 31355) //23131 = 1 more than ZZ
        {
            bits16 = 16705; //AA
            bits24 = bits24 + 1;
            
            if(bits24 % 65536 == 23163)
            {
                bits24 = bits24 + 1734;
            }
            else if(bits24 % 65536 == 31355)
            {
                bits24 = bits24 + (65534 - (57 << 8) - 56);
                
                if((bits24 - bits24%64434 - bits24%256) >> 16 == 90)
                {
                    bits24 = bits24 + 65534 * 6 + 12;
                }
            }//4291194
            else if(bits24 % 256 == 91)
            {
                bits24 = bits24 + 6;
            }
            else if(bits24 % 256 == 123)
            {
                bits24 = bits24 + 198;
            }
        }
        else if(bits16 == 23163)
        {
            bits16 = 24897;
        }
        else if(bits16 % 256 == 91)
        {
            bits16 = bits16 + 6;
        }
        else if(bits16 % 256 == 123)
        {
            bits16 = bits16 + 198;
        }
        
        bits17 = bits16 << 1 | 0x01;
        bits25 = bits24 << 1 | 0x01;
        //System.out.println(charStringFromBits(bits24)+charStringFromBits(bits16) + " " + bits16);
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
        
        //System.out.println(bitString(keyStreamByte));
        
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
        if(sum > 255)
        {
            carry = 1;
            return sum % 256;
        }
        else
        {
            carry = 0;
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
    
    static String charStringFromBits(int bits)
    {
        String charString = "";
        //System.out.println(bitString(bits));
        while(bits > 0)
        {
            //System.out.print(bits % 256);
            //System.out.println(" - " + (char)(bits % 256));
            charString = charString + (char)(bits % 256);
            bits = bits >> 8;
        }
        
        return new StringBuilder(charString).reverse().toString();
    }
}
