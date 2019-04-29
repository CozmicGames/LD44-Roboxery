package de.cozmic

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Disposable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Assets : Disposable {
    private val manager = AssetManager()

    private inner class AssetDelegate<T : Any>(private val filename: String, cls: KClass<T>, param: AssetLoaderParameters<T>? = null) {
        init {
            manager.load(filename, cls.java, param)
        }

        operator fun getValue(thisRef: Any, property: KProperty<*>): T = this@Assets[filename]
    }

    /*
    Assets
     */

    val font: BitmapFont by asset("font.fnt")
    val textures: TextureAtlas by asset("textures.atlas")
    val move: Sound by asset("move.wav")
    val interact: Sound by asset("interact.wav")
    val falseInteract: Sound by asset("falseinteract.wav")
    val energyTransfer: Sound by asset("energytransfer.wav")
    val getEnergy: Sound by asset("getenergy.wav")
    val createBox: Sound by asset("createbox.wav")
    val breakBox: Sound by asset("breakbox.wav")
    val sellBox: Sound by asset("sellbox.wav")
    val lost: Sound by asset("lost.wav")
    val won: Sound by asset("won.wav")


    /*

     */

    private inline fun <reified T : Any> asset(filename: String, param: AssetLoaderParameters<T>? = null) = AssetDelegate(filename, T::class, param)

    operator fun <T> get(filename: String): T = manager[filename]

    fun update(delta: Float) = manager.update((delta * 1000).toInt())

    override fun dispose() {
        manager.dispose()
    }
}