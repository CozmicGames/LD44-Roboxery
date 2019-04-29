package de.cozmic

import de.cozmic.scene.World
import de.cozmic.scene.tiles.*

enum class Pattern(
    val layout: Array<IntArray>,
    val size: Int,
    val playerX: Int,
    val playerY: Int,
    val getJobManager: () -> JobManager
) {
    SQUARE_6(arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1),
        intArrayOf(7, 0, 0, 0, 6, 1),
        intArrayOf(1, 0, 0, 0, 0, 1),
        intArrayOf(1, 2, 0, 0, 0, 1),
        intArrayOf(1, 2, 2, 0, 3, 1),
        intArrayOf(1, 1, 1, 1, 1, 1)
    ), 6, 1, 1, { JobManager(5.0f, 20.0f, 0.5f, 0.25f, 0.5f) }),
    SQUARE_8(arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(7, 0, 0, 0, 0, 0, 2, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 2, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 6, 2, 0, 0, 3, 0, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1)
    ), 8, 6, 2, { JobManager(5.0f, 30.0f, 0.5f, 0.15f, 0.3f) }),
    SQUARE_12(arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 1),
        intArrayOf(1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0, 1),
        intArrayOf(7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 2, 0, 0, 0, 2, 2, 0, 6, 0, 0, 1),
        intArrayOf(1, 2, 0, 0, 0, 2, 2, 0, 0, 0, 0, 1),
        intArrayOf(1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    ), 12, 3, 3, { JobManager(10.0f, 30.0f, 0.5f, 0.1f, 0.4f) });

    fun generate(world: World, x: Int, y: Int) = when (layout[y][x]) {
        1 -> WallTile(world, x, y)
        2 -> ScaffoldTile(world, x, y)
        3 -> CounterTile(world, x, y)
        5 -> CrateTile(world, x, y)
        6 -> CrateGeneratorTile(world, x, y)
        7 -> TankTile(world, x, y)
        else -> BackgroundTile(world, x, y)
    }
}