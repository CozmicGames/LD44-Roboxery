package de.cozmic.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import de.cozmic.Game
import de.cozmic.use

object GUI {
    val backgroundColor = Color(0x357266FF)
    val borderColor = Color(0x152D29FF)
    val shadowColor = Color(Color.BLACK)
    val borderSize = 4.0f
    val shadowOffset = 10.0f

    val rectangle = Rectangle()

    fun button(camera: OrthographicCamera, text: String, x: Float, y: Float, width: Float, height: Float): Boolean {
        rectangle.x = x
        rectangle.y = y
        rectangle.width = width
        rectangle.height = height

        val over = isOver(camera)

        val color = Color()

        val scale = if (over && Gdx.input.isTouched)
            0.95f
        else if (over)
            1.05f
        else
            1.0f

        Game.renderer.render(camera) {
            val ww = rectangle.width * scale
            val hh = rectangle.height * scale
            val xx = rectangle.x + (rectangle.width - ww) * 0.5f
            val yy = rectangle.y + (rectangle.height - hh) * 0.5f

            it.addPathFilled(shadowColor) {
                roundedRect(xx + shadowOffset, yy + shadowOffset, ww, hh, 5.0f)
            }

            color.set(backgroundColor)
            color.mul(if (over && Gdx.input.isTouched)
                0.9f
            else if (over)
                1.1f
            else
                1.0f)

            it.addPathFilled(color) {
                roundedRect(xx, yy, ww, hh, 5.0f)
            }

            color.set(borderColor)
            color.mul(if (over && Gdx.input.isTouched)
                0.9f
            else if (over)
                1.1f
            else
                1.0f)

            it.addPathStroke(borderColor, borderSize) {
                roundedRect(xx, yy, ww, hh, 5.0f)
            }
        }

        val font = Game.assets.font
        val layout = font.cache.setText(text, 0.0f, 0.0f)

        Game.batch.use {
            font.draw(it, text, x + (width - layout.width) * 0.5f, Gdx.graphics.height - (y + (height - layout.height) * 0.5f))
        }

        return over && Gdx.input.justTouched()
    }

    private fun isOver(camera: OrthographicCamera): Boolean {
        val touchPos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0.0f)
        camera.unproject(touchPos)
        return rectangle.contains(touchPos.x, touchPos.y)
    }
}