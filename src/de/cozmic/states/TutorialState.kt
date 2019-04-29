package de.cozmic.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import de.cozmic.Game
import de.cozmic.State
import de.cozmic.graphics.GUI

class TutorialState(val state: State) : State {
    private lateinit var textures: Array<TextureRegion>
    private var index = 0

    private val camera = OrthographicCamera()

    override fun create() {
        camera.setToOrtho(true)
        textures = arrayOf(TextureRegion(Texture("tutorial0.png")), TextureRegion(Texture("tutorial1.png")))
    }

    override fun render(): State {
        Game.renderer.render(camera) {
            it.draw(textures[index], 0.0f, 0.0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        }

        if (index == 0) {
            if (GUI.button(camera, "Next", 250.0f, 520.0f, 300.0f, 50.0f))
                index++
        } else {
            if (GUI.button(camera, "Close Tutorial", 250.0f, 520.0f, 300.0f, 50.0f))
                return state
        }

        return this
    }

    override fun dispose() {
        textures.forEach { it.texture.dispose() }
    }
}