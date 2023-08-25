package com.trans.opengl_es_show;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Tom灿
 * @description: 视图容器 需要扩展此类才能捕获触摸事件
 * @date :2023/8/18 15:17
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private MyGLRenderer renderer;

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Render the view only when there is a change in the drawing data 仅在绘图数据发生变化时才呈现视图
        //  To allow the triangle to rotate automatically, this line is commented out: 为了让三角形自动旋转，这一行被注释掉了(启动连续渲染):
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;
    private float previousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }

                renderer.setAngle(
                        renderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
        }

        previousX = x;
        previousY = y;
        return true;
    }

}
