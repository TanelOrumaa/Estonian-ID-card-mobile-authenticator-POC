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

    private static final byte[] selectMaster = Hex.decode("00a4040c10a000000077010800070000fe00000100");

    private static final byte[] MSESetAT = Hex.decode("0022c1a40f800a04007f0007020204020483010200");

    private static final byte[] GAGetNonce = Hex.decode("10860000027c0000");

    private static final byte[] GAMapNonceIncomplete = Hex.decode("10860000457c438141");

    private static final byte[] GAKeyAgreementIncomplete = Hex.decode("10860000457c438341");

    private static final byte[] GAMutualAuthenticationIncomplete = Hex.decode("008600000c7c0a8508");

    private static final byte[] dataForMACIncomplete = Hex.decode("7f494f060a04007f000702020402048641");

    private static final byte[] selectFile = Hex.decode("0ca4010c1d871101");

    private static final byte[] readFile = Hex.decode("0cb000000d970100");

    private static final byte[] verifyPIN1 = Hex.decode("0c2000011d871101");

    private static final byte[] verifyPIN2 = Hex.decode("0c2000851d871101");

    private static final byte[] IASECCFID = {0x3f, 0x00};
    private static final byte[] personalDF = {0x50, 0x00};
    private static final byte[] AWP = {(byte) 0xad, (byte) 0xf1};
    private static final byte[] QSCD = {(byte) 0xad, (byte) 0xf2};
    private static final byte[] authCert = {0x34, 0x01};
    private static final byte[] signCert = {0x34, 0x1f};

    private final IsoDep idCard;
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
        byte[][] keys = PACE(CAN);
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

        // select the IAS-ECC application on the chip
        byte[] response = idCard.transceive(selectMaster);
        Log.i("Select the master application", Hex.toHexString(response));

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
            throw new RuntimeException("Could not verify chip's MAC."); // *Should* never happen.
        }
        return new byte[][]{keyEnc, keyMAC};

    }

    /**
     * Selects a file and reads its contents
     *
     * @param FID   file identifier of the required file
     * @param info  string for logging
     * @return decrypted file contents
     */
    private byte[] readFile(byte[] FID, String info) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        selectFile(FID, info);
        byte[] APDU = createSecureAPDU(new byte[0], readFile);
        byte[] response = idCard.transceive(APDU);
        Log.i("Read binary", Hex.toHexString(response));
        if (response[response.length - 2] != -112 || response[response.length - 1] != 0) {
            throw new RuntimeException(String.format("Could not read %s", info));
        }
        return encryptDecryptData(Arrays.copyOfRange(response, 3, 19), Cipher.DECRYPT_MODE);
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
        byte[] APDU = Arrays.copyOf(incomplete, incomplete.length + encryptedData.length + MAC.length + 3);
        if (encryptedData.length > 0) {
            System.arraycopy(encryptedData, 0, APDU, incomplete.length, encryptedData.length);
        }
        System.arraycopy(new byte[]{(byte) 0x8E, 0x08}, 0, APDU, incomplete.length + encryptedData.length, 2); // MAC is encapsulated using the tag 0x8E
        System.arraycopy(MAC, 0, APDU, incomplete.length + encryptedData.length + 2, MAC.length);
        ssc++;
        return APDU;

    }

    /**
     * Selects a FILE by its identifier
     *
     */
    private void selectFile(byte[] FID, String info) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        byte[] APDU = createSecureAPDU(FID, selectFile);
        byte[] response = idCard.transceive(APDU);
        Log.i(String.format("Select %s", info), Hex.toHexString(response));
        if (response[response.length - 2] != -112 || response[response.length - 1] != 0) {
            throw new RuntimeException(String.format("Could not select %s", info));
        }
    }

    /**
     * Gets the contents of the personal data dedicated file
     *
     * @param lastBytes   the last bytes of the personal data file identifiers (0 < x < 16)
     * @return array containing the corresponding data strings
     */
    public String[] readPersonalData(byte[] lastBytes) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {

        String[] personalData = new String[lastBytes.length];
        int stringIndex = 0;

        // select the master application
        selectFile(IASECCFID, "the master application");

        // select the personal data dedicated file
        selectFile(personalDF, "the personal data DF");

        byte[] FID = Arrays.copyOf(personalDF, personalDF.length);
        // select and read the personal data elementary files
        for (byte index : lastBytes) {

            if (index > 15 || index < 1) throw new RuntimeException("Invalid personal data FID.");
            FID[1] = index;

            // store the decrypted datum
            byte[] response = readFile(FID, "a personal data EF");
            int indexOfTerminator = Hex.toHexString(response).lastIndexOf("80") / 2;
            personalData[stringIndex++] = new String(Arrays.copyOfRange(response, 0, indexOfTerminator));

        }
        return personalData;

    }

    /**
     * Attempts to verify the selected PIN
     *
     * @param PIN user-provided PIN
     * @param oneOrTwo true for PIN1, false for PIN2
     */
    public void verifyPIN(byte[] PIN, boolean oneOrTwo) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {

        selectFile(IASECCFID, "the master application");
        if (!oneOrTwo) {
            selectFile(QSCD, "the application");
        }

        // pad the PIN and use the chip for verification
        byte[] paddedPIN1 = Hex.decode("ffffffffffffffffffffffff");
        System.arraycopy(PIN, 0, paddedPIN1, 0, PIN.length);
        byte[] APDU = createSecureAPDU(paddedPIN1, oneOrTwo ? verifyPIN1 : verifyPIN2);
        byte[] response = idCard.transceive(APDU);
        Log.i(String.format("PIN%d verification", oneOrTwo ? 1 : 2), Hex.toHexString(response));

        byte sw1 = response[response.length - 2];
        byte sw2 = response[response.length - 1];
        if (sw1 != -112 || sw2 != 0) {
            if (sw1 == 105 && sw2 == -125) {
                throw new RuntimeException("Invalid PIN. Authentication method blocked.");
            } else {
                throw new RuntimeException(String.format("Invalid PIN1. %d attempt%s left.", sw2 + 64, sw2 == -63 ? "" : "s"));
            }
        }
    }

    /**
     * Retrieves the authentication or signature certificate from the chip
     *
     * @param authOrSign true for auth, false for sign cert
     * @return the requested certificate
     */
    public byte[] getCertificate(boolean authOrSign) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {

        selectFile(IASECCFID, "the master application");

        selectFile(authOrSign ? AWP : QSCD, "the application");

        selectFile(authOrSign ? authCert : signCert, "the certificate");

        byte[] certificate = new byte[0];
        byte[] readCert = Arrays.copyOf(readFile, readFile.length);
        // Construct the certificate byte array n=indexOfTerminator bytes at a time
        for (int i = 0; i < 16; i++) {

            // Set the P1/P2 values to incrementally read the certificate
            readCert[2] = (byte) (certificate.length / 256);
            readCert[3] = (byte) (certificate.length % 256);
            byte[] APDU = createSecureAPDU(new byte[0], readCert);
            byte[] response = idCard.transceive(APDU);
            Log.i("Read the certificate", Hex.toHexString(response));

            byte sw1 = response[response.length - 2];
            byte sw2 = response[response.length - 1];
            if (sw1 == 107 && sw2 == 0) {
                throw new RuntimeException("Wrong read parameters.");
            }

            // Set the range containing a portion of the certificate and decrypt it
            int start = response[2] == 1 ? 3 : 4;
            int end = start + (response[start - 2] + 256) % 256 - 1;
            byte[] decrypted = encryptDecryptData(Arrays.copyOfRange(response, start, end), Cipher.DECRYPT_MODE);
            int indexOfTerminator = Hex.toHexString(decrypted).lastIndexOf("80") / 2;
            certificate = Arrays.copyOf(certificate, certificate.length + indexOfTerminator);
            System.arraycopy(decrypted, 0, certificate, certificate.length - indexOfTerminator, indexOfTerminator);

            if (sw1 == -112 && sw2 == 0) {
                break;
            }
        }

        // For debugging, ascertain that the byte array corresponds to a valid certificate
//        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
//        X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificate));
//        Log.i("Certificate serial number", String.valueOf(x509Certificate.getSerialNumber()));

        return certificate;

    }
}
