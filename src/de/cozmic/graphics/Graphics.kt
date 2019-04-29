package de.cozmic.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import de.cozmic.Game

object Graphics {
    private class Particle(var x: Float, var y: Float, var dx: Float, var dy: Float, var life: Float) {
        var time = 0.0f
    }

    private val smoke = Game.assets.textures.findRegion("smoke")
    private val particles = arrayListOf<Particle>()

    fun drawParticles() {
        val color = Color()

        with(particles.iterator()) {
            while (hasNext()) {
                val particle = next()
                val delta = Gdx.graphics.deltaTime
                particle.time += delta
                if (particle.time >= particle.life)
                    remove()

                particle.x += particle.dx * delta * 20.0f
                particle.y += particle.dy * delta * 20.0f

                color.a = particle.time / particle.life
                Game.renderer.draw(smoke, particle.x, particle.y, smoke.regionWidth.toFloat(), smoke.regionHeight.toFloat())
            }
        }
    }

    fun clearParticles() {
        particles.clear()
    }

    fun addSmoke(x: Float, y: Float) {
        repeat(25) {
            val px = (Game.random.nextFloat() - 0.5f) * Game.worldDrawSize
            val py = (Game.random.nextFloat() - 0.5f) * Game.worldDrawSize
            val dx = (Game.random.nextFloat() - 0.5f) * 2.0f
            val dy = (Game.random.nextFloat() - 0.5f) * 2.0f
            particles += Particle(x + px, y + py, dx, dy, 0.5f + Game.random.nextFloat() * 0.5f)
        }
    }
}