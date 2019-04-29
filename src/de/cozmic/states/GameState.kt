package de.cozmic.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import de.cozmic.Game
import de.cozmic.Pattern
import de.cozmic.State
import de.cozmic.graphics.GUI
import de.cozmic.graphics.Graphics
import de.cozmic.scene.TileType
import de.cozmic.scene.World
import de.cozmic.scene.tiles.*
import de.cozmic.use
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sign

class GameState(val pattern: Pattern, val file: String) : State {
    private val camera = OrthographicCamera()
    private val uiCamera = OrthographicCamera()

    private val battery = Game.assets.textures.findRegion("battery")
    private val batteryCharge = Game.assets.textures.findRegion("batteryCharge")
    private val pin = Game.assets.textures.findRegion("pin")
    private val jobNote = Game.assets.textures.findRegion("jobNote")
    private lateinit var world: World

    override fun create() {
        TileType.register(BackgroundTile::class)
        TileType.register(WallTile::class)
        TileType.register(CounterTile::class)
        TileType.register(ScaffoldTile::class)
        TileType.register(CrateTile::class)
        TileType.register(CrateGeneratorTile::class)
        TileType.register(TankTile::class)

        world = World(pattern, file)

        camera.setToOrtho(true)
        uiCamera.setToOrtho(true)

        val totalWorldSize = world.pattern.size * Game.worldDrawSize
        val worldX = (Gdx.graphics.width - totalWorldSize) * 0.5f
        val worldY = (Gdx.graphics.height - totalWorldSize) * 0.5f

        camera.translate(-worldX, -worldY)
        camera.update()
    }

    override fun render(): State {
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP))
            world.player.moveY(false)
        else if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN))
            world.player.moveY(true)
        else if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            world.player.moveX(false)
        else if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            world.player.moveX(true)

        val distFromCenterX = world.player.center.x - (world.pattern.size * Game.worldDrawSize * 0.5f)
        val distFromCenterY = world.player.center.y - (world.pattern.size * Game.worldDrawSize * 0.5f)

        val cameraX = (world.pattern.size * Game.worldDrawSize * 0.5f) + min(abs(distFromCenterX), 32.0f) * sign(distFromCenterX)
        val cameraY = (world.pattern.size * Game.worldDrawSize * 0.5f) + min(abs(distFromCenterY), 32.0f) * sign(distFromCenterY)

        camera.position.x -= (camera.position.x - cameraX) * 0.05f
        camera.position.y -= (camera.position.y - cameraY) * 0.05f

        camera.update()

        Game.renderer.render(camera) {
            world.render()
            Graphics.drawParticles()
        }

        Game.renderer.render(uiCamera) {
            it.draw(battery, 120.0f, 8.0f, battery.regionWidth.toFloat(), battery.regionHeight.toFloat())
            val width = ceil(world.player.charge * 110.0f)
            it.draw(batteryCharge, 144.0f, 20.0f, width, 35.0f, color = if (world.player.charge < 0.25f) Color.RED else Color.WHITE)
        }

        Game.batch.use {
            world.jobs.forEachIndexed { index, job ->
                val x = Gdx.graphics.width - 202.0f
                val y = 10.0f + index * 74.0f

                it.draw(jobNote, x, y, jobNote.regionWidth.toFloat(), jobNote.regionHeight.toFloat())

                val font = Game.assets.font
                font.draw(it, "Charge: ${(job.price * 100).toInt()}%", x + 16.0f, y + 30.0f)
                font.draw(it, "Time:   ${(job.remainingTime / 60).toInt()}:${job.remainingTime.toInt() % 60}", x + 16.0f, y + 53.0f)

                if (job == world.currentJob)
                    it.draw(pin, x + 164.0f, y + 48.0f, 32.0f, 32.0f)
            }
        }

        if (world.failed)
            return LostState(file)

        if (world.solved)
            return WonState(file)

        if (GUI.button(uiCamera, "Menu", 10.0f, 10.0f, 80.0f, 50.0f))
            return MenuState()

        return this
    }

    override fun dispose() {
        world.save()
        Graphics.clearParticles()
    }
}