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
 * @description: 绘制圆柱体
 * @date :2023/11/8 9:34
 */
public class CylinderMeta implements Render {
    private static final Render INSTANS = new CylinderMeta();
    private FloatBuffer colorBuffer;
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
    private static final int SEPARATE_COUNT = 120;
    private static final float RADIUS = 0.5f;
    private static final float HEIGHT = 1.0f;
    private float cylinderCoords[];
    private float cylinderTopCoords[];
    private float cylinderBottomCoords[];
    private FloatBuffer cylinderBuffer;
    private FloatBuffer cylinderTopBuffer;
    private FloatBuffer cylinderBottomBuffer;
    //三个顶点
    private static final int POSITION_COMPONENT_COUNT = 3;


    //各个顶点的颜色参数
    private float color[] = {
            0.0f, 0.0f, 1.0f, 1.0f,//top left
            0.0f, 1.0f, 0.0f, 1.0f,// bottom left
            0.0f, 0.0f, 1.0f, 1.0f,// bottom right
            1.0f, 0.0f, 0.0f, 1.0f// top right
    };


    public CylinderMeta() {
    }


    private void init() {
        // 分配内存空间
        initMemory();

        // 编译着色器程序
        // 编译顶点着色器
        int vertexShaderId = compileShader(GLES30.GL_VERTEX_SHADER,
                ResReadUtils.readResource(R.raw.vertex_cylinder_shader));
        // 编译片段着色器
        int fragmentShaderId = compileShader(GLES30.GL_FRAGMENT_SHADER,
                ResReadUtils.readResource(R.raw.fragment_cone_shader));

        // 连接着色器程序
        int mProgram = linkProgram(vertexShaderId, fragmentShaderId);

        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        //将背景设置为灰色
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        // 获取相关属性变量的句柄
        uMatrixLocation = GLES30.glGetUniformLocation(mProgram, "u_Matrix");  // 矩阵属性变量句柄
        aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition"); // 顶点属性变量句柄
        aColorLocation = GLES30.glGetAttribLocation(mProgram, "aColor"); // 颜色属性变量句柄
    }

    /**
     * 绘制圆柱体: 应用了变换矩阵（添加了相机视图和透视投影）
     */
    private void drawCylinder() {

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
                cylinderBuffer // TODO 圆柱体侧面
        );
        //启用顶点位置句柄
        GLES30.glEnableVertexAttribArray(aPositionLocation);

        //准备颜色数据
        GLES30.glVertexAttribPointer(aColorLocation,
                4, GLES30.GL_FLOAT, false, 0,
                colorBuffer);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aColorLocation);
        // 绘制圆柱体侧面
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, cylinderCoords.length / 3);


        //准备圆柱体顶部坐标数据
        GLES30.glVertexAttribPointer(
                aPositionLocation,
                3,
                GLES30.GL_FLOAT,
                false, 0,
                cylinderTopBuffer // TODO 圆柱体顶部
        );
        //绘制圆锥顶部
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, cylinderTopCoords.length / 3);


        //准备圆柱体底部坐标数据
        GLES30.glVertexAttribPointer(
                aPositionLocation,
                3,
                GLES30.GL_FLOAT,
                false, 0,
                cylinderBottomBuffer // TODO 圆柱体顶部
        );
        //绘制圆锥顶部
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, cylinderBottomCoords.length / 3);

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
//        Matrix.setLookAtM(mViewMatrix, 0,   // 圆的相机
//                   0, 0, 7.0f,
//                 0f, 0f, 0f,
//                   0f, 1.0f, 0.0f);
        Matrix.setLookAtM(mViewMatrix, 0,  // 圆柱体的相机
                6, 0, -1f,
                0f, 0f, 0f,
                0f, 0.0f, 1.0f);
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
        createPositions(); // 准备圆柱体顶点数据
        createTopAndBottomPlanePositions(); // 圆柱体顶部以及底部顶点数据
        // 圆柱体的顶点缓存区
        cylinderBuffer = ByteBuffer.allocateDirect(cylinderCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        cylinderBuffer.put(cylinderCoords);
        cylinderBuffer.position(0);

        // 圆柱体顶部顶点缓存区
        cylinderTopBuffer = ByteBuffer.allocateDirect(cylinderTopCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        cylinderTopBuffer.put(cylinderTopCoords);
        cylinderTopBuffer.position(0);

        // 圆柱体底部顶点缓存区
        cylinderBottomBuffer = ByteBuffer.allocateDirect(cylinderBottomCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        cylinderBottomBuffer.put(cylinderBottomCoords);
        cylinderBottomBuffer.position(0);

        //顶点颜色相关
        colorBuffer = ByteBuffer.allocateDirect(color.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);
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
     * 创建圆柱体侧面顶点位置
     */
    private void createPositions() {
        ArrayList<Float> pos = new ArrayList<>();
        float angDegSpan = 360f / SEPARATE_COUNT;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            pos.add((float) (RADIUS * Math.sin(i * Math.PI / 180f)));
            pos.add((float) (RADIUS * Math.cos(i * Math.PI / 180f)));
            pos.add(HEIGHT);
            pos.add((float) (RADIUS * Math.sin(i * Math.PI / 180f)));
            pos.add((float) (RADIUS * Math.cos(i * Math.PI / 180f)));
            pos.add(0.0f);
        }
        float[] d = new float[pos.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = pos.get(i);
        }

        cylinderCoords = d;
    }


    /**
     * 创建圆柱体顶部顶点位置和底部顶点位置
     */
    private void createTopAndBottomPlanePositions() {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f);
        data.add(0.0f);
        data.add(HEIGHT);

        ArrayList<Float> data1 = new ArrayList<>();
        data1.add(0.0f);
        data1.add(0.0f);
        data1.add(0.0f);

        float angDegSpan = 360f / SEPARATE_COUNT;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (RADIUS * Math.sin(i * Math.PI / 180f)));
            data.add((float) (RADIUS * Math.cos(i * Math.PI / 180f)));
            data.add(HEIGHT);

            data1.add((float) (RADIUS * Math.sin(i * Math.PI / 180f)));
            data1.add((float) (RADIUS * Math.cos(i * Math.PI / 180f)));
            data1.add(0.0f);
        }
        float[] f = new float[data.size()];
        float[] f1 = new float[data.size()];

        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
            f1[i] = data1.get(i);
        }

        cylinderTopCoords = f;
        cylinderBottomCoords = f1;
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
        drawCylinder();
    }

    public static Render Builder() {
        return INSTANS;
    }


}
