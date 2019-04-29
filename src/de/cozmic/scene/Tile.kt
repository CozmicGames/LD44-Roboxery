package de.cozmic.scene

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game

abstract class Tile(val world: World, x: Int, y: Int, val texture: TextureRegion) : WorldObject(x * Game.worldDrawSize, y * Game.worldDrawSize) {
    var previousTile: Tile? = null

    open val isCollidable = true

    open val isSelectable = false

    open fun onCollision(entity: Entity) = isCollidable

    open fun onInteraction(entity: Entity) {}

    override fun render() {
        //Game.batch.draw(texture, rect.x, rect.y, Game.worldDrawSize, Game.worldDrawSize)
        Game.renderer.draw(texture, rect.x, rect.y, Game.worldDrawSize, Game.worldDrawSize)
    }

    override fun read(data: DataInput) {
        if (data.readBoolean()) {
            val x = data.readInt(true)
            val y = data.readInt(true)
            val type = TileType[data.readString()]!!
            val tile = type.create(world, x, y)
            tile.read(data)
            previousTile = tile
        }
    }

    override fun write(data: DataOutput) {
        data.writeBoolean(previousTile != null)
        if (previousTile != null) {
            data.writeInt((previousTile!!.rect.x / Game.worldDrawSize).toInt(), true)
            data.writeInt((previousTile!!.rect.y / Game.worldDrawSize).toInt(), true)
            data.writeString(previousTile!!::class.java.name)
            previousTile!!.write(data)
        }
    }
}