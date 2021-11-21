package com.unisinos.teoria.informacao;


/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        MyCipher myCipher = new MyCipher();

        int[] encryptResult = myCipher.encrypt("message", "jkxt");
        String result = myCipher.decrypt(encryptResult);
    }
}
