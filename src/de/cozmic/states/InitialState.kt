package de.cozmic.states

import com.badlogic.gdx.Gdx
import de.cozmic.Game
import de.cozmic.State

class InitialState : State {
    override fun render(): State {
        return if (Game.assets.update(Gdx.graphics.deltaTime)) MenuState() else this
    }
}