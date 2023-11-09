package com.trans.opengles.meta.basic;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.trans.opengles.surface.Render;
import com.trans.opengles.surface.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author Tom灿
 * @description: 定义形状：一个三角形 (图元/图元装配)
 * @date :2023/8/18 15:40
 */
public class TriangleMeta implements Render {
    private static final Render INSTANS = new TriangleMeta();
    private FloatBuffer vertexBuffer;

    // 顶点坐标
    private float triangleCoords[] = {   // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };   // 顶点尚未针对显示 GLSurfaceView 的屏幕区域的比例进行校正


    // 顶点着色器-源代码
    private final String vertexShaderCode =       // OpenGL 着色语言 (GLSL) 代码
//            "attribute vec4 vPosition;" +  //TODO 未进行使用矩阵投影和相机转换
            "uniform mat4 uMVPMatrix;" +    //TODO 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "attribute vec4 vPosition;" +   //TODO 将矩阵变体添加到着色程序，以此应用矩阵投影和相机转换
                    "void main() {" +
//                    "  gl_Position = vPosition;" +       //TODO 未进行使用矩阵投影和相机转换

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

    private int mProgram;

    public TriangleMeta() {

    }


    private int positionHandle;
    private int colorHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;   // 每个顶点的坐标数

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex 每个顶点四个字节

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    private void init() {
        /**
         * 初始化坐标：
         * 把需要绘制的顶点坐标放入缓冲区
         */
        // initialize vertex byte buffer for shape coordinates  // 初始化顶点字节缓冲区的形状坐标
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order0
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer  // 从ByteBuffer创建一个浮点缓冲区
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate  // 将缓冲区设置为读取第一个坐标
        vertexBuffer.position(0);


        /**
         * 编译并加载着色器程序：
         * 要绘制形状，必须编译着色程序代码，将它们添加到 OpenGL ES 程序对象中，然后关联该程序。
         * 该操作需要在绘制对象的构造函数中完成，因此只需执行一次
         */
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,  // 顶点着色器,可在可编程顶点处理器上运行
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, // 片段着色器,可在可编程片段处理器上运行
                fragmentShaderCode);

        /**
         * 创建空的OpenGL ES程序对象, 返回一个非零值，该值可以被引用
         * 该程序对象可以被着色器对象附加,指定将被链接到创建程序的着色器对象
         *
         * glCreateProgram创建一个空的程序对象并返回一个非零值，该值可以被引用。
         * 程序对象是着色器对象可以附加到的对象。这提供了一种机制来指定将被链接到创建程序的着色器对象。
         * 它还提供了一种方法来检查将用于创建程序的着色器的兼容性(例如，检查顶点着色器和片段着色器之间的兼容性)。
         * 当不再需要作为程序对象的一部分时，着色器对象可以分离。
         * 通过使用glAttachShader成功地将着色器对象附加到程序对象中，使用glCompileShader成功地编译着色器对象，
         * 并使用glLinkProgram成功地链接程序对象，可以在程序对象中创建一个或多个可执行文件。当调用glUseProgram时，
         * 这些可执行文件成为当前状态的一部分。程序对象可以通过调用glDeleteProgram来删除。
         * 当与程序对象关联的内存不再是任何上下文的当前呈现状态的一部分时，它将被删除。
         */
        // create empty OpenGL ES Program  创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram(); // 可以附加着色器的GLES程序对象


        /**
         *  描述为了创建可执行文件，必须有一种方法来指定将被链接在一起的内容列表。程序对象提供了这种机制。
         *  要在程序对象中链接在一起的着色器必须首先附加到该程序对象上。
         *  glAttachShader将shader指定的shader对象附加到program指定的program对象上。
         *  这表明着色器将包含在将在程序上执行的链接操作中。所有可以在着色器对象上执行的操作都是有效的，
         *  无论着色器对象是否附加到程序对象上。在源代码被加载到shader对象或shader对象被编译之前，
         *  将shader对象附加到程序对象是允许的。相同类型的多个着色器对象不能附加到单个程序对象上。
         *  然而，单个着色器对象可以附加到多个程序对象。
         *  如果一个着色器对象在附加到程序对象时被删除，它将被标记为删除，
         *  并且直到调用glDetachShader将其从附加的所有程序对象中分离出来才会发生删除。
         *
         *  参数1-program:指定将附加着色器对象的程序对象。
         *  参数2-shader:指定要附加的着色器对象。
         */
        // add the vertex shader to program  添加顶点着色器到程序
        GLES20.glAttachShader(mProgram, vertexShader);  // 将顶点着色器对象附加到程序对象中,要想被GLES程序连接到,必须先添加到GLES程序中

        // add the fragment shader to program  添加片段着色器到程序
        GLES20.glAttachShader(mProgram, fragmentShader); // 将片段着色器对象附加到程序对象中,要想被GLES程序连接到,必须先添加到GLES程序中

        /**
         *  链接由程序指定的程序对象。
         * 一个GL VERTEx_SHADER类型的着色器对象被附加到程序中创建可在可编程顶点处理器上运行的可执行文件。
         * 一个类型为GL_FRACMENT_SHADER的着色器对象附加到程序是用来创建一个可执行文件，将运行在可编程片段处理器。
         * 链接操作的状态将作为程序对象状态的一部分存储。该值将被设置为cL_TRuE程序对象的链接没有错误，
         * 可以使用，否则返回GL_FaLsE。可以通过调用查询参数为program和GL_LINK_STATUS。
         * 链接操作成功后，将初始化属于program的所有活动用户定义的统一变量到0，
         * 并且每个程序对象的活动统一变量将被分配一个位置，该位置可以通过调用glGetUniformLocation。
         * 此外，任何未绑定到通用顶点的活动用户定义属性变量此时属性索引将绑定到1。
         * 程序对象的链接可能由于OpenGL ES着色语言中指定的许多原因而失败规范。下面列出了一些会导致链接错误的条件。
         *
         * 参数-program: 指定要链接的程序对象的句柄。
         */
        // creates OpenGL ES program executables  创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);  // 链接着色器程序对象.可以在程序对象中创建一个或多个可执行文件
    }

    /**
     * 创建用于绘制形状的 draw() 方法。
     * 此代码将位置和颜色值设置为形状的顶点着色程序和 Fragment 着色程序，然后执行绘制功能
     */
    private void drawFrame() {
        /**
         * glUseProgram将program指定的程序对象安装为当前呈现状态的一部分。
         * glUseProgram将程序对象指定为程序实例化，作为当前生成状态的一部分。
         * 通过使用glAttachShader成功地将着色器对象附加到程序对象中，使用glCompileShader成功地编译着色器对象，
         * 并使用glLinkProgram成功地链接程序对象，可以在程序对象中创建每个阶段的可执行文件。
         * 如果一个程序对象包含一个类型为GL_VERTEX_SHADER的着色器对象
         * 和一个类型为GL_FRACMENT_SHADER的着色器对象已经成功编译和链接，
         * 那么它将包含将在顶点和片段处理器上运行的可执行文件。
         * 当一个程序对象在使用时，应用程序可以自由地修改附加的着色器对象，
         * 编译附加的着色器对象，附加着色器对象，以及分离或删除着色器对象。这些操作都不会影响作为当前状态一部分的可执行文件。
         * 但是，如果链接操作成功，重新链接当前正在使用的程序对象将把程序对象安装为当前呈现状态的一部分(参见glLinkProgram)。
         * 如果当前正在使用的程序对象链接不成功，它的链接状态将被设置为cL FaLsE，但是可执行文件和相关状态将保持当前状态的一部分，
         * 直到后续调用glUseProgram将其从使用中移除。在它从使用中移除后，它不能成为当前状态的一部分，直到它被成功地重新链接。
         * 如果program为0，则当前渲染状态指的是无效的程序对象，
         * 并且由于任何gIDrawArrays或gIDrawElements命令而执行顶点和片段着色器的结果是未定义的。
         *
         * 参数-program:程序指定程序对象的句柄，该程序对象的可执行文件将被用作当前呈现状态的一部分。
         */
        // Add program to OpenGL ES environment 添加程序到OpenGL ES环境
        GLES20.glUseProgram(mProgram); // 将程序对象安装为当前呈现状态的一部分

        /**
         * glGetAttriblocation查询由程序指定的先前链接的程序对象，以获取由名称指定的属性变量，
         * 并返回绑定到该属性变量的通用顶点属性的索引。如果name是矩阵属性变量，则返回矩阵第一列的索引。
         * 如果指定的属性变量不是指定程序对象中的活动属性，或者name以保留前缀“gl_”开头，则返回值为-1。
         * 属性变量名和通用属性索引之间的关联可以在任何时候通过调用glBindAttribLocation来指定。
         * 属性绑定在调用glLinkProgram之前不会生效。成功链接程序对象之后，属性变量的索引值保持固定，直到下一次链接命令出现。
         * 只有链路成功后才能查询该属性值。glGetAttribLocation返回上次为指定程序对象调用glLinkProgram时实际生效的绑定。
         * 自上次链接操作以来指定的属性绑定不会由glGetAttribLocation返回。
         *
         * 参数1-program:指定要查询的程序对象。
         * 参数2-name:指向一个以空结尾的字符串，该字符串包含要查询其位置的属性变量的名称。
         *
         * 返回值:返回属性变量的位置 (返回指定程序指定属性的位置)
         *
         * Note: int(Long) ==> 句柄 ==> index索引 ==> 指针的指针 ==> id ==> 位置
         */
        // get handle to vertex shader's vPosition member  获取顶点着色器的vPosition成员的句柄
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        /**
         * glEnableVertexAttribArray启用由索引指定的通用顶点属性数组。
         * glDisableVertexAttribArray禁用由index指定的通用顶点属性数组。
         * 默认情况下，
         * 禁用所有客户端功能，包括所有通用顶点属性数组。
         * 如果启用，在调用顶点数组命令(如glDrawArrays或glDrawElements)时，
         * 将访问和使用通用顶点属性数组中的值进行渲染。
         *
         * 参数-index:指定要启用或禁用的通用顶点属性的索引。
         */
        // Enable a handle to the triangle vertices  启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(positionHandle); // 启用或禁用通用顶点属性数组  (通过索引句柄来启用对应属性变量)


        /**
         * glVertexAttribPointer指定在索引索引处通用顶点属性数组的位置和数据格式，以便在渲染时使用。
         * Size指定每个属性的组件数量，必须为1、2、3或4。
         * Type指定每个组件的数据类型，stride指定从一个属性到下一个属性的字节跨度，
         * 从而允许将顶点和属性打包到单个数组中或存储在单独的数组中。
         * 如果设置为GL_TRUE，则normalized表示存储在整数格式中的值在被访问并转换为浮点数时将被映射到[-1,1](对于有符号值)或[0,1](对于无符号值)的范围。
         * 否则，值将直接转换为浮点数而不进行规范化。如果一个非零命名的缓冲区对象被绑定到GL_ARRAYBUFFER目标(参见glBindBuffer)，
         * 而一个通用的顶点属性数组被指定，指针被视为一个字节偏移到缓冲区对象的数据存储。
         * 此外，缓冲区对象绑定(GL_ARRAY_BUFFER_BINDING)被保存为indexindex的通用顶点属性数组客户端状态(GLVERTEX_ATTRIB_ARRAY_BUFFER_BINDING)。
         * 当指定通用顶点属性数组时，除了当前顶点数组缓冲区对象绑定之外，还将大小、类型、规范化、步距和指针保存为客户端状态。
         * 要启用和禁用通用顶点属性数组，请调用glEnableVertexAttribArray和glDisableVertexAttribArray。
         * 如果启用，则在调用glDrawArrays或glDrawElements时使用通用顶点属性数组。
         *
         * 参数1-index:指定要修改的通用顶点属性的索引。
         * 参数2-size:指定每个通用顶点属性的组件数。必须是1、2、3或4。初始值为4。
         * 参数3-type:指定数组中每个组件的数据类型。
         *      符号常量GL BYTE, GL_UNSIGNED_BYTE, GL_SHORT，GL_UNSIGNED_SHORT、GL_FIXED
         *      或GL_FLOAT可以接受。初始值为GL_FLOAT。
         * 参数4-normalized:指定定点数据值在访问时是应该规范化(GL_TRUE)还是直接转换为定点值(GL_FALSE)。
         * 参数5-stride:指定连续通用顶点属性之间的字节偏移量。如果stride为0，则将通用顶点属性理解为紧密地封装在数组中。初始值为0。
         * 参数6-pointer:指定指向数组中第一个通用顶点属性的第一个组件的指针。初始值为0。
         */
        // Prepare the triangle coordinate data  准备三角坐标数据
        GLES20.glVertexAttribPointer(
                positionHandle, // 句柄
                COORDS_PER_VERTEX, // 每个顶点的坐标数
                GLES20.GL_FLOAT,   // 数据类glEnableVertexAttribArray型
                false,
                vertexStride,
                vertexBuffer);  // 定义一个通用顶点属性数据数组

        /**
         * glGetUniformLocation返回一个整数，表示程序对象中特定统一变量的位置。名称必须是一个以空结尾的字符串，不包含空格。
         * 在程序中，Name必须是一个活动的统一变量名，而不是结构体、结构体数组或向量或矩阵的子组件。
         * 如果name与程序中的活动统一变量不对应，或者name以保留前缀“gl_”开头，则返回-1。
         * 可以通过对结构中的每个字段调用glGetUniformLocation来查询作为结构或结构数组的统一变量。
         * 数组元素操作符“”和结构字段操作符“。”可以在name中使用，以便选择数组中的元素或结构中的字段。
         * 使用这些操作符的结果不允许是另一个结构体、结构体的数组、向量或矩阵的子组件。
         * 除非name的最后一部分表示的是一个统一变量数组，
         * 否则可以通过使用数组的名称或使用添加“[0]”的名称来检索数组的第一个元素的位置。
         * 分配给统一变量的实际位置在程序对象成功链接之前是不知道的。
         * 链接完成后，可以使用命令glGetUniformLocation获取统一变量的位置。
         * 然后，这个位置值可以传递给glUniform来设置统一变量的值，或者传递给glGetUniform来查询统一变量的当前值。
         * 成功链接程序对象后，统一变量的索引值保持固定，直到下一次链接命令出现。
         * 只有在链接成功后才能查询统一变量的位置和值。
         *
         *  参数1-program:指定要查询的程序对象。
         *  参数2-name:指向一个以空结尾的字符串，该字符串包含要查询其位置的统一变量的名称。
         *
         * 返回值:返回统一变量的位置
         */
        // get handle to fragment shader's vColor member  获取片段着色器的vColor成员的句柄
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        /**
         * glUniform用于修改统一变量或统一变量数组的值。
         * 要修改的统一变量的位置由Iocation指定，它应该是由glGetUniformLocation返回的一个值。
         * 通过调用glUseProgram, glUniform对程序对象进行操作，该程序对象已成为当前状态的一部分。
         * 命令glUniformf1 2l3l4) ffli)用于使用作为参数传递的值更改由location指定的统一变量的值。
         * 命令中指定的数量应与指定的统一变量的数据类型中的组件数量相匹配(例如，float, int, bool为1;2为vec2, ivec2, bvec2等)。
         * 后缀f表示正在传递浮点值;后缀I表示传递的是整数值，该类型也应该与指定的统一变量的数据类型匹配。
         * 该函数的i个变体应该用于为定义为int、ivec2、ivec3、ivec4或这些变量的数组的统一变量提供值。
         * 应该使用f变量为float、vec2、vec3、vec4等类型的统一变量或这些类型的数组提供值。
         * i或f变量可用于为bool类型、bvec2、bvec3、bvec4或这些类型的数组的统一变量提供值。
         * 如果输入值为0或0，则统一变量将被设置为false。的，否则将设置为true。
         *
         * 参数1-location:指定要修改的统一值的位置。
         * 参数2-count:指定要修改的元素的数目。如果目标统一变量不是数组，则应该是1，如果是数组，则应该是1或更多。
         * 参数3-value:指定指向计数值数组的指针，该数组将用于更新指定的统一变量。
         * 参数4:(OpenGL® ES es2.0 C规范中没有此参数)
         */
        // Set color for drawing the triangle  设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0); // 为当前程序对象指定统一变量的值

        /**
         * glDrawArrays通过很少的子例程调用指定了多个几何原语。而不是调用GL过程来传递每个单独的顶点属性，
         * 你可以使用glVertexAttribPointer来预先指定单独的顶点、法线和颜色数组，
         * 并使用它们来构建一个原语序列，只需调用glDrawArrays。
         * 当调用giDrawArrays时，它将从每个启用的数组中使用count顺序元素来构造一个几何原语序列，从元素开始。
         * Mode指定构造什么类型的原语以及数组元素如何构造这些原语。
         * 要启用和禁用通用顶点属性数组，请调用glEnableVertexAttribArray和glDisableVertexAttribArray。
         *
         * 参数1-mode:指定要呈现的原语类型。
         *           符号常量GL_PoINTS, GLLINE_STRIP, GL_LINE_LOOP, GL_LINES,
         *           GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN和gl_triangle可以接受。
         * 参数2-first:指定启用的数组中的起始索引。
         * 参数3-count:指定要呈现的索引数。
         */
        // Draw the triangle  画出三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount); // 从数组数据渲染原语

        /**
         * glEnableVertexAttribArray启用由索引指定的通用顶点属性数组。
         * glDisableVertexAttribArray禁用由index指定的通用顶点属性数组。
         * 默认情况下，禁用所有客户端功能，包括所有通用顶点属性数组。
         * 如果启用，在调用顶点数组命令(如glDrawArrays或glDrawElements)时，将访问和使用通用顶点属性数组中的值进行渲染。
         *
         * 参数-index:指定要启用或禁用的通用顶点属性的索引
         */
        // Disable vertex array  禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle);    // 启用或禁用通用顶点属性数组
    }


    /**
     * 创建用于绘制形状的 draw() 方法。
     * 此代码将位置和颜色值设置为形状的顶点着色程序和 Fragment 着色程序，然后执行绘制功能
     * 应用了相机视图
     */
    private void drawMatrix() {
        setCameraView();
        drawFrame(vPMatrix);
    }

    // Use to access and set the view transformation 用于访问和设置视图转换
    private int vPMatrixHandle;

    /**
     * 创建用于绘制形状的 draw() 方法。
     * 此代码将位置和颜色值设置为形状的顶点着色程序和 Fragment 着色程序，然后执行绘制功能
     *
     * @param mvpMatrix 通过计算的变换矩阵
     */
    private void drawFrame(float[] mvpMatrix) { // pass in the calculated transformation matrix

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

        /**
         * glUniform*在默认的uniform块中修改uniform变量或uniform变量数组的值。
         * l要修改的统一变量的位置由location指定，location应该是由glGetUniformLocation返回的值。
         * glUniform对程序对象进行操作，该对象由调用 glUseProgram。
         *
         * 命令glUniform{1121314}{flilui}用于使用作为参数传递的值更改由location指定的uniform变量的值。
         * 命令中指定的数量应与指定的统一变量的数据类型中的组件数量相匹配(例如，float, int, unsigned int,
         * bool为1;2为vec2, ivec2, uvec2, bvec2等)。
         * 后缀f表示传递的是浮点值，后缀i表示传递的是整数值;后缀UI表示传递的是无符号整数值，
         * 该类型还应该与指定的统一变量的数据类型匹配。
         * 该函数的i个变体应该用于为定义为int、ivec2、ivec3、ivec4或这些变量的数组的统一变量提供值。
         * 该函数的ui变体应该用于为定义为unsigned int、uvec2、uvec3、uvec4或其数组的统一变量提供值。
         * 应该使用f变量为float、vec2、vec3、vec4等类型的统一变量或这些类型的数组提供值。
         * i、ui或f变量都可用于为bool、bvec2、bvec3、bvec4类型的统一变量或这些类型的数组提供值。
         * 如果输入值为0或O. of，则统一变量将被设置为false，否则将被设置为true。
         *
         * 当程序对象被成功链接时，程序对象中定义的所有活动统一变量都初始化为0。
         * 它们保留调用glUniform时赋给它们的值，直到下一个成功的链接操作发生在程序对象上，此时它们再次初始化为0。
         *
         *
         * 命令glUniform{1121314}{flilui}v可用于修改单个统一变量或统一变量数组。
         * 这些命令传递一个计数和指向要加载到统一变量或统一变量数组中的值的指针。
         * 如果修改单个均匀变量的值，应该使用1计数，如果修改整个数组或部分数组，可以使用1或更大的计数。
         * 当在一个均匀变量数组中从任意位置m开始加载n个元素时，数组中的元素m + n- 1将被替换为新的值。
         * 如果m + n-1大于均匀变量数组的大小，则忽略数组末尾以外的所有数组元素的值。
         * 命令名中指定的数字表示value中每个元素的组件数量，
         * 并且它应该与指定的统一变量的数据类型中的组件数量相匹配(例如，float, int, bool为1;2为vec2, ivec2, bvec2等)。
         * 命令名称中指定的数据类型必须与前面描述的glUniform{1|2|314}{flilui}中指定的统一变量的数据类型匹配。
         *
         * 对于均匀变量数组，
         * 数组的每个元素都被认为是名称中所指示的类型命令(例如，glUniform3f或glUniform3fv)可用于加载类型为vec3)。
         * 要修改的均匀变量数组的元素数量由count指定
         *
         * glUniformMatrix{2|3|4|2x3|3x2|2x4|4x2|3x4|4x3}fv命令用于修改矩阵或矩阵数组。
         * 命令名中的数字被解释为矩阵的维数。数字2表示2 × 2矩阵(即4个值)，数字3表示3 × 3矩阵(即9个值)，
         * 数字4表示4 × 4矩阵(即16个值)。非方阵维度是显式的，第一个数字表示列数，第二个数字表示行数。
         * 例如，2x4表示一个2x4矩阵，有2列4行(即8个值)。如果转置为GL FALSE，则假定每个矩阵按列主顺序提供。
         * 如果转置为GL TRUE，则假定每个矩阵按行主序提供。count参数指示要传递的矩阵的数量。
         * 如果修改单个矩阵的值，应该使用1的计数，而大于1的计数可以用于修改矩阵数组。
         *
         *  参数1-location：指定要修改的统一变量的位置。数对于vector (glUniform*v)命令，
         *  参数2-count：指定要修改的元素的数量。如果目标统一变量不是数组，则应该是1，如果是数组，则应该是1或更多。
         *              对于matrix (glUniformMatrix*)命令，指定要修改的矩阵个数。
         *              如果目标统一变量不是矩阵数组，则应该为1，如果是矩阵数组，则应该为1或更多。转置对于矩阵命令，
         *  参数3-transpose：指定在将值加载到统一变量中时是否对矩阵进行转置。VO、VL、V2、V3对于标量命令，
         *  参数4-v0, v1, v2, v3：指定要用于指定的统一变量的新值。价值对于vector和matrix命令，
         *  参数5-value：指定指向计数值数组的指针，该数组将用于更新指定的统一变量。
         */
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
    private void setMatrix(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        /**
         *   float[] m, //接收透视投影的变换矩阵
         *   int mOffset, //变换矩阵的起始位置（偏移量）
         *   float left,//相对观察点近面的左边距
         *   float right,//相对观察点近面的右边距
         *   float bottom, //相对观察点近面的下边距
         *   float top, //相对观察点近面的上边距
         *   float near, //相对观察点近面距离 （近裁剪平面）
         *   float far  //相对观察点远面距离  （远裁剪平面）
         */
        // this projection matrix is applied to object coordinates  这个投影矩阵应用于物体坐标
        // in the onDrawFrame() method  在onDrawFrame()方法中
        // 用六个剪辑平面定义一个投影矩阵
        Matrix.frustumM(projectionMatrix,  // 填充了一个投影矩阵 projectionMatrix
                0, -ratio, ratio, -1, 1, 3, 7);
    }


    // vPMatrix is an abbreviation for "Model View Projection Matrix"  vPMatrix是“模型视图投影矩阵”的缩写。
    private final float[] vPMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    /**
     * 定义相机视图
     */
    private void setCameraView() {
        /**
         * 参数1-rm:接收相机变换矩阵
         * 参数2-rmOffset:变换矩阵的起始位置（偏移量）
         * 参数3-eyeX:相机位置
         * 参数4-eyeY:相机位置
         * 参数5-eyeZ:相机位置
         * 参数6-centerX:观测点位置（指定被观察物的位置：（相机位置点指向被观察物位置点，组成一个向量，这个就是相机的拍摄方向））
         * 参数7-centerY:观测点位置
         * 参数8-centerZ:观测点位置
         * 参数9-upX:up向量在xyz上的分量 （相机上方向。（我们可以想象成原始相机上面那个闪光灯的朝向））
         * 参数10-upY:up向量在xyz上的分量
         * 参数11-upZ:up向量在xyz上的分量
         */
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


        /**
         *  “变换矩阵”
         *  float[] result,    //接收相乘结果
         *  int resultOffset,  //接收矩阵的起始位置（偏移量）
         *  float[] lhs,       //左矩阵
         *  int lhsOffset,     //左矩阵的起始位置（偏移量）
         *  float[] rhs,       //右矩阵
         *  int rhsOffset     //右矩阵的起始位置（偏移量）
         */
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
    private void drawAnimation() {
        setCameraView();
        animationModel();
        drawFrame(scratch);
    }

    /**
     * 动画
     * 创建一个转换矩阵（旋转矩阵），然后将其与投影和相机视图转换矩阵相结合
     * 启用了连续渲染
     */
    private void animationModel() {
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
    private void drawOnTouchRotate(float mAngle) {
        setCameraView();
        rotateModel(mAngle);
        drawFrame(scratch);
    }


    /**
     * 响应onTouch事件 （模型的手势监听）
     * （需要启用连续渲染）
     */
    private void rotateModel(float mAngle) {

        // Create a rotation for the triangle
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);

        // Draw triangle
        drawFrame(scratch);
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
//        drawFrame(); // 绘制：三角形
//        drawMatrix();  // 绘制： 应用了投影和相机视图
        drawAnimation(); // 绘制：应用了投影和相机视图以及旋转矩阵
//        drawOnTouchRotate(-mAngle);// 绘制：应用了投影和相机视图以及旋转矩阵,并响应手势
    }

    public static Render Builder() {
        return INSTANS;
    }


}

