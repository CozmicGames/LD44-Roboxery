package de.cozmic

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import kotlin.math.abs

fun SpriteBatch.use(block: (SpriteBatch) -> Unit) {
    begin()
    enableBlending()
    block(this)
    disableBlending()
    end()
}

fun Rectangle.getStretched(x: Float = 0.0f, y: Float = 0.0f): Rectangle {
    var minX = this.x
    var minY = this.y
    var maxX = this.x + width
    var maxY = this.y + height

    if (x < 0.0f)
        minX += x
    else
        maxX += x

    if (y < 0.0f)
        minY += y
    else
        maxY += y

    val width = maxX - minX
    val height = maxY - minY

    return Rectangle(minX, minY, width, height)
}

fun Rectangle.resolveCollisionX(other: Rectangle, amount: Float): Float {
    if (amount == 0.0f)
        return amount

    val newAmount = if (amount > 0.0f)
        other.x - (x + width)
    else
        other.x + other.width - x

    return if (abs(newAmount) > abs(amount))
        amount
    else
        newAmount
}

fun Rectangle.resolveCollisionY(other: Rectangle, amount: Float): Float {
    if (amount == 0.0f)
        return amount

    val newAmount = if (amount > 0.0f)
        other.y - (y + height)
    else
        other.y + other.height - y

    return if (abs(newAmount) > abs(amount))
        amount
    else
        newAmount
}