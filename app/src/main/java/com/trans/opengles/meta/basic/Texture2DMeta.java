package com.trans.opengles.meta.basic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.nio.ShortBuffer;

/**
 * @author Tom灿
 * @description: 绘制Texture纹理2D
 * @date :2023/11/8 15:01
 */
public class Texture2DMeta implements Render {
    private static final Render INSTANS = new Texture2DMeta();
    private static final String TAG = "Texture2DMeta";

    private FloatBuffer vertexBuffer, mTexVertexBuffer;

    private ShortBuffer mVertexIndexBuffer;

    //纹理id
    private int textureId;

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];

    //返回属性变量的位置
    //变换矩阵
    private int uMatrixLocation;
    //顶点
    private int aPositionLocation;

    //纹理
    private int aTextureLocation;

    /**
     * 顶点坐标
     * (x,y,z)
     */
    private static final float[] POSITION_VERTEX = new float[]{
            0f, 0f, 0f,     //顶点坐标V0
            1f, 1f, 0f,     //顶点坐标V1
            -1f, 1f, 0f,    //顶点坐标V2
            -1f, -1f, 0f,   //顶点坐标V3
            1f, -1f, 0f     //顶点坐标V4
    };

    /**
     * 纹理坐标
     * (s,t)
     */
    private static final float[] TEX_VERTEX = {
            0.5f, 0.5f, //纹理坐标V0
            1f, 0f,     //纹理坐标V1
            0f, 0f,     //纹理坐标V2
            0f, 1.0f,   //纹理坐标V3
            1f, 1.0f    //纹理坐标V4
    };

    /**
     * 绘制顺序索引
     */
    private static final short[] VERTEX_INDEX = {
            0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
            0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
            0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
            0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
    };

    //图片生成的位图
    private Bitmap mBitmap;
    private int vertexTextureShaderId;
    private int fragmentTextureShaderId;

    public Texture2DMeta() {
    }

    private void init() {
        initMemory();
        initShader();
        initProgram();
        textureId = loadTexture(R.mipmap.wangfei);
    }


    /**
     * 绘制纹理
     */
    private void drawTexture2D() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        //将变换矩阵传入顶点渲染器
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
        //启用顶点坐标属性
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        GLES30.glVertexAttribPointer(aPositionLocation, 3,
                GLES30.GL_FLOAT, false, 0, vertexBuffer);
        //启用纹理坐标属性
        GLES30.glEnableVertexAttribArray(aTextureLocation);
        GLES30.glVertexAttribPointer(aTextureLocation, 2,
                GLES30.GL_FLOAT, false, 0, mTexVertexBuffer);

        /**
         * glActiveTexture选择后续纹理状态调用将影响的纹理单元。一个实现支持的纹理单元数取决于实现，但必须至少为32。
         *
         * 参数-textre：指定要激活的纹理单元。纹理单元的数量取决于实现，但必须至少为32。texture必须是GL_TEXTUREi中的一个，
         * 其中i的取值范围是从0到GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS的值减1。初始值为GL_TEXTUREO。
         */
        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); // 选择活动纹理单元
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        // 绘制
        GLES30.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aTextureLocation);
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
     * 加载纹理
     *
     * @param resourceId
     * @return
     */
    private int loadTexture(int resourceId) {
        final int[] textureIds = new int[1];
        /**
         * glGenTextures返回n个纹理名称。不能保证这些名称形成一个连续的整数集;
         * 但是，可以保证在调用glGenTextures之前没有一个返回的名称在使用。调用glGenTextures返回的纹理名称不会被后续调用返回，
         * 除非它们首先被删除glDeleteTextures。
         * 纹理中返回的名称被标记为使用，仅用于glGenTextures，但只有当它们第一次使用glBindTexture绑定时，它们才获得状态和维度。
         *
         * 参数1-n：指定要生成的纹理名称的数量。
         * 参数2-texture：指定存储生成的纹理名称的数组。
         * 参数3-offset：偏移量
         */
        //创建一个纹理对象
        GLES30.glGenTextures(1, textureIds, 0); // 生成纹理名称
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL textureId object.");
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //这里需要加载原图未经缩放的数据
        options.inScaled = false;
        mBitmap = BitmapFactory.decodeResource(MyApplication.application.getResources(),
                resourceId, options);
        if (mBitmap == null) {
            Log.e(TAG, "Resource ID " + resourceId + " could not be decoded.");
            /**
             *
             * glDeleteTextures删除n个以数组纹理元素命名的纹理。
             * 删除纹理后，它没有内容或维度，其名称也不再使用。如果当前绑定的纹理被删除，则绑定将恢复为0(默认纹理)。
             * 纹理中未使用的名称被标记为用于glGenTextures的目的，再次被标记为未使用。
             * glDeleteTextures会静默忽略与现有纹理不对应的0和名称。
             *
             * 参数1-n：指定要删除的纹理数量。
             * 参数2-texture：指定要删除的纹理数组。
             */
            GLES30.glDeleteTextures(1, textureIds, 0); // 删除命名纹理
            return 0;
        }

        /**
         *  glBindTexture将名称为texture的纹理对象绑定到target指定的纹理目标。
         *  调用glBindTexture，目标设置为GL TEXTURE 2D,
         *                            GL TEXTURE 3D,
         *                            GL TEXTURE 2D ARRAY，
         *                           或GL TEXTURE CUBE MAP，
         *  纹理设置为新纹理的名称，将纹理名称绑定到该目标。当纹理绑定到目标时，该目标的先前绑定将自动中断。
         *
         *  纹理名称是无符号整数。保留0值来表示每个纹理目标的默认纹理。纹理名称和相应的纹理内容是当前GL渲染上下文的共享对象空间的局部;
         *  只有当两个渲染上下文通过适当的GL窗口接口函数显式启用上下文之间的共享时，它们才共享纹理名称。
         *
         *  你必须使用glGenTextures来生成一组新的纹理名称。
         *
         *  当一个纹理第一次被绑定时，它假定指定的目标:一个纹理第一次绑定到GL texture 2D成为二维纹理，
         *  一个纹理第一次绑定到GL_TEXTURE_3D成为三维纹理，一个纹理第一次绑定到GL texture 2D ARRAY成为二维任意纹理，
         *  一个纹理第一次绑定到GL texture CUBE MAP成为所有立方体映射纹理。
         *  二维纹理在第一次绑定后的状态与GL初始化时默认的GL_TEXTURE_2D的状态相同，其他纹理类型也类似。
         *
         *  当纹理被绑定时，GL对被绑定目标的操作会影响被绑定的纹理，而对被绑定目标的查询会返回被绑定纹理的状态。
         *  实际上，纹理目标成为当前绑定到它们的纹理的别名，纹理名称0指的是初始化时绑定到它们的默认纹理。
         *
         *  使用glBindTexture创建的纹理绑定保持活动状态，直到不同的纹理绑定到相同的目标，
         *  或者直到绑定的纹理被alDeleteTextures删除。
         *
         *  一旦创建，命名纹理可以根据需要随时重新绑定到相同的原始目标。
         *  使用glBindTexture将现有的命名纹理绑定到纹理目标之一通常比使用glTexImage2D,
         *  glTexImage3D或其他类似函数重新加载纹理图像要快得多。
         *
         * 纹理绑定受状态GL_ACTIVE_TExTuRE设置的影响(参见glActiveTexture)。一个纹理对象可以同时绑定到多个纹理单元。
         *
         * 参数1-target：指定纹理绑定到的目标。
         *              必须是 GL_TEXTURE_2D,
         *                    GL_TEXTURE_3D,
         *                    GL_TEXTURE_2D_ARRAY
         *                  或GL_TEXTURE_CUBE_MAP，
         * 参数2-texture：指定纹理的名称。
         */
        //绑定纹理到OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]); // 将命名纹理绑定到纹理目标

        /**
         * giTexParameter将params中的一个或多个值分配给指定为pname的纹理参数。
         * target定义目标纹理，可以是GL_TEXTURE_2D、
         *                       GL_TEXTURE_CUBE_MAP、
         *                       GL_TEXTURE_2D_ARRAY
         *                      或GL_TEXTURE_3D。
         * pname中接受以下符号:(略 https://registry.khronos.org/OpenGL-Refpages/es3.0/)
         *
         * 参数1-target：指定目标纹理，必须是GL_TEXTURE_2D、
         *                              GL_TEXTURE_3D、
         *                              GL_TEXTURE_2D_ARRAY
         *                              或GL_TEXTURE_CUBE_MAP。
         * 参数2-pname :指定单值纹理参数的符号名称。Pname可以是以下参数之一:GL_TEXTURE_BASE_LEVEL,
         *                                                  GL_TEXTURE_COMPARE_FUNC, GL_TEXTURE_COMPARE_MODE,
         *                                                  GL_TEXTURE_MIN_FILTER, GL_TEXTURE MAG_FILTER,
         *                                                  GL_TEXTURE_MIN_LOD,GL_TEXTURE_MAX_LOD,
         *                                                  GL_TEXTURE_MAX_LEVEL, GL_TEXTURE_SWIZZLE_R,
         *                                                  GL_TEXTURE_SWIZZLE_G, GL_TEXTURE_SWIZZLE_B,
         *                                                  GL_TEXTURE_SWIZZLE_A,GL_TEXTURE_WRAP_S,
         *                                                  GL_TEXTURE_WRAP_T，或GL纹理WRAP R。
         * 参数3-param：指定pname的值。
         * 参数4-params：对于vector命令，指定指向存储pname值的数组的指针。
         */
        //设置默认的纹理过滤参数
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_LINEAR_MIPMAP_LINEAR); // 设置纹理参数
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        /**
         * 纹理允许着色器读取图像数组的元素。
         *
         * 要定义纹理图像，调用glTexImage2D。
         * 参数描述了纹理图像的参数，如高度、宽度、边框宽度、细节级别(参见glTexParameter)和提供的颜色组件的数量。
         * 最后三个参数描述了图像如何在内存中表示。
         *
         * 如果目标是GL_TEXTURE_2D或GL_TEXTURE_CUBE_MAP目标之一，
         * 则根据类型，从数据中读取数据作为有符号字节或无符号字节、短字节或长字节或单精度浮点值的序列。
         * 根据格式，将这些值分组为一个、两个、三个或四个值的集合，以形成元素。
         *
         * 如果在指定纹理图像时将非零命名缓冲区对象绑定到GL_PIXEL_UNPACK_BuFFER目标(参见glBindBuffer)，
         * 则数据将被视为缓冲区对象数据存储中的字节偏移量。
         *
         * 第一个元素对应于纹理图像的左下角。后续元素从左到右依次遍历纹理图像中最低行的剩余纹理，然后依次遍历纹理图像中较高的行。
         * 最后一个元素对应于纹理图像的右上角。
         *
         * 参数1-target：目标指定目标纹理。必须为GL_TEXTURE_2D、
         *                          GL_TEXTURE_CUBE_MAP_POsITIVE_X、
         *                          GL_TEXTURE_CUBE_MAP_NEGATIVE_X、
         *                          GL_TEXTURE_CUBE_MAP_POSITIVE_Y、
         *                          GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
         *                          或GL_TEXTURE_CUBE_MAP_NEGATIVE_Z。
         * 参数2-level：突出的数字。O级是base image。n级是nth mipmap减减图像。
         * 参数3-bitmap：Bitmap对象
         * 参数4-border：该值必须为0
         */
        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0); // 指定二维纹理图像

        /**
         * glGenerateMipmap为附加到活动纹理单元目标的纹理生成mipmap。
         * 对于立方体贴图纹理，如果附加到目标上的纹理不是立方体完整的，会产生GL INVALID OPERATION错误。
         *
         * 无论如何，Mipmap生成将文本数组levelbase+1到q替换为从levelbase数组派生的数组他们以前的内容。
         * 所有其他mimap数组，包括levelbase数组，在此计算中保持不变。
         *
         * 派生的mipmap数组的内部格式都与levelbase数组的内部格式匹配。
         * 派生数组的内容是通过对levelbase数组进行重复的、过滤的缩减来计算的。对于二维纹理数组，每一层都是独立过滤的。
         *
         * 参数-target：指定要生成mimaps的纹理绑定到的目标。target必须为GL_TEXTURE_2D，GL纹理3D, GL纹理2D数组或GL纹理立方体贴图。
         */
        //生成MIP贴图
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D); // 为指定的纹理目标生成mipmaps

        //数据如果已经被加载进OpenGL,则可以回收该bitmap
        mBitmap.recycle();

        //取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }


    /**
     * 连接着色器
     */
    private void initProgram() {
        // 连接着色器程序
        int mProgram = linkProgram(vertexTextureShaderId, fragmentTextureShaderId);

        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        //将背景设置为白色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        //将背景设置为黑色
//        GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

        // 获取相关属性变量的句柄
        uMatrixLocation = GLES30.glGetUniformLocation(mProgram, "u_Matrix");  // 矩阵属性变量句柄
        aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition"); // 顶点属性变量句柄
        aTextureLocation = GLES30.glGetAttribLocation(mProgram, "aTextureCoord");
    }

    /**
     * 创建加载、编译着色器程序
     */
    private void initShader() {
        // TODO 编译顶点着色器：定义了矩阵属性变量
        vertexTextureShaderId = compileShader(GLES30.GL_VERTEX_SHADER,
                ResReadUtils.readResource(R.raw.vertex_texture_shader));
        // 编译片段着色器
        fragmentTextureShaderId = compileShader(GLES30.GL_FRAGMENT_SHADER,
                ResReadUtils.readResource(R.raw.fragment_texture_shader));
    }

    /**
     * 分配内存空间
     */
    private void initMemory() {
        //顶点位置相关
        //分配本地内存空间,每个浮点型占4字节空间；将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(POSITION_VERTEX);
        vertexBuffer.position(0);

        mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX);
        mTexVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);

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
        drawTexture2D();
    }

    public static Render Builder() {
        return INSTANS;
    }


}
