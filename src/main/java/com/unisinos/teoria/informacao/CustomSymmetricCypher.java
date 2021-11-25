package com.unisinos.teoria.informacao;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class CustomSymmetricCypher {

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

    public int[] encrypt(int[] messageBits, String key) throws IOException {
        keySchedule(key.getBytes());

        for (byte[] subKey : subKeys) {
            messageBits = substituteAndTranspose(messageBits, subKey);
        }
        return messageBits;
    }

    public int[] decrypt(int[] messageBits) throws IOException {
        for (int i = subKeys.length - 1; i >= 0; i--) {
            messageBits = substituteAndTranspose(messageBits, subKeys[i]);
        }
        return messageBits;
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

            for (int j = 0; j < subKeyBits.length && j < bits.length; j++) {
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

    public int[] substituteAndTranspose(int[] messageBits, byte[] key) throws IOException {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(key);
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        int[] keyBits = new int[messageBits.length];
        for (int i = 0; i < 32; i++) {
            keyBits[i] = bitInputStream.readBit() ? 1 : 0;
        }
        bitInputStream.close();

        int[] xorResult = new int[messageBits.length];
        for (int i = 0; i < xorResult.length; i++) {
            xorResult[i] = messageBits[i] ^ keyBits[i];
        }
        return xorResult;
    }

}
