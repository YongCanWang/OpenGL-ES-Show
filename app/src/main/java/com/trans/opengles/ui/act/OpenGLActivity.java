package com.trans.opengles.ui.act;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.trans.opengles.surface.MyGLSurfaceView;
import com.trans.opengles.surface.Render;

/**
 * @author Tom灿
 * @description:
 * @date :2023/8/18 15:16
 */
public class OpenGLActivity extends ComponentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Class clazz = (Class) getIntent().getBundleExtra("class")
                    .getSerializable("class");
            Render render = (Render) clazz.newInstance();
            setContentView(new MyGLSurfaceView(this, render));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
