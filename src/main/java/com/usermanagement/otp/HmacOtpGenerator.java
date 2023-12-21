/*
package com.usermanagement.otp;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

*/
/**
 * This class contains static methods that are used to calculate the
 * One-Time Password (OTP) using JCE to provide the HMAC-SHA-1.
 *
 * Adapted from https://www.ietf.org/rfc/rfc4226.txt
 *//*

public class HmacOtpGenerator {
    */
/**
     * This method uses the JCE to provide the HMAC-SHA-1 algorithm.
     * HMAC computes a Hashed Message Authentication Code and in this case SHA1 is the hash algorithm used.
     *
     * @param keyBytes   the bytes to use for the HMAC-SHA-1 key
     * @param text       the message or text to be authenticated.
     *
     *//*

    private static byte[] hmacSha1(byte[] keyBytes, byte[] text) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha1;
        try {
            hmacSha1 = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            hmacSha1 = Mac.getInstance("HMAC-SHA-1");
        }
        SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
        hmacSha1.init(macKey);
        return hmacSha1.doFinal(text);
    }

    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    */
/**
     * This method generates an OTP value for the given
     * set of parameters.
     *
     * @param secret       the shared secret
     * @param movingFactor the counter, time, or other value that changes on a per-use basis.
     * @param codeDigits   the number of digits in the OTP, not including the checksum, if any.
     *
     * @return A numeric String in base 10 that includes [codeDigits] digits
     *//*

    public static String generateOTP(byte[] secret, long movingFactor, int codeDigits) {
        // put movingFactor value into text byte array
        long mf = movingFactor;
        byte[] text = new byte[8];
        for (int i = text.length - 1; i >= 0; i--) {
            text[i] = (byte) (mf & 0xffL);
            mf = mf >> 8;
        }
        // compute hmac hash
        byte[] hash;
        try {
            hash = hmacSha1(secret, text);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        int otp = binary % DIGITS_POWER[codeDigits];
        String result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }
}


*/
