package com.trans.opengles.surface

/**
 * @author Tom灿
 * @description:
 * @date :2023/11/9 14:25
 */
interface Render {
    fun shader()
    fun view(width: Int, height: Int)
    fun draw()
}