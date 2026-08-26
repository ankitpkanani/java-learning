package com.learning.jdk25;

import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HexFormat;

/**
 * JDK 25 (JEP 510): the Key Derivation Function API -- javax.crypto.KDF --
 * standardizes deriving one or more secret keys from existing keying material
 * (e.g. a shared secret from a key exchange), instead of every library
 * hand-rolling its own HKDF implementation on top of low-level MAC/hash
 * primitives.
 */
public class KeyDerivationFunctionDemo {

    public static void main(String[] args) throws Exception {
        extractThenExpand();
        sameInputsProduceSameKey();
        differentInfoProducesIndependentKeys();
        deriveRawBytesInsteadOfASecretKey();
    }

    private static void extractThenExpand() throws Exception {
        byte[] inputKeyingMaterial = "shared-secret-from-a-key-exchange".getBytes();
        byte[] salt = "per-session-salt".getBytes();
        byte[] contextInfo = "encryption-key-v1".getBytes();

        // HKDF (RFC 5869) works in two stages: "extract" condenses the (possibly non-uniform) input
        // keying material + salt into a fixed-length pseudorandom key, then "expand" stretches that
        // into as much output keying material as requested, bound to the given "info" context string.
        KDF hkdf = KDF.getInstance("HKDF-SHA256");
        AlgorithmParameterSpec spec = HKDFParameterSpec.ofExtract()
                .addIKM(inputKeyingMaterial)
                .addSalt(salt)
                .thenExpand(contextInfo, 32); // 32 bytes = 256 bits, matching AES-256

        SecretKey derivedKey = hkdf.deriveKey("AES", spec);

        System.out.println("KDF.getInstance(\"HKDF-SHA256\"): " + hkdf.getAlgorithm());
        System.out.println("derived key algorithm: " + derivedKey.getAlgorithm());
        System.out.println("derived key length (bytes): " + derivedKey.getEncoded().length);
        System.out.println("derived key (hex): " + HexFormat.of().formatHex(derivedKey.getEncoded()));
    }

    private static void sameInputsProduceSameKey() throws Exception {
        byte[] ikm = "identical-secret".getBytes();
        byte[] salt = "identical-salt".getBytes();
        byte[] info = "identical-info".getBytes();

        SecretKey first = deriveAesKey(ikm, salt, info);
        SecretKey second = deriveAesKey(ikm, salt, info);

        // HKDF is deterministic: the same (IKM, salt, info) always derives the exact same key bytes --
        // there's no randomness involved, unlike generating a fresh key with a SecureRandom.
        System.out.println("same inputs -> identical derived key bytes: "
                + java.util.Arrays.equals(first.getEncoded(), second.getEncoded()));
    }

    private static void differentInfoProducesIndependentKeys() throws Exception {
        byte[] sharedSecret = "one-shared-secret-many-purposes".getBytes();
        byte[] salt = "app-salt".getBytes();

        // The same shared secret can be safely fanned out into multiple, cryptographically
        // independent keys just by varying the "info" label -- e.g. one key for encryption, a
        // different one for message authentication, both derived from one underlying secret.
        SecretKey encryptionKey = deriveAesKey(sharedSecret, salt, "purpose:encryption".getBytes());
        SecretKey macKey = deriveAesKey(sharedSecret, salt, "purpose:mac".getBytes());

        System.out.println("encryption key and MAC key, from the same secret, differ: "
                + !java.util.Arrays.equals(encryptionKey.getEncoded(), macKey.getEncoded()));
    }

    private static void deriveRawBytesInsteadOfASecretKey() throws Exception {
        byte[] ikm = "raw-bytes-example".getBytes();
        byte[] salt = "salt".getBytes();
        byte[] info = "raw-output".getBytes();

        // deriveData(...) is the same derivation, but returns a plain byte[] instead of wrapping it
        // in a SecretKey -- useful when the derived material isn't going straight into a Cipher.
        KDF hkdf = KDF.getInstance("HKDF-SHA256");
        AlgorithmParameterSpec spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).thenExpand(info, 16);
        byte[] raw = hkdf.deriveData(spec);

        System.out.println("deriveData(...) raw byte[] length: " + raw.length);
        System.out.println("deriveData(...) (hex): " + HexFormat.of().formatHex(raw));
    }

    private static SecretKey deriveAesKey(byte[] ikm, byte[] salt, byte[] info) throws Exception {
        KDF hkdf = KDF.getInstance("HKDF-SHA256");
        AlgorithmParameterSpec spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).thenExpand(info, 32);
        return hkdf.deriveKey("AES", spec);
    }
}
