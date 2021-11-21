package com.unisinos.teoria.informacao;


import java.util.List;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        CustomSymmetricCypher customSymmetricCypher = new CustomSymmetricCypher();

        List<Integer> encryptResult = customSymmetricCypher.encrypt("message dijasiuhdiausdasd", "jkxt");
        String result = customSymmetricCypher.decrypt(encryptResult);
    }
}
