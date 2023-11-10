package com.trans.opengles.surface;

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
    private final Render render;

    public MyGLRenderer(Render render) {
        this.render = render;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        render.shader();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 通过投影来调整坐标
        render.view(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        render.draw();
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

        /**
         * glCreateShader创建一个空的着色器对象，并返回一个非零值，它可以被引用。
         * 着色器对象用于维护定义着色器的源代码字符串。shaderType表示要创建的着色器类型。
         * 支持两种类型的着色器。
         * GL_VERTEX_SHADER类型的着色器是一个着色器，旨在在可编程顶点处理器上运行。
         * GL_FRAGMENT_SHADER类型的着色器是一个着色器，旨在在可编程片段处理器上运行。当创建时，
         * 着色器对象的GL_SHADER_TYPE参数被设置为GL_VERTEX_SHADER或GL_FRAGMENT_SHADER，这取决于shaderType的值。
         *
         * 参数-shaderType:指定要创建的着色器类型。必须是GLVERTEX_SHADER或GL_FRACMENT_SHApER。
         */
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);  // 创建一个着色器对象

        /**
         * 对于支持shader编译器的实现，glShaderSource将shader中的源代码设置为string指定的字符串数组中的源代码。
         * 以前存储在着色器对象中的任何源代码都将被完全替换。数组中的字符串数由count指定。如果length为NuLL，
         * 则假定每个字符串以空结束。如果length不是NULL值，它指向一个数组，其中包含string的每个相应元素的字符串长度。
         * length数组中的每个元素可以包含对应字符串的长度(null字符不被计算在字符串长度中)或小于0的值，
         * 以表示字符串以null结束。源代码字符串此时不会被扫描或解析;它们被简单地复制到指定的着色器对象中。
         *
         * 参数1-shader:着色器-指定要替换源代码的着色器对象的句柄。
         * 参数2-count:数-指定string和length数组中的元素个数。
         * 参数3-**string:字符串-指定一个指向字符串的指针数组，其中包含要加载到着色器中的源代码。
         * 参数4-*length:长度-指定字符串长度的数组。
         */
        // add the source code to the shader and compile it  // 将源代码添加到着色器并编译它
        GLES20.glShaderSource(shader, shaderCode); // 在着色器对象中替换源代码
        /**
         * 对于支持shader编译器的实现，glCompileShader编译已经存储在shader指定的shader对象中的源代码字符串。
         * 编译状态将作为着色器对象状态的一部分存储。这个值将被设置为GL_TRue，如果着色器编译没有错误并且准备好使用，
         * 否则GL_FALSE。它可以通过使用参数shader和GL_COMPILE_STATUS调用glGetShaderiv来查询。
         * 由于OpenGL ES着色语言规范规定的一些原因，着色器的编译可能会失败。无论编译是否成功，
         * 都可以通过调用glGetShaderlnfoLog从着色器对象的信息日志中获得有关编译的信息。
         *
         * 参数-shader:指定要编译的着色器对象。
         */
        GLES20.glCompileShader(shader); // 编译着色器对象(编译着色器指定的源代码)
        return shader;  // 编译成功(GL_TRue),并准备好使用的着色器
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
