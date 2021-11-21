package com.unisinos.teoria.informacao;


import java.util.List;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        MyCipher myCipher = new MyCipher();

        List<Integer> encryptResult = myCipher.encrypt("message dijasiuhdiausdasd", "jkxt");
        String result = myCipher.decrypt(encryptResult);
    }
}
