package de.cozmic

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.*
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer
import de.cozmic.graphics.Graphics
import de.cozmic.graphics.Renderer
import de.cozmic.states.InitialState
import java.util.*

class Game : ApplicationAdapter() {
    companion object {
        var time = 0.0f
            private set

        var worldDrawSize = 32.0f

        lateinit var renderer: Renderer
            private set

        lateinit var batch: SpriteBatch
            private set

        lateinit var assets: Assets
            private set

        var state: State = InitialState()
            private set

        val random = Random()
    }

    private var createState = true

    override fun create() {
        renderer = Renderer()
        batch = SpriteBatch()
        assets = Assets()
    }

    override fun render() {
        val clearColor = Color(0x22232b00)

        Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
        Gdx.gl.glEnable(GL_BLEND)
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT)

        time += Gdx.graphics.deltaTime

        if (createState) {
            state.create()
            createState = false
        }

        val newState = state.render()
        if (newState != state) {
            state.dispose()
            state = newState
            createState = true
        }
    }

    override fun pause() {
        state.pause()
    }

    override fun resume() {
        state.resume()
    }

    override fun dispose() {
        state.dispose()

        assets.dispose()
        renderer.dispose()
        batch.dispose()
    }
}