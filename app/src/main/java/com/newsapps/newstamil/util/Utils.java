package com.newsapps.newstamil.util;

import android.content.Context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by NewsTamil on 1/9/2016.
 */
public class Utils {

    public static String getAndroidId(Context context) {
        String androidId = ""
                + android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        return androidId;
    }


    public static String performHash256(String uniqueKey) {

        String hashedKey = "";
        hashedKey = uniqueKey;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        md.update(hashedKey.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        //System.out.println("Hex format : " + sb.toString()+"LENGHT:"+sb.length());
        hashedKey = sb.toString();
        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<byteData.length;i++) {
            String hex=Integer.toHexString(0xff & byteData[i]);
            if(hex.length()==1) hexString.append('0');
            hexString.append(hex);
        }
        //System.out.println("Hex format : " + hexString.toString()+"LENGTH:"+hexString.length());
        return hashedKey;

    }
}
