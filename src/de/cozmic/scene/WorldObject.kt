package de.cozmic.scene

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game

abstract class WorldObject(x: Float, y: Float) {
    val rect = Rectangle(x, y, Game.worldDrawSize, Game.worldDrawSize)
    val center = Vector2()
        get() = rect.getCenter(field)

    abstract fun render()

    open fun read(data: DataInput) {}
    open fun write(data: DataOutput) {}
}