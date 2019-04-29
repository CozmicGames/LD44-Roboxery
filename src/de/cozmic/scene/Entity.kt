package de.cozmic.scene

import de.cozmic.Game

abstract class Entity(val world: World, x: Float, y: Float, size: Float = Game.worldDrawSize) : WorldObject(x, y) {
    init {
        rect.width = size
        rect.height = size
    }
}