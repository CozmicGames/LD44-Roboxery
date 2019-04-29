package de.cozmic.scene.tiles

import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game
import de.cozmic.graphics.Graphics
import de.cozmic.scene.Tile
import de.cozmic.scene.TileType
import de.cozmic.scene.World
import java.util.*

class CrateTile(world: World, x: Int, y: Int) : Tile(world, x, y, Game.assets.textures.findRegion("crate")) {
    companion object {
        private val damages = Array(4) {
            Game.assets.textures.findRegion("damage$it")
        }
    }

    override val isSelectable = true

    var damage = 0

    override fun render() {
        previousTile?.let {
            Game.renderer.draw(it.texture, rect.x, rect.y, Game.worldDrawSize, Game.worldDrawSize)
        }
        super.render()
        drawDamage(rect.x, rect.y)
    }

    fun drawDamage(x: Float, y: Float) {
        //Game.batch.draw(damages[damage], x, y, rect.width, rect.height)
        Game.renderer.draw(damages[damage], x, y, rect.width, rect.height)
    }

    fun destroy(x: Float, y: Float) {
        Game.assets.breakBox.play()

        Graphics.addSmoke(x + Game.worldDrawSize * 0.5f, y + Game.worldDrawSize * 0.5f)

        if (Game.random.nextInt(10) > 7) {
            val item = world.getNextItem() ?: return

            item.rect.x = x + (Game.worldDrawSize - item.rect.width) * 0.5f
            item.rect.y = y + (Game.worldDrawSize - item.rect.height) * 0.5f
            item.isActive = true
        } else {
            val tileX = (x / Game.worldDrawSize).toInt()
            val tileY = (y / Game.worldDrawSize).toInt()

            val oldTime = world.getTile(tileX, tileY)
            val tile = if (Game.random.nextInt(10) > 5)
                CrateTile(world, tileX, tileY)
            else
                ScaffoldTile(world, tileX, tileY)
            tile.previousTile = oldTime
            world.setTile(tileX, tileY, tile)
        }
    }

    override fun read(data: DataInput) {
        super.read(data)
        damage = data.readInt(true)
    }

    override fun write(data: DataOutput) {
        super.write(data)
        data.writeInt(damage, true)
    }
}