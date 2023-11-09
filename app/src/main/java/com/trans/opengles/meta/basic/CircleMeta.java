package com.trans.opengles.meta.basic;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.trans.opengles.R;
import com.trans.opengles.surface.Render;
import com.trans.opengles.utils.ResReadUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * @author Tom灿
 * @description: 绘制一个圆
 * @date :2023/11/8 9:34
 */
public class CircleMeta implements Render {
    private static final Render INSTANS = new CircleMeta();
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indicesBuffer;
    private static final int BYTES_PER_FLOAT = 4;
    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];
    //返回属性变量的位置
    //变换矩阵
    private int uMatrixLocation;
    //位置
    private int aPositionLocation;
    //颜色
    private int aColorLocation;
    int n;
    int radius;
    private float circularCoords[];
    private FloatBuffer circleBuffer;
    //三个顶点
    private static final int POSITION_COMPONENT_COUNT = 3;

    //四个顶点的位置参数
    private float rectangleCoords[] = {
            -0.5f, 0.5f, 0.0f,//top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f, 0.5f, 0.0f // top right
    };

    /**
     * 顶点索引
     */
    private short[] indices = {
            0, 1, 2, 0, 2, 3
    };

    //各个顶点的颜色参数
    private float color[] = {
            0.0f, 0.0f, 1.0f, 1.0f,//top left
            0.0f, 1.0f, 0.0f, 1.0f,// bottom left
            0.0f, 0.0f, 1.0f, 1.0f,// bottom right
            1.0f, 0.0f, 0.0f, 1.0f// top right
    };


    public CircleMeta() {}


    private void init() {
        // 分配内存空间
        initMemory();

        // 编译着色器程序
        // 编译顶点着色器
        int vertexShaderId = compileShader(GLES30.GL_VERTEX_SHADER,
                ResReadUtils.readResource(R.raw.vertex_triangle_shader));
        // TODO 编译顶点着色器：定义了矩阵属性变量
        int vertexIsoscelesShaderId = compileShader(GLES30.GL_VERTEX_SHADER,
                ResReadUtils.readResource(R.raw.rvertex_isosceles_triangle_shader));
        // 编译片段着色器
        int fragmentShaderId = compileShader(GLES30.GL_FRAGMENT_SHADER,
                ResReadUtils.readResource(R.raw.fragment_triangle_shader));

        // 连接着色器程序
        int mProgram = linkProgram(vertexIsoscelesShaderId, fragmentShaderId);

        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        //将背景设置为白色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 获取相关属性变量的句柄
        uMatrixLocation = GLES30.glGetUniformLocation(mProgram, "u_Matrix");  // 矩阵属性变量句柄
        aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition"); // 顶点属性变量句柄
        aColorLocation = GLES30.glGetAttribLocation(mProgram, "aColor"); // 颜色属性变量句柄
    }

    /**
     * 绘制圆: 应用了变换矩阵（添加了相机视图和透视投影）
     */
    private void drawCircle() {

        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        //将变换矩阵传入顶点渲染器
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
        //准备坐标数据
        GLES30.glVertexAttribPointer(
                aPositionLocation,
                3,
                GLES30.GL_FLOAT,
                false, 0,
//                vertexBuffer // TODO 点、线、三角形、矩形
                circleBuffer // TODO 圆
        );

        //启用顶点位置句柄
        GLES30.glEnableVertexAttribArray(aPositionLocation);

        //准备颜色数据
        GLES30.glVertexAttribPointer(aColorLocation,
                4, GLES30.GL_FLOAT, false, 0,
                colorBuffer);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aColorLocation);

        //绘制三个点
        //GLES30.glDrawArrays(GLES30.GL_POINTS, 0, POSITION_COMPONENT_COUNT);

        //绘制三条线
        //GLES30.glLineWidth(3);//设置线宽
        //GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, POSITION_COMPONENT_COUNT);

        //绘制三角形-顶点法
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, POSITION_COMPONENT_COUNT);

        //绘制矩形-索引法
//        GLES30.glDrawElements(GL10.GL_TRIANGLES, indices.length,
//                GL10.GL_UNSIGNED_SHORT, indicesBuffer);

        // 绘制圆
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, circularCoords.length / 3);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aColorLocation);
    }


    /**
     * 通过透视投影矩阵*相机视图矩阵相乘计算得到变换矩阵
     * 正交投影：特点：物体呈现出来的大小不会随着其距离视点的远近而发生变化。
     * 透视投影：特点：物体离视点越远，呈现出来的越小。离视点越近，呈现出来的越大。（类似于我们的眼睛观察世界）
     *
     * @param width
     * @param height
     */
    private void transMatrix(int width, int height) {
        viewport(width, height);
        //计算宽高比
        float ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);


        //正交投影方式
//        final float aspectRatio = width > height ?
//                (float) width / (float) height :
//                (float) height / (float) width;
//        if (width > height) {
//            //横屏
//            Matrix.orthoM(mMVPMatrix, 0, -aspectRatio, aspectRatio,
//                    -1f, 1f, -1f, 1f);
//        } else {
//            //竖屏
//            Matrix.orthoM(mMVPMatrix, 0, -1f, 1f,
//                    -aspectRatio, aspectRatio, -1f, 1f);
//        }
    }


    /**
     * 链接小程序
     *
     * @param vertexShaderId   顶点着色器
     * @param fragmentShaderId 片段着色器
     */
    private int linkProgram(int vertexShaderId, int fragmentShaderId) {
        //创建一个空的OpenGLES程序
        final int programId = GLES30.glCreateProgram();
        if (programId != 0) {
            //将顶点着色器加入到程序
            GLES30.glAttachShader(programId, vertexShaderId);
            //将片元着色器加入到程序中
            GLES30.glAttachShader(programId, fragmentShaderId);
            //链接着色器程序
            GLES30.glLinkProgram(programId);
            final int[] linkStatus = new int[1];

            GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                String logInfo = GLES30.glGetProgramInfoLog(programId);
                System.err.println(logInfo);
                GLES30.glDeleteProgram(programId);
                return 0;
            }
            return programId;
        } else {
            //创建失败
            return 0;
        }
    }


    /**
     * 编译
     *
     * @param type       顶点着色器:GLES30.GL_VERTEX_SHADER
     *                   片段着色器:GLES30.GL_FRAGMENT_SHADER
     * @param shaderCode 着色器语言编写的着色器程序
     * @return
     */
    private int compileShader(int type, String shaderCode) {
        //创建一个着色器
        final int shaderId = GLES30.glCreateShader(type);
        if (shaderId != 0) {
            GLES30.glShaderSource(shaderId, shaderCode);// 给着色器添加源代码
            GLES30.glCompileShader(shaderId);  // 编译着色器(源代码)
            //检测状态
            final int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                String logInfo = GLES30.glGetShaderInfoLog(shaderId);
                System.err.println(logInfo);
                //创建失败
                GLES30.glDeleteShader(shaderId);
                return 0;
            }
            return shaderId;
        } else {
            //创建失败
            return 0;
        }
    }


    /**
     * 分配内存空间
     */
    private void initMemory() {
        //顶点位置相关
        //分配本地内存空间,每个浮点型占4字节空间；将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = ByteBuffer.allocateDirect(rectangleCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(rectangleCoords);
        vertexBuffer.position(0);

        //顶点颜色相关
        colorBuffer = ByteBuffer.allocateDirect(color.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        //顶点索引相关
        indicesBuffer = ByteBuffer.allocateDirect(indices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indicesBuffer.put(indices);
        indicesBuffer.position(0);

        createPositions(1, 60); // 准备圆顶点数据
        // 圆的顶点缓存区
        circleBuffer = ByteBuffer.allocateDirect(circularCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        circleBuffer.put(circularCoords);
        circleBuffer.position(0);

    }

    /**
     * 设置绘制窗口
     *
     * @param width
     * @param height
     */
    private void viewport(int width, int height) {
        //设置绘制窗口
        GLES30.glViewport(0, 0, width, height);
    }


    /**
     * 创建顶点位置和颜色
     *
     * @param radius 半径
     * @param n
     */
    private void createPositions(int radius, int n) {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        float angDegSpan = 360f / n;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (radius * Math.sin(i * Math.PI / 180f)));
            data.add((float) (radius * Math.cos(i * Math.PI / 180f)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }

        circularCoords = f;

        //处理各个顶点的颜色
        color = new float[f.length * 4 / 3];
        ArrayList<Float> tempC = new ArrayList<>();
        ArrayList<Float> totalC = new ArrayList<>();
        tempC.add(1.0f);
        tempC.add(0.0f);
        tempC.add(0.0f);
        tempC.add(1.0f);
        for (int i = 0; i < f.length / 3; i++) {
            totalC.addAll(tempC);
        }

        for (int i = 0; i < totalC.size(); i++) {
            color[i] = totalC.get(i);
        }
    }


    private float[] createPositions() {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        float angDegSpan = 360f / n;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (radius * Math.sin(i * Math.PI / 180f)));
            data.add((float) (radius * Math.cos(i * Math.PI / 180f)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }
        return f;
    }

    @Override
    public void shader() {
        init();
    }

    @Override
    public void view(int width, int height) {
      transMatrix(width, height);
    }


    @Override
    public void draw() {
        drawCircle();
    }

    public static Render Builder() {
        return INSTANS;
    }


}
