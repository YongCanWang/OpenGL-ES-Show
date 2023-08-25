package com.trans.opengl_es_show;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

/**
 * @author Tom灿
 * @description:
 * @date :2023/8/18 15:16
 */
public class OpenGLActivity extends ComponentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyGLSurfaceView(this));
    }
}
