package com.trans.opengl_es_show;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Tom灿
 * @description: 渲染程序类：此类可控制在与之相关联的 GLSurfaceView 上绘制的内容
 * @date :2023/8/18 15:21
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    Triangle triangle;
    Square square;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        triangle = new Triangle();
        square = new Square();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Set the background frame color
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // 通过投影来调整坐标
        triangle.setMatrix(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Set the background frame color
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//        triangle.draw(); // 绘制
//        triangle.drawMatrix();  // 绘制： 应用了投影和相机视图
//        triangle.drawAnimation(); // 绘制：应用了投影和相机视图以及旋转矩阵
        triangle.drawOnTouchRotate(-mAngle);// 绘制：应用了投影和相机视图以及旋转矩阵,并响应手势
    }


    /**
     * 加载着色器程序
     * 着色程序包含 OpenGL 着色语言 (GLSL) 代码，必须先对其进行编译，然后才能在 OpenGL ES 环境中使用
     *
     * @param type
     * @param shaderCode
     * @return
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    // 由于渲染程序代码在独立于应用的主界面线程的线程上运行，因此必须将此公开变量声明为 volatile
    public volatile float mAngle; // 旋转角度


    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }
}
