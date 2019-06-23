package org.toughproxy.common;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

public class NetUtils {


    public static byte[] encodeString(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            return str.getBytes();
        }
    }


    public static String decodeString(byte[] utf8) {
        try {
            return new String(utf8, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            return new String(utf8);
        }
    }

    public static byte[] encodeShort(short val){
        byte[] b = new byte[2];
        b[1] = (byte) (val >>> 0);
        b[0] = (byte) (val >>> 8);
        return b;
    }

    public static short decodeShort(byte[] b){
        return (short) (((b[1] & 0xFF) << 0) + ((b[0] & 0xFF) << 8));
    }

    public static byte[] encodeInt(int val){
        byte[] b = new byte[4];
        b[3] = (byte) (val >>> 0);
        b[2] = (byte) (val >>> 8);
        b[1] = (byte) (val >>> 16);
        b[0] = (byte) (val >>> 24);
        return b;
    }

    public static int decodeInt(byte b[]){
        return ((b[3] & 0xFF) << 0) + ((b[2] & 0xFF) << 8)
                + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
    }


    public static String getHexString(byte[] data) {
        StringBuffer hex = new StringBuffer("0x");
        if (data != null)
            for (int i = 0; i < data.length; i++) {
                String digit = Integer.toString(data[i] & 0x0ff, 16);
                if (digit.length() < 2)
                    hex.append('0');
                hex.append(digit);
            }
        return hex.toString();
    }

    public static String decodeIpv4(byte[] src){
        if (src.length!=4)
            throw new IllegalArgumentException("bad IP bytes");
        return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
    }

    public static byte[] encodeIpV4(String value){
        try {
            return Inet4Address.getByName(value).getAddress();
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("bad IP number");
        }
    }

    public static byte[] encodeIpV6(String value){
        try {
            return Inet6Address.getByName(value).getAddress();
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("bad IP number");
        }
    }

    public static byte[] encodeMacAddr(String value){
        if (value == null || value.length() != 17)
            throw new IllegalArgumentException("bad mac");

        value = value.replaceAll("-",":");
        byte []macBytes = new byte[6];
        String [] strArr = value.split(":");

        for(int i = 0;i < strArr.length; i++){
            int val = Integer.parseInt(strArr[i],16);
            macBytes[i] = (byte) val;
        }
        return macBytes;
    }

    public static String decodeMacAddr(byte [] src){
        String value = "";
        for(int i = 0;i < src.length; i++){
            String sTemp = Integer.toHexString(0xFF &  src[i]);
            if(sTemp.equals("0")){
                sTemp += "0";
            }
            value = value+sTemp+":";
        }
        return value.substring(0,value.lastIndexOf(":"));
    }

}
