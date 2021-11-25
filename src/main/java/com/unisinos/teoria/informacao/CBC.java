package com.unisinos.teoria.informacao;

import htsjdk.samtools.cram.io.BitInputStream;
import htsjdk.samtools.cram.io.DefaultBitInputStream;
import htsjdk.samtools.util.RuntimeEOFException;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class CBC {

    int[] firstVector = new int[48];

    public CBC(String key) {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(key.getBytes());
        BitInputStream keyBits = new DefaultBitInputStream(byteArray);

        List<Integer> bits = new ArrayList<>();
        while (true) {
            try {
                bits.add(keyBits.readBit() ? 1 : 0);
            } catch (RuntimeEOFException e) {
                break;
            }
        }

        for (int i = 0; i < bits.size(); i++) {
            firstVector[i] = bits.get(i);
        }

        for (int i = 0; i < 16; i++) {
            firstVector[i+32] = bits.get(i);
        }
    }

    public int[] operate(int[] encryptedMessage, int[] vectorToXor) {

        if (vectorToXor == null) {
            vectorToXor = firstVector;
        }

        int[] xorResult = new int[encryptedMessage.length];

        for (int i = 0; i < xorResult.length; i++) {
            xorResult[i] = encryptedMessage[i] ^ vectorToXor[i];
        }
        return xorResult;
    }
}
