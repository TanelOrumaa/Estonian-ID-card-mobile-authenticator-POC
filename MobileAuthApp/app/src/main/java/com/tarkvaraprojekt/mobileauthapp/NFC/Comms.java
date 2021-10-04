package com.tarkvaraprojekt.mobileauthapp.NFC;

import android.nfc.tech.IsoDep;
import android.util.Log;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Comms {
    private static final byte[] master = { // select Main AID
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x0C, (byte) 0x10,
            (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x77,
            (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0x07, (byte) 0x00,
            (byte) 0x00, (byte) 0xFE, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x00,
    };

    private static final byte[] masterSec = { // select Main AID
            (byte) 0x0C, (byte) 0xA4, (byte) 0x04, (byte) 0x0C, (byte) 0x2D,
            (byte) 0x87, (byte) 0x21, (byte) 0x01,
    };

    private static final byte[] MSESetAT = { // manage security environment: set authentication template
            (byte) 0x00, (byte) 0x22, (byte) 0xC1, (byte) 0xA4, (byte) 0x0F,
            (byte) 0x80, (byte) 0x0A, (byte) 0x04, (byte) 0x00, (byte) 0x7F,
            (byte) 0x00, (byte) 0x07, (byte) 0x02, (byte) 0x02, (byte) 0x04,
            (byte) 0x02, (byte) 0x04, (byte) 0x83, (byte) 0x01, (byte) 0x02,
            (byte) 0x00,
    };

    private static final byte[] GAGetNonce = { // general authenticate: get nonce
            (byte) 0x10, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x02,
            (byte) 0x7C, (byte) 0x00, (byte) 0x00,
    };

    private static final byte[] GAMapNonceIncomplete = {
            (byte) 0x10, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x45,
            (byte) 0x7C, (byte) 0x43, (byte) 0x81, (byte) 0x41,
    };

    private static final byte[] GAKeyAgreementIncomplete = {
            (byte) 0x10, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x45,
            (byte) 0x7C, (byte) 0x43, (byte) 0x83, (byte) 0x41,
    };

    private static final byte[] dataForMACIncomplete = {
            (byte) 0x7F, (byte) 0x49, (byte) 0x4F, (byte) 0x06, (byte) 0x0A,
            (byte) 0x04, (byte) 0x00, (byte) 0x7F, (byte) 0x00, (byte) 0x07,
            (byte) 0x02, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x04,
            (byte) 0x86, (byte) 0x41,
    };

    private static final byte[] GAMutualAuthenticationIncomplete = {
            (byte) 0x00, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x0C,
            (byte) 0x7C, (byte) 0x0A, (byte) 0x85, (byte) 0x08,
    };

    private static final byte[] personal = { // select personal data DF
            (byte) 0x0C, (byte) 0xA4, (byte) 0x01, (byte) 0x0C, (byte) 0x1D,
            (byte) 0x87, (byte) 0x11, (byte) 0x01,
    };

    private static final byte[] read = { // read binary
            (byte) 0x0C, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0x0D,
            (byte) 0x97, (byte) 0x01, (byte) 0x00,
    };

    private byte ssc; // Send sequence counter. Ok as long as the number of sent and received APDUs is <128.

    /**
     * Calculates the message authentication code
     * @param keyMAC the cipher key
     * @param APDU the byte array on which the CMAC algorithm is performed
     * @return MAC
     */
    private byte[] getMAC(byte[] keyMAC, byte[] APDU) {
        BlockCipher blockCipher = new AESEngine();
        CMac cmac = new CMac(blockCipher);
        cmac.init(new KeyParameter(keyMAC));
        cmac.update(APDU, 0, APDU.length);
        byte[] MAC = new byte[cmac.getMacSize()];
        cmac.doFinal(MAC, 0);
        return Arrays.copyOf(MAC, 8);
    }

    /**
     * Creates an application protocol data unit
     * @param template the byte array to be used as a template
     * @param data the data necessary for completing the APDU
     * @param extra the missing length of the APDU being created
     * @return the complete APDU
     */
    private byte[] createAPDU(byte[] template, byte[] data, int extra) {
        byte[] APDU = Arrays.copyOf(template, template.length + extra);
        System.arraycopy(data, 0, APDU, template.length, data.length);
        return APDU;
    }

    /**
     * Creates a cipher key
     * @param unpadded the array to be used as the basis for the key
     * @param last the last byte in the appended padding
     * @return the constructed key
     */
    private byte[] createKey(byte[] unpadded, byte last) throws NoSuchAlgorithmException {
        byte[] padded = Arrays.copyOf(unpadded, unpadded.length + 4);
        padded[padded.length - 1] = last;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return messageDigest.digest(padded);
    }

    /**
     * Decrypts the nonce
     * @param encryptedNonce the encrypted nonce received from the chip
     * @param CAN the card access number provided by the user
     * @return the decrypted nonce
     */
    private byte[] decryptNonce(byte[] encryptedNonce, String CAN) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] decryptionKey = createKey(CAN.getBytes(StandardCharsets.UTF_8), (byte) 3);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptionKey, "AES"), new IvParameterSpec(new byte[16]));
        return cipher.doFinal(encryptedNonce);
    }

    /**
     * Attempts to use the PACE protocol to create a secure channel with an Estonian ID-card
     * @param idCard the IsoDep link to the card
     * @param CAN the card access number
     * @return session keys if authentication succeeds, otherwise null
     */
    public byte[][] PACE(IsoDep idCard, String CAN) throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] response;
        byte[] APDU;
        byte[] decryptedNonce;
        byte[] encodedSecret;
        byte[] keyEnc;
        byte[] keyMAC;
        byte[] MAC;
        BigInteger privateKey;
        ECPoint publicKey;
        ECPoint cardPublicKey;
        ECPoint sharedSecret;
        ECPoint mappedECBasePoint;
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");

        // select the ECC applet on the chip
        response = idCard.transceive(master);
        Log.i("Select applet", Hex.toHexString(response));

        // initiate PACE
        response = idCard.transceive(MSESetAT);
        Log.i("Authentication template", Hex.toHexString(response));

        // get nonce
        response = idCard.transceive(GAGetNonce);
        Log.i("Get nonce", Hex.toHexString(response));
        decryptedNonce = decryptNonce(Arrays.copyOfRange(response, 4, response.length - 2), CAN);

        // generate an EC keypair and exchange public keys with the chip
        privateKey = new BigInteger(255, new SecureRandom()).add(BigInteger.ONE); // should be in [1, spec.getN()-1], but this is good enough for this application
        publicKey = spec.getG().multiply(privateKey).normalize();
        APDU = createAPDU(GAMapNonceIncomplete, publicKey.getEncoded(false), 66);
        response = idCard.transceive(APDU);
        Log.i("Map nonce", Hex.toHexString(response));
        cardPublicKey = spec.getCurve().decodePoint(Arrays.copyOfRange(response, 4, 69));

        // calculate the new base point, use it to generate a new keypair, and exchange public keys
        sharedSecret = cardPublicKey.multiply(privateKey);
        mappedECBasePoint = spec.getG().multiply(new BigInteger(1, decryptedNonce)).add(sharedSecret).normalize();
        privateKey = new BigInteger(255, new SecureRandom()).add(BigInteger.ONE);
        publicKey = mappedECBasePoint.multiply(privateKey).normalize();
        APDU = createAPDU(GAKeyAgreementIncomplete, publicKey.getEncoded(false), 66);
        response = idCard.transceive(APDU);
        Log.i("Key agreement", Hex.toHexString(response));
        cardPublicKey = spec.getCurve().decodePoint(Arrays.copyOfRange(response, 4, 69));

        // generate the session keys and exchange MACs to verify them
        sharedSecret = cardPublicKey.multiply(privateKey).normalize();
        encodedSecret = sharedSecret.getAffineXCoord().getEncoded();
        keyEnc = createKey(encodedSecret, (byte) 1);
        keyMAC = createKey(encodedSecret, (byte) 2);
        APDU = createAPDU(dataForMACIncomplete, cardPublicKey.getEncoded(false), 65);
        MAC = getMAC(keyMAC, APDU);
        APDU = createAPDU(GAMutualAuthenticationIncomplete, MAC, 9);
        response = idCard.transceive(APDU);
        Log.i("Mutual authentication", Hex.toHexString(response));

        // if the chip-side verification fails, return null
        if (response.length == 2) return null;

        // otherwise verify chip's MAC and return session keys
        APDU = createAPDU(dataForMACIncomplete, publicKey.getEncoded(false), 65);
        MAC = getMAC(keyMAC, APDU);
        assert (Hex.toHexString(response, 4, 8).equals(Hex.toHexString(MAC)));
        return new byte[][]{keyEnc, keyMAC};

    }

    /**
     * Encrypts or decrypts the APDU data
     * @param data the array containing the data to be processed
     * @param keyEnc the cipher key
     * @param mode indicates whether to en- or decrypt the data
     * @return the result of encryption or decryption
     */
    private byte[] encryptDecryptData(byte[] data, byte[] keyEnc, int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyEnc, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] iv = Arrays.copyOf(cipher.doFinal(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ssc}), 16);
        cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(mode, secretKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    /**
     * Constructs APDUs suitable for the secure channel.
     * @param data the data to be encrypted
     * @param keyEnc the encryption key
     * @param keyMAC the MAC key
     * @param incomplete the array to be used as a template
     * @return the constructed APDU
     */
    private byte[] createSecureAPDU(byte[] data, byte[] keyEnc, byte[] keyMAC, byte[] incomplete) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        ssc++;
        byte[] encryptedData = new byte[0];
        int length = 16 * (1 + data.length / 16);

        // construct the required array and calculate the MAC based on it
        byte[] macData = new byte[data.length > 0 ? 48 + length : 48];
        macData[15] = ssc; // first block contains the ssc
        System.arraycopy(incomplete, 0, macData, 16, 4); // second block has the command
        macData[20] = -128; // elements are terminated by 0x80 and zero-padded to the next block
        System.arraycopy(incomplete, 5, macData, 32, 3); // third block contains appropriately encapsulated data/Le
        if (data.length > 0) { // if the APDU has data, add padding and encrypt it
            byte[] paddedData = Arrays.copyOf(data, length);
            paddedData[data.length] = -128;
            encryptedData = encryptDecryptData(paddedData, keyEnc, Cipher.ENCRYPT_MODE);
            System.arraycopy(encryptedData, 0, macData, 35, encryptedData.length);
        }
        macData[35 + encryptedData.length] = -128;
        byte[] MAC = getMAC(keyMAC, macData);

        // construct the APDU using the encrypted data and the MAC
        byte[] APDU = new byte[incomplete.length + encryptedData.length + MAC.length + 3];
        System.arraycopy(incomplete, 0, APDU, 0, incomplete.length);
        if (encryptedData.length > 0) {
            System.arraycopy(encryptedData, 0, APDU, incomplete.length, encryptedData.length);
        }
        System.arraycopy(new byte[]{-114, 8}, 0, APDU, incomplete.length + encryptedData.length, 2); // MAC is encapsulated using the tag 0x8E
        System.arraycopy(MAC, 0, APDU, incomplete.length + encryptedData.length + 2, MAC.length);

        ssc++;
        return APDU;

    }

    /**
     * Gets contents of the personal data dedicated file
     * @param idCard link to the ID-card
     * @param keyEnc the encryption key
     * @param keyMAC the MAC key
     * @return an array containing personal data
     */
    public byte[][] readPersonalData(IsoDep idCard, byte[] keyEnc, byte[] keyMAC) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {

        byte[][] personalData = new byte[8][];
        byte[] data;
        byte[] APDU;
        byte[] response;

        // select the personal data dedicated file
        data = new byte[]{80, 0}; // personal data DF FID
        APDU = createSecureAPDU(data, keyEnc, keyMAC, personal);
        response = idCard.transceive(APDU);
        Log.i("Select personal data DF", Hex.toHexString(response));

        // select and read the first 8 elementary files in the DF
        for (byte i = 0; i < 8; i++) {

            data[1] = (byte) (i + 1);
            APDU = createSecureAPDU(data, keyEnc, keyMAC, personal);
            response = idCard.transceive(APDU);
            Log.i(String.format("Select EF 500%d", i + 1), Hex.toHexString(response));

            APDU = createSecureAPDU(new byte[0], keyEnc, keyMAC, read);
            response = idCard.transceive(APDU);
            Log.i(String.format("Read binary EF 500%d", i + 1), Hex.toHexString(response));

            // store the decrypted datum
            personalData[i] = encryptDecryptData(Arrays.copyOfRange(response, 3, 19), keyEnc, Cipher.DECRYPT_MODE);
        }

        return personalData;

    }
}
