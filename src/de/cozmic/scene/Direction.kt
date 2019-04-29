package de.cozmic.scene

enum class Direction(val x: Float, val y: Float) {
    NONE(0.0f, 0.0f),
    NORTH(0.0f, -1.0f),
    SOUTH(0.0f, 1.0f),
    WEST(-1.0f, 0.0f),
    EAST(1.0f, 0.0f)
}