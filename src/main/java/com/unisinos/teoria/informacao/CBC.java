package com.unisinos.teoria.informacao;

public class CBC {

    int[] firstVector = {1, 1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1};

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
