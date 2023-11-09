package com.trans.opengles.utils;

import android.content.res.Resources;

import com.trans.opengles.MyApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Tom灿
 * @description:
 * @date :2023/11/6 16:19
 */
public class ResReadUtils {

    /**
     * 读取资源
     *
     * @param resourceId
     */
    public static String readResource(int resourceId) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = MyApplication.application.getResources().openRawResource(resourceId);
            InputStreamReader streamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String textLine;
            while ((textLine = bufferedReader.readLine()) != null) {
                builder.append(textLine);
                builder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

}
