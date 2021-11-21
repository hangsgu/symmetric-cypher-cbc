package com.unisinos.teoria.informacao;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

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

    static int[] TRANSPOSE = {
            16, 7, 20, 21,
            29, 12, 28, 17,
            1, 15, 23, 26,
            5, 18, 31, 10,
            2, 8, 24, 14,
            32, 27, 3, 9,
            19, 13, 30, 6,
            22, 11, 4, 25
    };

    private static int[][] s1 = {
            {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
            {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
            {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
            {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}
    };

    private static int[][] s2 = {
            {15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10},
            {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5},
            {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15},
            {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}
    };

    private static int[][] s3 = {
            {10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8},
            {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1},
            {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7},
            {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}
    };

    private static int[][] s4 = {
            {7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15},
            {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9},
            {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4},
            {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}
    };

    private static int[][] s5 = {
            {2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9},
            {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6},
            {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14},
            {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}
    };

    private static int[][] s6 = {
            {12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11},
            {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8},
            {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6},
            {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}
    };

    private static int[][] s7 = {
            {4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1},
            {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6},
            {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2},
            {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}
    };

    private static int[][] s8 = {
            {13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7},
            {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2},
            {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8},
            {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}
    };

    private static int[][][] sBox = {s1, s2, s3, s4, s5, s6, s7, s8};

    public static void keySchedule(byte[] initialKey) throws IOException {
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

    public static String substituteAndTranspose(String binaryMessage, byte[] key) {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(key);
        BitInputStream bitInputStream = new DefaultBitInputStream(byteArray);

        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            bits.append(bitInputStream.readBit() ? "1" : "0");
        }

        long data = Long.parseLong(binaryMessage, 2);
        long k = Long.parseLong(bits.toString(), 2);

        long xorResult = data ^ k;

        String binaryString = Long.toBinaryString(xorResult);
        while (binaryString.length() < 48) {
            binaryString = "0" + binaryString;
        }

        String[] substringFromBinary = new String[8];
        for (int i = 0; i < 8; i++) {
            substringFromBinary[i] = binaryString.substring(0, 6);
            binaryString = binaryString.substring(6);
        }


        String[] sBoxResult = new String[8];
        for (int i = 0; i < 8; i++) {
            int[][] curS = sBox[i];
            String cur = substringFromBinary[i];

            int row = Integer.parseInt(cur.charAt(0) + "" + cur.charAt(5), 2);
            int col = Integer.parseInt(cur.substring(1, 5), 2);

            sBoxResult[i] = Integer.toBinaryString(curS[row][col]);

            while (sBoxResult[i].length() < 4)
                sBoxResult[i] = "0" + sBoxResult[i];

        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            result.append(sBoxResult[i]);
        }

        StringBuilder resultPermuted = new StringBuilder();
        for (int i = 0; i < TRANSPOSE.length; i++) {
            resultPermuted.append(result.charAt(TRANSPOSE[i] - 1));
        }

        return resultPermuted.toString();
    }

}
