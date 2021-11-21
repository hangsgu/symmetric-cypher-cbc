package com.unisinos.teoria.informacao;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.util.RuntimeEOFException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyCipher {

    private static final byte[][] subKeys = new byte[4][];

    private static final int[] KEY_PERMUTATION = {
            14, 5, 18, 31,
            27, 10, 26, 15,
            4, 13, 20, 24,
            3, 23, 29, 8,
            11, 6, 22, 12,
            30, 25, 1, 7,
            17, 21, 28, 16,
            19, 9, 2, 32
    };

    private static final int[] SUBKEY_PERMUTATION = {
            15, 6, 19, 1,
            28, 11, 27, 16,
            5, 14, 21, 25,
            4, 24, 30, 9,
            12, 7, 23, 13,
            31, 26, 2, 8,
            18, 22, 29, 17,
            20, 10, 32, 3
    };

    public List<Integer> encrypt(String message, String key) throws IOException {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(message.getBytes());
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        List<Integer> bits = new ArrayList<>();
        while (true) {
            try {
                bits.add(bitInputStream.readBit() ? 1 : 0);

            } catch (RuntimeEOFException e) {
                break;
            }
        }
        bitInputStream.close();

        keySchedule(key.getBytes());

        for (int i = 0; i < subKeys.length; i++) {
            bits = substituteAndTranspose(bits, subKeys[i]);
        }
        return bits;
    }

    public String decrypt(List<Integer> messageBinary) throws IOException {
        for (int i = subKeys.length - 1; i >= 0; i--) {
            messageBinary = substituteAndTranspose(messageBinary, subKeys[i]);
        }

        String message = messageBinary.stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));

        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(message.split("(?<=\\G.{8})"))
                .forEach(s -> stringBuilder.append((char) Integer.parseInt(s, 2)));
        return stringBuilder.toString();
    }

    public void keySchedule(byte[] initialKey) throws IOException {
        if (initialKey.length != 4) {
            throw new RuntimeException();
        }

        ByteArrayInputStream byteArray = new ByteArrayInputStream(initialKey);
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        String[] bits = new String[32];
        for (int value : KEY_PERMUTATION) {
            bits[value - 1] = bitInputStream.readBit() ? "1" : "0";
        }

        bitInputStream.close();

        for (int i = 0; i < subKeys.length; i++) {
            String permutedKey = Arrays.toString(bits).replaceAll("[^a-zA-Z0-9]", "");

            int leftKey = Integer.parseInt(permutedKey.substring(0, 16), 2);
            int rightKey = Integer.parseInt(permutedKey.substring(16), 2);

            leftKey = Integer.rotateLeft(leftKey, 2);
            rightKey = Integer.rotateLeft(rightKey, 2);

            long mergedKeys = ((long) leftKey << 16) + rightKey;

            String subKey = Long.toBinaryString(mergedKeys);
            String[] subKeyBits = subKey.split("");

            for (int j = 0; j < bits.length; j++) {
                bits[SUBKEY_PERMUTATION[j] - 1] = subKeyBits[j];
            }

            StringBuilder subKeyByte = new StringBuilder();
            for (int j = 0; j < bits.length; j++) {
                if ((j + 1) % 8 == 0) {
                    subKeys[i] = subKeyByte.toString().getBytes();
                    subKeyByte = new StringBuilder();
                } else {
                    subKeyByte.append(bits[j]);
                }
            }
        }
    }

    public List<Integer> substituteAndTranspose(List<Integer> binaryMessage, byte[] key) throws IOException {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(key);
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        int[] bits = new int[binaryMessage.size()];
        int index = 0;
        while (true) {
            try {
                bits[index++] = bitInputStream.readBit() ? 1 : 0;
            } catch (RuntimeEOFException e) {
                break;
            }
        }
        bitInputStream.close();

        List<Integer> xorResult = new ArrayList<>();
        for (int i = 0; i < bits.length; i++) {
            xorResult.add(binaryMessage.get(i) ^ bits[i]);
        }
        return xorResult;
    }

}
