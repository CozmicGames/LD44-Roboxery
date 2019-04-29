package de.cozmic.scene.tiles

import de.cozmic.Game
import de.cozmic.scene.Tile
import de.cozmic.scene.World

class BackgroundTile(world: World, x: Int, y: Int) : Tile(world, x, y, Game.assets.textures.findRegion("background")) {
    override val isCollidable = false
}