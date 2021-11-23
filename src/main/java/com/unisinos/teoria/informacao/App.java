package com.unisinos.teoria.informacao;


import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.BitOutputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.util.RuntimeEOFException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        byte[] result = encrypt();
        byte[] decrypt = decrypt(result);

        ByteArrayInputStream byteArray = new ByteArrayInputStream(decrypt);
        BitInputStream bits = new DefaultBitInputStream(byteArray);

        String message = "";
        while (true) {
            try {
                message += bits.readBit() ? "1" : "0";
            } catch (RuntimeEOFException e) {
                break;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(message.split("(?<=\\G.{8})"))
                .forEach(s -> stringBuilder.append((char) Integer.parseInt(s, 2)));
        System.out.println(stringBuilder.toString());
    }

    private static byte[] encrypt() throws IOException {
        CustomSymmetricCypher customSymmetricCypher = new CustomSymmetricCypher();
        CBC cbc = new CBC();

        ByteArrayInputStream byteArray = new ByteArrayInputStream("messag".getBytes());
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
        BitOutputStream bitOutputStream = new DefaultBitOutputStream(bytesOutput);

        int[] messageBits = new int[48];
        int[] encryptResult;
        int i = 0;
        while (true) {
            try {
                for (i = 0; i < 48; i++) {
                    messageBits[i] = bitInputStream.readBit() ? 1 : 0;
                }
            } catch (RuntimeEOFException e) {
                break;
            } finally {
                if (i == 0) {
                    break;
                } else {
                    for (int j = i; j < 48; j++) {
                        messageBits[j] = 0;
                    }
                }
//                int[] cbcResult = cbc.operate(messageBits, encryptResult);
                encryptResult = customSymmetricCypher.encrypt(messageBits, "jkxt");

                for (int j = 0; j < encryptResult.length; j++) {
                    bitOutputStream.write(encryptResult[j] == 1);
                }
            }
        }
        bitOutputStream.close();
        bitInputStream.close();
        return bytesOutput.toByteArray();
    }

    private static byte[] decrypt(byte[] encryptedMessage) throws IOException {
        CustomSymmetricCypher customSymmetricCypher = new CustomSymmetricCypher();
        CBC cbc = new CBC();

        ByteArrayInputStream byteArray = new ByteArrayInputStream(encryptedMessage);
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
        BitOutputStream bitOutputStream = new DefaultBitOutputStream(bytesOutput);

        int[] messageBits = new int[48];
        int[] decryptedResult;
        int[] cbcBlock = null;
        int i;
        while (true) {
            try {
                for (i = 0; i < 48; i++) {
                    messageBits[i] = bitInputStream.readBit() ? 1 : 0;
                }
            } catch (RuntimeEOFException e) {
                break;
            }
                decryptedResult = customSymmetricCypher.decrypt(messageBits);
//                cbcBlock = cbc.operate(decryptedResult, cbcBlock);

            for (int j = 0; j < decryptedResult.length; j++) {
                bitOutputStream.write(decryptedResult[j] == 1);
            }
        }
        bitOutputStream.close();
        bitInputStream.close();
        return bytesOutput.toByteArray();
    }
}
