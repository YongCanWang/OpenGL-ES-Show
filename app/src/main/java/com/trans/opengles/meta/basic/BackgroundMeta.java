package com.trans.opengles.meta.basic;

import android.opengl.GLES30;

import com.trans.opengles.surface.Render;

import javax.microedition.khronos.opengles.GL10;

/**
 * @author Tom灿
 * @description: 绘制背景色
 * @date :2023/11/6 15:18
 */
public class BackgroundMeta implements Render {
    private static final Render INSTANS = new BackgroundMeta();

    public BackgroundMeta() {}

    private void init() {
        /**
         * glClearColor指定了glClear用于清除定点和浮点点颜色缓冲区的红色、绿色、蓝色和alpha值。
         * 无符号归一化的定点RGBA颜色缓冲区被清除为颜色值，
         * 这些颜色值是通过将透明颜色的每个分量固定在[0,1]范围内，然后将(可能是sRGB转换和/或抖动)颜色转换为定点颜色而获得的。
         */
        // 设置背景颜色
        GLES30.glClearColor(0, 0, 1, 1); // 为颜色缓冲区指定明确的值
    }

    private void viewport(int width, int height) {
        //设置视图窗口
        GLES30.glViewport(0, 0, width, height); // 设置视口
    }

    private void drawBackground() {
        /**
         * glClear将窗口的位面区域设置为先前由glClearColor, glClearDepthf和glClearStencil选择的值。
         * 通过使用glDrawBuffers一次选择多个缓冲区，可以同时清除多个颜色缓冲区。
         * 像素所有权测试、剪刀测试、sRGB转换、抖动和缓冲区写掩码都会影响glClear的操作。
         * 剪刀框限定了已清除的区域。Alpha函数、混合函数、模板化、纹理映射和深度缓冲被glClear忽略。
         * glClear接受一个参数，该参数是几个值的位或值，指示要清除哪个缓冲区。取值如下:
         */
        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT); // 将缓冲区清除为预设值
    }


    @Override
    public void shader() {
        init();
    }

    @Override
    public void view(int width, int height) {
        viewport(width, height);
    }

    @Override
    public void draw() {
        drawBackground();
    }

    public static Render Builder() {
        return INSTANS;
    }


}
