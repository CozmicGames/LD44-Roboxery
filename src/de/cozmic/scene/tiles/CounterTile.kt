package de.cozmic.scene.tiles

import com.badlogic.gdx.graphics.Color
import de.cozmic.Game
import de.cozmic.scene.Tile
import de.cozmic.scene.World

class CounterTile(world: World, x: Int, y: Int) : Tile(world, x, y, Game.assets.textures.findRegion("counter")) {
    companion object {
        val overlay = Game.assets.textures.findRegion("overlay")
        private val overlayColor = Color(0xFF220CFF.toInt())
    }

    override val isCollidable = false

    override fun render() {
        super.render()
        Game.renderer.drawRotated(overlay, rect.x, rect.y, Game.worldDrawSize, Game.worldDrawSize, Game.time * 50.0f + 185.0f, color = overlayColor)
    }
}