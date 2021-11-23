package com.unisinos.teoria.informacao;


import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;

import java.io.ByteArrayInputStream;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        CustomSymmetricCypher customSymmetricCypher = new CustomSymmetricCypher();

        ByteArrayInputStream byteArray = new ByteArrayInputStream("message".getBytes());
        BitInputStream bits = new DefaultBitInputStream(byteArray);

        int[] messageBits = new int[48];
        for (int i = 0; i < 48; i++) {
            messageBits[i] = bits.readBit() ? 1 : 0;
        }
        bits.close();

        int[] encryptResult = customSymmetricCypher.encrypt(messageBits, "jkxt");
        String result = customSymmetricCypher.decrypt(encryptResult);
        System.out.println(result);
    }
}
