package de.cozmic.scene.items

import de.cozmic.Game
import de.cozmic.scene.Item
import de.cozmic.scene.Player
import de.cozmic.scene.World

class BatteryItem(world: World, x: Float, y: Float, var charge: Float) : Item(world, x, y, Game.assets.textures.findRegion("batteryItem")) {
    init {
        rect.width = Game.worldDrawSize * 0.5f
        rect.height = Game.worldDrawSize * 0.75f
    }

    override fun onCollection(player: Player) {
        Game.assets.getEnergy.play()
        player.charge += charge
        moveOutOfView()
    }
}