package de.cozmic.scene.tiles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game
import de.cozmic.graphics.Graphics
import de.cozmic.scene.Tile
import de.cozmic.scene.World

class CrateGeneratorTile(world: World, x: Int, y: Int) : Tile(world, x, y, Game.assets.textures.findRegion("crateGenerator")) {
    companion object {
        private val overlay = Game.assets.textures.findRegion("overlay")
        private val overlayColor = Color(0x8CCEC7FF.toInt())
    }

    override val isCollidable = false

    private var spawnTime = 0.0f

    override fun render() {
        super.render()
        Game.renderer.drawRotated(overlay, rect.x, rect.y, Game.worldDrawSize, Game.worldDrawSize, Game.time * 50.0f, color = overlayColor)

        if (world.player.collisionRect.overlaps(rect))
            return

        val job = world.jobs.find { !it.isCreated } ?: return
        spawnTime += Gdx.graphics.deltaTime
        job.remainingTime = job.prepareTime - spawnTime

        if (spawnTime >= job.prepareTime) {
            spawnTime = 0.0f

            val x = (rect.x / Game.worldDrawSize).toInt()
            val y = (rect.y / Game.worldDrawSize).toInt()

            val crate = CrateTile(world, x, y)
            crate.previousTile = this
            world.setTile(x, y, crate)

            Graphics.addSmoke(rect.x + Game.worldDrawSize * 0.5f, rect.y + Game.worldDrawSize * 0.5f)
            Game.assets.createBox.play()
        }
    }

    override fun read(data: DataInput) {
        super.read(data)
        spawnTime = data.readFloat()
    }

    override fun write(data: DataOutput) {
        super.write(data)
        data.writeFloat(spawnTime)
    }
}