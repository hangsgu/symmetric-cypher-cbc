package com.unisinos.teoria.informacao;


import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.BitOutputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.cram.io.DefaultBitOutputStream;
import htsjdk.samtools.util.RuntimeEOFException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {

        String file = args[0];
        String key = args[1];

        if (key.length() != 4) {
            throw new IllegalArgumentException("Key is not 4 chars long");
        }

        byte[] data = Files.readAllBytes(Paths.get(file));

//        if ("ENCODE".equals(encodeOrDecode)) {
//            Codification codification = codificationFactory.create(args[3], args.length >= 5 ? args[4] : "");
//            byte[] result = codification.compress(new String(file).chars().toArray());
//            FileUtils.writeByteArrayToFile(new File(destinationFile), result);
//        } else {
//            CodificationHeader codificationHeader = CodificationHeader.fromValue(bits.readBits(Codification.BYTE_SIZE));
//            Codification codification = codificationFactory.create(codificationHeader.toString());
//            int[] result = codification.decompress(file);
//            String buffer = new String();
//            for (int i = 0; i < result.length; i++) {
//                buffer += (char) result[i];
//            }
//            FileUtils.write(new File(destinationFile), buffer, StandardCharsets.UTF_8);
//        }


        byte[] result = encrypt(data, key);
        FileUtils.writeByteArrayToFile(new File("encrypted"), result);

        byte[] decrypt = decrypt("encrypted");
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
        FileUtils.write(new File("decrypted"), stringBuilder.toString(), StandardCharsets.UTF_8);
    }

    private static byte[] encrypt(byte[] message, String key) throws IOException {
        CustomSymmetricCypher customSymmetricCypher = new CustomSymmetricCypher();
        CBC cbc = new CBC();

        ByteArrayInputStream byteArray = new ByteArrayInputStream(message);
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
        BitOutputStream bitOutputStream = new DefaultBitOutputStream(bytesOutput);

        int paddingCount = Math.abs(((message.length * 8) % 48) - 48);
        if (paddingCount == 48) {
            paddingCount = 0;
        }
        String paddingAsText = StringUtils.leftPad(String.valueOf(paddingCount), 2, '0');

        ByteArrayInputStream paddingBytesOutput = new ByteArrayInputStream(paddingAsText.getBytes());
        BitInputStream paddingBitOutputStream = new DefaultBitInputStream(paddingBytesOutput);

        while (true) {
            try {
                bitOutputStream.write(paddingBitOutputStream.readBit());
            } catch (RuntimeEOFException e) {
                break;
            }
        }

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
                encryptResult = customSymmetricCypher.encrypt(messageBits, key);

                for (int j = 0; j < encryptResult.length; j++) {
                    bitOutputStream.write(encryptResult[j] == 1);
                }
            }
        }
        bitOutputStream.close();
        bitInputStream.close();
        return bytesOutput.toByteArray();
    }

    private static byte[] decrypt(String file) throws IOException {
        CustomSymmetricCypher customSymmetricCypher = new CustomSymmetricCypher();
        CBC cbc = new CBC();

        ByteArrayInputStream byteArray = new ByteArrayInputStream(Files.readAllBytes(Paths.get(file)));
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
        BitOutputStream bitOutputStream = new DefaultBitOutputStream(bytesOutput);

        String paddingAsText = "";
        for (int j = 0; j < 16; j++) {
            paddingAsText += bitInputStream.readBit() ? "1" : "0";
        }

        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(paddingAsText.split("(?<=\\G.{8})"))
                .forEach(s -> stringBuilder.append((char) Integer.parseInt(s, 2)));

        int paddingAmount = Integer.valueOf(stringBuilder.toString());
        int[] messageBits = new int[48];
        List<Integer> decryptedResult = new ArrayList<>();
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

            int[] result = customSymmetricCypher.decrypt(messageBits);
            for (int j = 0; j < result.length; j++) {
                decryptedResult.add(result[j]);
            }
//                cbcBlock = cbc.operate(decryptedResult, cbcBlock);
        }

        for (int j = 0; j < decryptedResult.size() - paddingAmount; j++) {
            bitOutputStream.write(decryptedResult.get(j) == 1);
        }

        bitOutputStream.close();
        bitInputStream.close();
        return bytesOutput.toByteArray();
    }
}
