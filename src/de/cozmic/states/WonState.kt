package de.cozmic.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils.clamp
import de.cozmic.Game
import de.cozmic.State
import de.cozmic.graphics.GUI
import de.cozmic.use

class WonState(val file: String) : State {
    private val won = Game.assets.textures.findRegion("won")
    private val camera = OrthographicCamera()
    private val interpolation = Interpolation.fade
    private var counter = 0.0f

    override fun create() {
        camera.setToOrtho(true)

        Gdx.files.local(file).apply {
            if (exists())
                delete()
        }

        Game.assets.won.play()
    }

    override fun render(): State {
        counter += Gdx.graphics.deltaTime
        counter = clamp(counter, 0.0f, 1.0f)

        val scale = interpolation.apply(counter)

        Game.batch.use {
            val w = won.regionWidth * scale
            val h = won.regionHeight * scale
            val x = (Gdx.graphics.width - w) * 0.5f
            val y = (Gdx.graphics.height - h) * 0.5f
            it.draw(won, x, y, w, h)
        }

        if (scale == 1.0f) {
            if (GUI.button(camera, "Menu", 325.0f, 500.0f, 150.0f, 50.0f))
                return MenuState()
        }

        return this
    }
}