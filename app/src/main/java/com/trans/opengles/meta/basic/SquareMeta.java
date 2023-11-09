package com.trans.opengles.meta.basic;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.trans.opengles.surface.Render;
import com.trans.opengles.surface.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author Tom灿
 * @description: 定义形状：一个矩形，由两个三角形组成
 * @date :2023/8/18 15:44
 */
public class SquareMeta implements Render {
    private static final Render INSTANS = new SquareMeta();
    private int mProgram;
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    private final int COORDS_PER_VERTEX = 3;
    private float squareCoords[] = {
            -0.5f, 0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f, 0.5f, 0.0f}; // top right

    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;

    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    // 顶点着色器-源代码
    private final String vertexShaderCode =       // OpenGL 着色语言 (GLSL) 代码
            "uniform mat4 uMVPMatrix;" +    //TODO 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "attribute vec4 vPosition;" +   //TODO 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position // 矩阵必须作为gl_Position的修饰符包含
                    // Note that the uMVPMatrix factor *must be first* in order // 注意，uMVPMatrix因子*必须按顺序排在第一位
                    // for the matrix multiplication product to be correct. // 矩阵乘法的乘积是正确的。
                    "  gl_Position = uMVPMatrix * vPosition;" +   //TODO 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "}";

    // 片段着色器-源代码
    private final String fragmentShaderCode =     //  OpenGL 着色语言 (GLSL) 代码
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    public SquareMeta() {
    }


    private void init() {
        // initialize vertex byte buffer for shape coordinates // 初始化顶点字节缓冲区的形状坐标
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list // 为绘制列表初始化字节缓冲区
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,  // 顶点着色器,可在可编程顶点处理器上运行
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, // 片段着色器,可在可编程片段处理器上运行
                fragmentShaderCode);

        // create empty OpenGL ES Program  创建空的OpenGL ES程序
        // 可以附加着色器的GLES程序对象
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program  添加顶点着色器到程序
        GLES20.glAttachShader(mProgram, vertexShader);  // 将顶点着色器对象附加到程序对象中,要想被GLES程序连接到,必须先添加到GLES程序中

        // add the fragment shader to program  添加片段着色器到程序
        GLES20.glAttachShader(mProgram, fragmentShader); // 将片段着色器对象附加到程序对象中,要想被GLES程序连接到,必须先添加到GLES程序中

        // creates OpenGL ES program executables  创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);  // 链接着色器程序对象.可以在程序对象中创建一个或多个可执行文件
    }

    int positionHandle;
    int colorHandle;
    int vPMatrixHandle;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex 每个顶点四个字节

    /**
     * 绘制
     */
    private void drawSquare() {
        setCameraView();
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
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix, 0);


        // Draw the triangle  画出这个三角形-顶点法
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // 绘制正方形-索引法
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


        // Disable vertex array  禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle);
    }


    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] vPMatrix = new float[16];

    /**
     * 定义相机视图
     */
    private void setCameraView() {
        // 根据一个视点、一个视点中心和一个向上向量定义一个观看变换。
        Matrix.setLookAtM(viewMatrix,
                0,
                0,
                0,
                -3,
                0f,
                0f,
                0f,
                0f,
                1.0f,
                0.0f);

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


    /**
     * 定义透视投影
     *
     * @param width
     * @param height
     */
    private void setMatrix(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates  这个投影矩阵应用于物体坐标
        // in the onDrawFrame() method  在onDrawFrame()方法中
        // 用六个剪辑平面定义一个投影矩阵
        Matrix.frustumM(projectionMatrix,  // 填充了一个投影矩阵 projectionMatrix
                0, -ratio, ratio, -1, 1, 3, 7);
    }


    @Override
    public void shader() {
        init();
    }

    @Override
    public void view(int width, int height) {
        setMatrix(width, height);
    }


    @Override
    public void draw() {
        drawSquare();
    }

    public static Render Builder() {
        return INSTANS;
    }


}
