package de.cozmic

interface State {
    fun create() {}
    fun render(): State = this
    fun pause() {}
    fun resume() {}
    fun dispose() {}
}