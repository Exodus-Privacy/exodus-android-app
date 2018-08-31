package org.eu.exodus_privacy.exodusprivacy;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Utils {

    @SuppressLint("PackageManagerGetSignatures")
    public static String getCertificateSHA1Fingerprint(PackageManager pm, String packageName) {
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert packageInfo != null;
        Signature[] signatures = packageInfo.signatures;

        StringBuilder builder = new StringBuilder();
        builder.append(packageName);


        for(Signature signature: signatures) {
            InputStream input = new ByteArrayInputStream(signature.toByteArray());
            CertificateFactory cf = null;
            try {
                cf = CertificateFactory.getInstance("X509");
            } catch (CertificateException e) {
                e.printStackTrace();
            }

            try {
                assert cf != null;
                X509Certificate c = (X509Certificate) cf.generateCertificate(input);
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    byte[] publicKey = md.digest(c.getEncoded());
                    builder.append(' ');
                    builder.append(byte2HexFormatted(publicKey).toUpperCase());
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                }
            } catch (CertificateException e) {
                e.printStackTrace();
            }
        }

        String hexString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(builder.toString().getBytes());
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        assert hexString != null;
        return hexString.toUpperCase();
    }

    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (byte anArr : arr) {
            String h = Integer.toHexString(anArr);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
        }
        return str.toString();
    }
}
