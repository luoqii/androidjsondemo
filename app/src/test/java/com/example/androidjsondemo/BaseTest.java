package com.example.androidjsondemo;

import java.io.BufferedInputStream;
import java.io.IOException;

public class BaseTest {
    public byte[] resourceFileData(String resourceFileName){
        BufferedInputStream bIn = null;
        try {
            bIn = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(resourceFileName));
            byte[] data = null;
            int totalLen = 0;
            byte[] buffer = new byte[8* 1024];
            int read;
            while ((read = bIn.read(buffer)) != -1){
                totalLen += read;
                if (null == data){
                    data = new byte[totalLen];
                    System.arraycopy(buffer, 0, data, 0, read);
                } else {
                    byte[] tmp = new byte[totalLen];
                    System.arraycopy(data, 0, tmp, 0, data.length);
                    System.arraycopy(buffer, 0, tmp, data.length, read);
                    data = tmp;
                }

            }

            return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != bIn){
                    bIn.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
