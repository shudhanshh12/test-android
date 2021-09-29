package tech.okcredit.android.base.extensions

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader

fun Bitmap.getRoundedBitmap(): Bitmap? {
    val circleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val shader = BitmapShader(this, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val paint = Paint()
    paint.shader = shader
    paint.isAntiAlias = true
    val c = Canvas(circleBitmap)
    c.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), paint)
    return circleBitmap
}
