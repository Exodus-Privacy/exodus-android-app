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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /*
    Simple and not complete markdownToHtml converter
     */
    public static String markdownToHtml(String markdown) {
        StringBuilder builder = new StringBuilder();
        String[] lines = markdown.split("\r\n");
        ArrayList<String> listStarter = new ArrayList<>();
        ArrayList<String> formatStarter = new ArrayList<>();
        ArrayList<String> closeTags = new ArrayList<>();
        for(String line : lines) {
            if (line.matches("^#{1,5} .*")) {
                int nb = line.indexOf(" ");
                String hx = "<h"+nb+">";
                String endhx = "</h"+nb+">";
                builder.append(hx);
                closeTags.add(endhx);
                line = line.substring(line.indexOf(" ")+1);
            } else if (line.matches("^ *[+\\-*] .*")) {
                String starter="";
                if (listStarter.size() > 0 && line.startsWith(listStarter.get(listStarter.size()-1))) {
                    starter = listStarter.get(listStarter.size()-1);
                } else {
                    Pattern pattern = Pattern.compile("^( *[+\\-*] )");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        starter = matcher.group(1);
                        listStarter.add(starter);
                        builder.append("<ul>\n");

                    }
                }
                builder.append("<li> ");
                int beginIndex = line.indexOf(starter)+starter.length();
                line = line.substring(beginIndex);
                closeTags.add("</li>");
            } else {
                while(!listStarter.isEmpty()) {
                    listStarter.remove(listStarter.size() - 1);
                    builder.append("</ul>\n");
                }
                builder.append("<p>");
                closeTags.add("</p>");
            }
            while(!line.isEmpty()){
                Pattern pattern = Pattern.compile("^\\[(.+?)(?=\\]\\()\\]\\((http.+?)(?=\\))\\)");
                //Pattern pattern = Pattern.compile("^\\[(.*)\\]\\((http.*)\\)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    builder.append("<a href=\"");
                    builder.append(matcher.group(2));
                    builder.append("\">");
                    builder.append(matcher.group(1));
                    builder.append("</a>");
                    line = line.substring(line.indexOf(")")+1);
                    continue;
                }
                pattern = Pattern.compile("^(http.*)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    builder.append("<a href=\"");
                    builder.append(matcher.group(1));
                    builder.append("\">");
                    builder.append(matcher.group(1));
                    builder.append("</a>");
                    line = line.substring(matcher.group(1).length());
                    continue;
                }
                pattern = Pattern.compile("^[*_]{2}(.+)[*_]{2}");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if(line.startsWith("*")) {
                        line = line.replaceFirst("\\*\\*", "<b>");
                        formatStarter.add("**");
                    }
                    else {
                        line = line.replaceFirst("__", "<b>");
                        formatStarter.add("__");
                    }
                    continue;
                }
                pattern = Pattern.compile("^[*_]{1}(.+)");
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if(line.startsWith("*")) {
                        line = line.replaceFirst("\\*", "<i>");
                        formatStarter.add("*");
                    }
                    else {
                        line = line.replaceFirst("_", "<i>");
                        formatStarter.add("_");
                    }
                    continue;
                }
                if(formatStarter.size() > 0) {
                    String checkFormat;
                    if(line.contains(" "))
                        checkFormat = line.substring(0,line.indexOf(" "));
                    else
                        checkFormat = line;
                    String lastFormat = formatStarter.get(formatStarter.size()-1);
                    if (checkFormat.contains(lastFormat)) {
                        if(lastFormat.length()==2) {
                            if (lastFormat.contains("*"))
                                line = line.replaceFirst("\\*\\*", "</b>");
                            else
                                line = line.replaceFirst("__", "</b>");
                        } else {
                            if (lastFormat.contains("*"))
                                line = line.replaceFirst("\\*", "</i>");
                            else
                                line = line.replaceFirst("_", "</i>");
                        }
                        formatStarter.remove(formatStarter.size()-1);
                        continue;
                    }
                }

                if(line.contains(" ")) {
                    builder.append(line.substring(0, line.indexOf(" ") + 1));
                    line = line.substring(line.indexOf(" ") + 1);
                } else {
                    builder.append(line);
                    line = "";
                }
            }
            //close all unclosed tags starting at the end
            while(!closeTags.isEmpty()) {
                builder.append(closeTags.remove(closeTags.size()-1));
            }
            builder.append("\n");

        }
        while(!listStarter.isEmpty()) {
            listStarter.remove(listStarter.size() - 1);
            builder.append("</ul>\n");
        }
        return builder.toString();
    }
}
