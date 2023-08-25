package com.trans.opengl_es_show;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author Tom灿
 * @description: 定义形状：一个三角形
 * @date :2023/8/18 15:40
 */
public class Triangle {

    private FloatBuffer vertexBuffer;

    // 顶点坐标
    static float triangleCoords[] = {   // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };   // 顶点尚未针对显示 GLSurfaceView 的屏幕区域的比例进行校正


    // 顶点着色器
    private final String vertexShaderCode =       // OpenGL 着色语言 (GLSL) 代码
//            "attribute vec4 vPosition;" +  // 未进行使用矩阵投影和相机转换
            "uniform mat4 uMVPMatrix;" +    // 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "attribute vec4 vPosition;" +   // 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "void main() {" +
//                    "  gl_Position = vPosition;" +       // 未进行使用矩阵投影和相机转换

                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +   // 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "}";

    // 片段着色器
    private final String fragmentShaderCode =     //  OpenGL 着色语言 (GLSL) 代码
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mProgram;

    public Triangle() {
        /**
         * 初始化坐标：
         * 把需要绘制的顶点坐标放入缓冲区
         */
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);


        /**
         * 编译并加载着色器程序：
         * 要绘制形状，必须编译着色程序代码，将它们添加到 OpenGL ES 程序对象中，然后关联该程序。
         * 该操作需要在绘制对象的构造函数中完成，因此只需执行一次
         */
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program  创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program  添加顶点着色器到程序
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program  添加片段着色器到程序
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables  创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);
    }


    private int positionHandle;
    private int colorHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;   // 每个顶点的坐标数

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex 每个顶点四个字节

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    /**
     * 创建用于绘制形状的 draw() 方法。
     * 此代码将位置和颜色值设置为形状的顶点着色程序和 Fragment 着色程序，然后执行绘制功能
     */
    public void draw() {
        // Add program to OpenGL ES environment 添加程序到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member  获取顶点着色器的vPosition成员的句柄
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices  启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data  准备三角坐标数据
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member  获取片段着色器的vColor成员的句柄
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle  设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Draw the triangle  画出三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array  禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle);
    }


    /**
     * 创建用于绘制形状的 draw() 方法。
     * 此代码将位置和颜色值设置为形状的顶点着色程序和 Fragment 着色程序，然后执行绘制功能
     * 应用了相机视图
     */
    public void drawMatrix() {
        setCameraView();
        draw(vPMatrix);
    }

    // Use to access and set the view transformation 用于访问和设置视图转换
    private int vPMatrixHandle;

    /**
     * 创建用于绘制形状的 draw() 方法。
     * 此代码将位置和颜色值设置为形状的顶点着色程序和 Fragment 着色程序，然后执行绘制功能
     *
     * @param mvpMatrix 通过计算的变换矩阵
     */
    public void draw(float[] mvpMatrix) { // pass in the calculated transformation matrix

        // Add program to OpenGL ES environment 添加程序到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member  获取顶点着色器的vPosition成员的句柄
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices  启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data  准备三角坐标数据
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member  获取片段着色器的vColor成员的句柄
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle  设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0);


        // get handle to shape's transformation matrix 得到形状变换矩阵的句柄
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader 将投影和视图转换传递给着色器
        // 将多个矩阵结合后的结果矩阵传递给着色器
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);


        // Draw the triangle  画出这个三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array  禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle);
    }


    private final float[] projectionMatrix = new float[16];

    /**
     * 定义投影矩阵
     *
     * @param width
     * @param height
     */
    public void setMatrix(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates  这个投影矩阵应用于物体坐标
        // in the onDrawFrame() method  在onDrawFrame()方法中
        // 用六个剪辑平面定义一个投影矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }


    // vPMatrix is an abbreviation for "Model View Projection Matrix"  vPMatrix是“模型视图投影矩阵”的缩写。
    private final float[] vPMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    /**
     * 定义相机视图
     */
    public void setCameraView() {
        // 根据一个视点、一个视点中心和一个向上向量定义一个观看变换。
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3,
                0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation  计算投影和视图变换
        // 将两个4x4矩阵相乘并将结果存储在第三个4x4矩阵中，
        // 矩阵表示法:result = lhs * rhs ===》vPMatrix = projectionMatrix * viewMatrix
        Matrix.multiplyMM(vPMatrix,   // 保存结果的浮点矩阵,相乘后的结果矩阵
                0,   // 结果数组偏移量
                projectionMatrix,  // 投影矩阵:保存左侧矩阵的浮点数组
                0,  // 左侧数组偏移量
                viewMatrix,  // 相机视图:保存右侧矩阵的浮点数组
                0);   // 右侧数组偏移量
    }


    private float[] rotationMatrix = new float[16];
    float[] scratch = new float[16];

    /**
     * 绘制动画的模型
     */
    public void drawAnimation() {
        setCameraView();
        animationModel();
        draw(scratch);
    }

    /**
     * 动画
     * 创建一个转换矩阵（旋转矩阵），然后将其与投影和相机视图转换矩阵相结合
     * 启用了连续渲染
     */
    public void animationModel() {
        // Create a rotation transformation for the triangle  为三角形创建一个旋转变换
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        // 创建一个矩阵，用于绕轴(x, y, z)旋转角度a(以度为单位)。
        Matrix.setRotateM(rotationMatrix, 0, angle, 0, 0, -1.0f); // 创建一个旋转矩阵


        // Combine the rotation matrix with the projection and camera view  将旋转矩阵与投影和相机视图结合起来
        // Note that the vPMatrix factor *must be first* in order 注意，vPMatrix因子*必须按顺序排在第一位
        // for the matrix multiplication product to be correct. 矩阵乘法的乘积是正确的。
        Matrix.multiplyMM(scratch, // 结果矩阵
                0, vPMatrix,  // 投影和相机视图结合后矩阵
                0, rotationMatrix, 0); // 旋转矩阵

    }


    /**
     * 绘制响应手势的模型
     */
    public void drawOnTouchRotate(float mAngle) {
        setCameraView();
        rotateModel(mAngle);
        draw(scratch);
    }


    /**
     * 响应onTouch事件 （模型的手势监听）
     * （需要启用连续渲染）
     */
    public void rotateModel(float mAngle) {

        // Create a rotation for the triangle
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);

        // Draw triangle
        draw(scratch);
    }


}

