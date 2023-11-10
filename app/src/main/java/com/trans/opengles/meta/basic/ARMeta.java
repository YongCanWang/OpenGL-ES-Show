package com.trans.opengles.meta.basic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.trans.opengles.MyApplication;
import com.trans.opengles.R;
import com.trans.opengles.surface.Render;
import com.trans.opengles.utils.ResReadUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * @author Tom灿
 * @description: AR图
 * @date :2023/11/8 9:34
 */
public class ARMeta implements Render {
    private static final Render INSTANS = new ARMeta();
    private static final int BYTES_PER_FLOAT = 4;
    private static final String TAG = "ARMeta";
    //顶点位置缓存
    private FloatBuffer vertexBuffer;
    //纹理顶点位置缓存
    private FloatBuffer mTexVertexBuffer;

    //图片生成的位图
    private Bitmap mBitmap;
    //纹理id
    private int textureId;

    //向量个数
    private int vCount;

    //相关属性id
    private int mHProjMatrix;
    private int mHViewMatrix;
    private int mHModelMatrix;
    private int mHRotateMatrix;
    private int mHUTexture;
    private int mHPosition;
    private int mHCoordinate;


    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];

    private final float[] mModelMatrix = new float[16];

    private ArrayList<Float> alVertix;
    private ArrayList<Float> textureVertix;

    public ARMeta() {
        initSensor();
    }


    private void init() {
        // 分配内存空间
        initMemory();

        // 编译着色器程序
        // 编译顶点着色器
        int vertexShaderId = compileShader(GLES30.GL_VERTEX_SHADER,
                ResReadUtils.readResource(R.raw.vertex_globe_shader));
        // 编译片段着色器
        int fragmentShaderId = compileShader(GLES30.GL_FRAGMENT_SHADER,
                ResReadUtils.readResource(R.raw.fragment_globe_shader));

        // 连接着色器程序
        int mProgram = linkProgram(vertexShaderId, fragmentShaderId);

        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        //将背景设置为灰色
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        // 获取相关属性变量的句柄
        //编译glprogram并获取控制句柄
        mHProjMatrix = GLES20.glGetUniformLocation(mProgram, "uProjMatrix");
        mHViewMatrix = GLES20.glGetUniformLocation(mProgram, "uViewMatrix");
        mHModelMatrix = GLES20.glGetUniformLocation(mProgram, "uModelMatrix");
        mHUTexture = GLES20.glGetUniformLocation(mProgram, "uTexture");
        mHPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mHCoordinate = GLES20.glGetAttribLocation(mProgram, "aCoordinate");
        mHRotateMatrix = GLES30.glGetUniformLocation(mProgram, "uRotateMatrix");
    }

    /**
     * 绘制球体: 应用了变换矩阵（添加了相机视图和透视投影）
     */
    private void drawGlobe() {

        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUniformMatrix4fv(mHProjMatrix, 1, false, mProjectMatrix, 0);
        GLES30.glUniformMatrix4fv(mHViewMatrix, 1, false, mViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mHModelMatrix, 1, false, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mHRotateMatrix, 1, false, uRotateMatrix, 0);

        GLES30.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES30.glEnableVertexAttribArray(mHPosition);
        GLES30.glVertexAttribPointer(mHPosition, 3, GLES20.GL_FLOAT,
                false, 0, vertexBuffer);

        GLES30.glEnableVertexAttribArray(mHCoordinate);
        GLES30.glVertexAttribPointer(mHCoordinate, 2, GLES20.GL_FLOAT,
                false, 0, mTexVertexBuffer);

        GLES30.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        GLES30.glDisableVertexAttribArray(mHCoordinate);
        GLES30.glDisableVertexAttribArray(mHPosition);
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
        //透视投影矩阵/视锥
//        Matrix.perspectiveM(mProjectMatrix, 0, 60, ratio, 1f, 300f);
        Matrix.perspectiveM(mProjectMatrix,0,90,ratio,0f,300f);
        //设置相机位置
//        Matrix.setLookAtM(mViewMatrix, 0, 0f, 4f, 2f, 0.0f, 0.0f, 0f, 0f, 0f, 1f);
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0.0f,0.0f, 0.0f, 0.0f,1.0f, 0f,1.0f, 0.0f);
        //模型矩阵
        Matrix.setIdentityM(mModelMatrix, 0);

        //Matrix.rotateM(mViewMatrix,0,180,0,0,1);

        // 旋转矩阵
        Matrix.setIdentityM(uRotateMatrix, 0);

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
        calculateAttribute();
        //顶点位置相关
        //分配本地内存空间,每个浮点型占4字节空间；将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = convertToFloatBuffer(alVertix);
        mTexVertexBuffer = convertToFloatBuffer(textureVertix);
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
     * 加载纹理图片
     *
     * @param resourceId
     * @return
     */
    private int loadTexture(int resourceId) {
        final int[] textureIds = new int[1];
        //创建一个纹理对象
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.");
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //这里需要加载原图未经缩放的数据
        options.inScaled = false;
        mBitmap = BitmapFactory.decodeResource(MyApplication.application.getResources(), resourceId, options);
        if (mBitmap == null) {
            Log.e(TAG, "Resource ID " + resourceId + " could not be decoded.");
            GLES30.glDeleteTextures(1, textureIds, 0);
            return 0;
        }
        //绑定纹理到OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);

        //设置默认的纹理过滤参数
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0);

        //生成MIP贴图
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        //数据如果已经被加载进OpenGL,则可以回收该bitmap
        mBitmap.recycle();

        //取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }


    //计算顶点坐标和纹理坐标
    private void calculateAttribute() {
        float radius = 1.0f; // 球的半径
        double angleSpan = Math.PI / 90f; // 将球进行单位切分的角度
        alVertix = new ArrayList<>();
        textureVertix = new ArrayList<>();
        for (double vAngle = 0; vAngle < Math.PI; vAngle = vAngle + angleSpan) {

            for (double hAngle = 0; hAngle < 2 * Math.PI; hAngle = hAngle + angleSpan) {
                float x0 = (float) (radius * Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius * Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius * Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius * Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));


                float s0 = (float) (-hAngle / Math.PI / 2);
                float s1 = (float) (-(hAngle + angleSpan) / Math.PI / 2);

                float t0 = (float) (vAngle / Math.PI);
                float t1 = (float) ((vAngle + angleSpan) / Math.PI);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x0);
                alVertix.add(y0);
                alVertix.add(z0);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x0 y0对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);
                textureVertix.add(s1);// x2 y3对应纹理坐标
                textureVertix.add(t1);
            }
        }
        vCount = alVertix.size() / 3;
    }

    //动态数组转FloatBuffer
    private FloatBuffer convertToFloatBuffer(ArrayList<Float> data) {
        float[] d = new float[data.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = data.get(i);
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(data.size() * 4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer ret = buffer.asFloatBuffer();
        ret.put(d);
        ret.position(0);
        return ret;
    }

    @Override
    public void shader() {
        init();
        textureId = loadTexture(R.mipmap.ar);
    }

    @Override
    public void view(int width, int height) {
        transMatrix(width, height);
    }


    @Override
    public void draw() {
        drawGlobe();
    }

    public static Render Builder() {
        return INSTANS;
    }


    /****************************** 支持旋转 *************************************/
    private final float[] mRotateMatrix = new float[16];
    //根据传感器变化的矩阵
    private float[] uRotateMatrix = new float[16];
    private Sensor mRotation;

    private void initSensor() {
        //传感器获取并监听相关数据
        SensorManager sensorManager = (SensorManager) MyApplication.application
                .getSystemService(Context.SENSOR_SERVICE);
        //List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        mRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                setRotateMatrix(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, mRotation, SensorManager.SENSOR_DELAY_GAME);
    }

    private void setRotateMatrix(SensorEvent event) {
        //Log.e("setRotateMatrix","setRotateMatrix");
        SensorManager.getRotationMatrixFromVector(mRotateMatrix, event.values);
        //Log.e("setuRotateMatrix","setuRotateMatrix");
        this.uRotateMatrix = mRotateMatrix;
    }

}
