package de.cozmic.scene.tiles

import de.cozmic.Game
import de.cozmic.scene.Entity
import de.cozmic.scene.Player
import de.cozmic.scene.Tile
import de.cozmic.scene.World
import kotlin.math.ceil

class TankTile(world: World, x: Int, y: Int) : Tile(world, x, y, Game.assets.textures.findRegion("tank")) {
    private val tankCharge = Game.assets.textures.findRegion("tankCharge")
    private var charge = 0.0f

    override fun render() {
        super.render()

        val scale = Game.worldDrawSize / texture.regionWidth
        val height = ceil(charge * 20.0f)
        Game.renderer.draw(tankCharge, rect.x + 8.0f * scale, rect.y + (5.0f + 20.0f - height) * scale, 16.0f * scale, height * scale)
    }

    override fun onInteraction(entity: Entity) {
        if (entity !is Player)
            return

        entity.charge -= 0.1f
        if (entity.charge <= 0.0f) {
            world.failed = true
            return
        }

        Game.assets.energyTransfer.play()

        charge += 0.1f

        if (charge >= 1.0f)
            world.solved = true
    }
}