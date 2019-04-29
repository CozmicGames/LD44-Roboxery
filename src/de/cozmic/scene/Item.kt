package de.cozmic.scene

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game
import kotlin.math.sin

abstract class Item(world: World, x: Float, y: Float, val texture: TextureRegion) : Entity(world, x, y) {
    var isActive = false

    override fun render() {
        if (!isActive)
            return

        val yOffset = sin(Game.time * 2.0f) * 0.05f * Game.worldDrawSize

        //Game.batch.draw(texture, rect.x, rect.y + yOffset, rect.width, rect.height)
        Game.renderer.draw(texture, rect.x, rect.y + yOffset, rect.width, rect.height)
    }

    abstract fun onCollection(player: Player)

    fun moveOutOfView() {
        rect.x = 10000.0f
    }

    override fun read(data: DataInput) {
        isActive = data.readBoolean()
        rect.x = data.readFloat()
        rect.y = data.readFloat()
    }

    override fun write(data: DataOutput) {
        data.writeBoolean(isActive)
        data.writeFloat(rect.x)
        data.writeFloat(rect.y)
    }
}