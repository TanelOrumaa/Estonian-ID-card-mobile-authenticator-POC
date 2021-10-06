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
            0, -92, 4, 12, 16, -96, 0, 0, 0, 119, 1, 8, 0, 7, 0, 0, -2, 0, 0, 1, 0
    };

    private static final byte[] MSESetAT = { // manage security environment: set authentication template
            0, 34, -63, -92, 15, -128, 10, 4, 0, 127, 0, 7, 2, 2, 4, 2, 4, -125, 1, 2, 0
    };

    private static final byte[] GAGetNonce = { // general authenticate: get nonce
            16, -122, 0, 0, 2, 124, 0, 0
    };

    private static final byte[] GAMapNonceIncomplete = {
            16, -122, 0, 0, 69, 124, 67, -127, 65
    };

    private static final byte[] GAKeyAgreementIncomplete = {
            16, -122, 0, 0, 69, 124, 67, -125, 65
    };

    private static final byte[] GAMutualAuthenticationIncomplete = {
            0, -122, 0, 0, 12, 124, 10, -123, 8
    };

    private static final byte[] dataForMACIncomplete = {
            127, 73, 79, 6, 10, 4, 0, 127, 0, 7, 2, 2, 4, 2, 4, -122, 65
    };

    private static final byte[] masterSec = {
            12, -92, 4, 12, 45, -121, 33, 1
    };

    private static final byte[] personal = { // select personal data DF
            12, -92, 1, 12, 29, -121, 17, 1
    };

    private static final byte[] read = { // read binary
            12, -80, 0, 0, 13, -105, 1, 0
    };

    private IsoDep idCard;
    private final byte[] keyEnc;
    private final byte[] keyMAC;
    private byte ssc; // Send sequence counter.

    /**
     * The constructor performs PACE and stores the session keys
     *
     * @param idCard link to the card
     * @param CAN the card authentication number
     */
    public Comms(IsoDep idCard, String CAN) throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        idCard.connect();

        this.idCard = idCard;

        long start = System.currentTimeMillis();
        byte[][] keys = PACE(CAN);
        Log.i("Pace duration", String.valueOf(System.currentTimeMillis() - start));

        keyEnc = keys[0];
        keyMAC = keys[1];
    }

    /**
     * Calculates the message authentication code
     *
     * @param APDU   the byte array on which the CMAC algorithm is performed
     * @param keyMAC the key for performing CMAC
     * @return MAC
     */
    private byte[] getMAC(byte[] APDU, byte[] keyMAC) {
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
     *
     * @param template the byte array to be used as a template
     * @param data     the data necessary for completing the APDU
     * @param extra    the missing length of the APDU being created
     * @return the complete APDU
     */
    private byte[] createAPDU(byte[] template, byte[] data, int extra) {
        byte[] APDU = Arrays.copyOf(template, template.length + extra);
        System.arraycopy(data, 0, APDU, template.length, data.length);
        return APDU;
    }

    /**
     * Creates a cipher key
     *
     * @param unpadded the array to be used as the basis for the key
     * @param last     the last byte in the appended padding
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
     *
     * @param encryptedNonce the encrypted nonce received from the chip
     * @param CAN            the card access number provided by the user
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
     *
     * @param CAN    the card access number
     */
    private byte[][] PACE(String CAN) throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        // select the ECC applet on the chip
        byte[] response = idCard.transceive(master);
        Log.i("Select applet", Hex.toHexString(response));

        // initiate PACE
        response = idCard.transceive(MSESetAT);
        Log.i("Authentication template", Hex.toHexString(response));

        // get nonce
        response = idCard.transceive(GAGetNonce);
        Log.i("Get nonce", Hex.toHexString(response));
        byte[] decryptedNonce = decryptNonce(Arrays.copyOfRange(response, 4, response.length - 2), CAN);

        // generate an EC keypair and exchange public keys with the chip
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
        BigInteger privateKey = new BigInteger(255, new SecureRandom()).add(BigInteger.ONE); // should be in [1, spec.getN()-1], but this is good enough for this application
        ECPoint publicKey = spec.getG().multiply(privateKey).normalize();
        byte[] APDU = createAPDU(GAMapNonceIncomplete, publicKey.getEncoded(false), 66);
        response = idCard.transceive(APDU);
        Log.i("Map nonce", Hex.toHexString(response));
        ECPoint cardPublicKey = spec.getCurve().decodePoint(Arrays.copyOfRange(response, 4, 69));

        // calculate the new base point, use it to generate a new keypair, and exchange public keys
        ECPoint sharedSecret = cardPublicKey.multiply(privateKey);
        ECPoint mappedECBasePoint = spec.getG().multiply(new BigInteger(1, decryptedNonce)).add(sharedSecret).normalize();
        privateKey = new BigInteger(255, new SecureRandom()).add(BigInteger.ONE);
        publicKey = mappedECBasePoint.multiply(privateKey).normalize();
        APDU = createAPDU(GAKeyAgreementIncomplete, publicKey.getEncoded(false), 66);
        response = idCard.transceive(APDU);
        Log.i("Key agreement", Hex.toHexString(response));
        cardPublicKey = spec.getCurve().decodePoint(Arrays.copyOfRange(response, 4, 69));

        // generate the session keys and exchange MACs to verify them
        sharedSecret = cardPublicKey.multiply(privateKey).normalize();
        byte[] encodedSecret = sharedSecret.getAffineXCoord().getEncoded();
        byte[] keyEnc = createKey(encodedSecret, (byte) 1);
        byte[] keyMAC = createKey(encodedSecret, (byte) 2);
        APDU = createAPDU(dataForMACIncomplete, cardPublicKey.getEncoded(false), 65);
        byte[] MAC = getMAC(APDU, keyMAC);
        APDU = createAPDU(GAMutualAuthenticationIncomplete, MAC, 9);
        response = idCard.transceive(APDU);
        Log.i("Mutual authentication", Hex.toHexString(response));

        // if the chip-side verification fails, crash and burn
        if (response.length == 2) throw new RuntimeException("Invalid CAN.");

        // otherwise verify chip's MAC and return session keys
        APDU = createAPDU(dataForMACIncomplete, publicKey.getEncoded(false), 65);
        MAC = getMAC(APDU, keyMAC);
        if (!Hex.toHexString(response, 4, 8).equals(Hex.toHexString(MAC))) {
            throw new RuntimeException("Could not verify chip's MAC."); // Should never happen.
        }
        return new byte[][]{keyEnc, keyMAC};

    }

    /**
     * Encrypts or decrypts the APDU data
     *
     * @param data   the array containing the data to be processed
     * @param mode   indicates whether to en- or decrypt the data
     * @return the result of encryption or decryption
     */
    private byte[] encryptDecryptData(byte[] data, int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
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
     *
     * @param data       the data to be encrypted
     * @param incomplete the array to be used as a template
     * @return the constructed APDU
     */
    private byte[] createSecureAPDU(byte[] data, byte[] incomplete) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

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
            encryptedData = encryptDecryptData(paddedData, Cipher.ENCRYPT_MODE);
            System.arraycopy(encryptedData, 0, macData, 35, encryptedData.length);
        }
        macData[35 + encryptedData.length] = -128;
        byte[] MAC = getMAC(macData, keyMAC);

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
     * Gets the contents of the personal data dedicated file
     *
     * @param FID   the last bytes of file identifiers being requested
     * @return array containing the data strings
     *
     */
    public String[] readPersonalData(byte[] FID) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {

        String[] personalData = new String[FID.length];
        byte[] data;
        byte[] APDU;
        byte[] response;

        // select the personal data dedicated file
        data = new byte[]{80, 0}; // personal data DF FID
        APDU = createSecureAPDU(data, personal);
        response = idCard.transceive(APDU);
        Log.i("Select personal data DF", Hex.toHexString(response));

        // select and read the first 8 elementary files in the DF
        for (int i = 0; i < FID.length; i++) {

            byte index = FID[i];
            if (index > 15 || index < 1) throw new RuntimeException("Invalid personal data FID.");

            data[1] = index;
            APDU = createSecureAPDU(data, personal);
            response = idCard.transceive(APDU);
            Log.i(String.format("Select EF 500%d", index), Hex.toHexString(response));

            APDU = createSecureAPDU(new byte[0], read);
            response = idCard.transceive(APDU);
            Log.i(String.format("Read binary EF 500%d", index), Hex.toHexString(response));

            // store the decrypted datum
            byte[] raw = encryptDecryptData(Arrays.copyOfRange(response, 3, 19), Cipher.DECRYPT_MODE);
            int indexOfTerminator = Hex.toHexString(raw).lastIndexOf("80") / 2;
            personalData[i] = new String(Arrays.copyOfRange(raw, 0, indexOfTerminator));

        }

        return personalData;

    }
}
