package de.cozmic

import com.badlogic.gdx.Gdx
import java.util.*

class JobManager(val minTime: Float, val maxTime: Float, val timeStep: Float, val minPrice: Float, val maxPrice: Float) {
    private var nextGenerateTime = 0.0f
    private var generateTime = 0.0f

    fun generateJob() = Job(minTime + Game.random.nextFloat() * ((maxTime - minTime) / timeStep) * timeStep, minPrice + Game.random.nextFloat() * (maxPrice - minPrice))

    fun update(): Job? {
        generateTime += Gdx.graphics.deltaTime
        if (generateTime >= nextGenerateTime) {
            generateTime = 0.0f
            nextGenerateTime = 5.0f + Game.random.nextFloat() * 5.0f
            return generateJob()
        }

        return null
    }
}