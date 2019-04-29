package de.cozmic.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import de.cozmic.Game
import de.cozmic.State
import de.cozmic.graphics.GUI
import de.cozmic.use

class MenuState : State {
    private val title = Game.assets.textures.findRegion("title")
    private val camera = OrthographicCamera()
    private val interpolation = Interpolation.fade
    private var counter = 0.0f

    override fun create() {
        camera.setToOrtho(true)
    }

    override fun render(): State {
        counter += Gdx.graphics.deltaTime
        counter = MathUtils.clamp(counter, 0.0f, 1.0f)

        val scale = interpolation.apply(counter)

        Game.batch.use {
            val w = title.regionWidth * scale
            val h = title.regionHeight * scale
            val x = (Gdx.graphics.width - w) * 0.5f
            val y = 330.0f
            it.draw(title, x, y, w, h)
        }

        if (scale == 1.0f) {
            if (GUI.button(camera, "Play", 300.0f, 330.0f, 200.0f, 50.0f))
                return SavesState()

            if (GUI.button(camera, "Tutorial", 300.0f, 410.0f, 200.0f, 50.0f))
                return TutorialState(this)

            if (GUI.button(camera, "Quit", 300.0f, 480.0f, 200.0f, 50.0f))
                Gdx.app.exit()
        }

        return this
    }
}