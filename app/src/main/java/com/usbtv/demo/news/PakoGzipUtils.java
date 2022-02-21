package com.usbtv.demo.news;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PakoGzipUtils {

    /**
     *
     *     * @param str：正常的字符串
     *
     *     * @return 压缩字符串 类型为：  ³)°K,NIc i£_`Çe#  c¦%ÂXHòjyIÅÖ`
     *
     *     * @throws IOException
     *
     */

    public static String compress(String str) throws IOException {

        if (str == null || str.length() == 0) {

            return str;

        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        GZIPOutputStream gzip = new GZIPOutputStream(out);

        gzip.write(str.getBytes());

        gzip.close();

        return out.toString("ISO-8859-1");

    }


    public static  String uncompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }

        byte[] bytes = str.getBytes("ISO-8859-1");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }


        return new String( out.toByteArray());
    }

    public static String deCompress(String str) throws IOException {

        if (str == null || str.length() == 0) {

            return str;

        }

        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));

        GZIPInputStream gzip = new GZIPInputStream(in);

        byte [] bytes = new byte[gzip.available()];
        gzip.read(bytes);

        gzip.close();

        return new String(bytes);

    }
}
