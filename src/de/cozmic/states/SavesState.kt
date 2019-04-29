package de.cozmic.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game
import de.cozmic.Pattern
import de.cozmic.State
import de.cozmic.graphics.GUI
import de.cozmic.use

class SavesState : State {
    inner class SaveSlot(val pattern: Pattern, val index: Int) {
        fun startGame() = GameState(pattern, "save$index.bin")
    }

    private val camera = OrthographicCamera()
    private val slots = arrayOfNulls<SaveSlot>(3)

    override fun create() {
        camera.setToOrtho(true)

        val data = Gdx.files.local("data.bin")
        if (data.exists()) {
            data.read().use {
                val input = DataInput(it)

                repeat(slots.size) { index ->
                    if (input.readBoolean()) {
                        val pattern = Pattern.values()[input.readInt(true)]
                        if (Gdx.files.local("save$index.bin").exists())
                            slots[index] = SaveSlot(pattern, index)
                    }
                }
            }
        }
    }

    override fun render(): State {
        repeat(slots.size) { index ->
            val x = 150.0f
            val y = 100.0f + index * 120.0f
            val width = 500.0f
            val height = 100.0f
            Game.renderer.render(camera) {
                it.addPathFilled(Color.BLACK) {
                    roundedRect(x + 10.0f, y + 10.0f, width, height, 5.0f)
                }

                it.addPathFilled(Color(0xFFF3B0FF.toInt())) {
                    roundedRect(x, y, width, height, 5.0f)
                }

                it.addPathStroke(Color(0xE09F3EFF.toInt())) {
                    roundedRect(x, y, width, height, 5.0f)
                }
            }

            var slot = slots[index]

            if (slot == null) {
                val font = Game.assets.font

                Game.batch.use {
                    font.draw(it, "Empty", x + 10.0f, Gdx.graphics.height - (y + 10.0f))
                }

                if (GUI.button(camera, "6 by 6", x + 10.0f, y + 40.0f, 120.0f, 40.0f)) {
                    slot = SaveSlot(Pattern.SQUARE_6, index)
                    slots[index] = slot
                    return slot.startGame()
                }

                if (GUI.button(camera, "8 by 8", x + 160.0f, y + 40.0f, 120.0f, 40.0f)) {
                    slot = SaveSlot(Pattern.SQUARE_8, index)
                    slots[index] = slot
                    return slot.startGame()
                }
            } else {
                val font = Game.assets.font

                Game.batch.use {
                    font.draw(it, when (slot.pattern) {
                        Pattern.SQUARE_6 -> "6 by 6"
                        Pattern.SQUARE_8 -> "8 by 8"
                    }, x + 10.0f, Gdx.graphics.height - (y + 10.0f))
                }

                if (GUI.button(camera, "Start", x + 10.0f, y + 40.0f, 120.0f, 40.0f)) {
                    return slot.startGame()
                }

                if (GUI.button(camera, "Delete", x + 160.0f, y + 40.0f, 120.0f, 40.0f))
                    slots[index] = null
            }
        }

        if (GUI.button(camera, "Back", 325.0f, 480.0f, 150.0f, 50.0f))
            return MenuState()

        return this
    }

    override fun dispose() {
        val data = Gdx.files.local("data.bin")
        data.write(false).use {
            val output = DataOutput(it)
            output.writeInt(slots.count { it != null }, true)
            slots.forEach { slot ->
                output.writeBoolean(slot != null)
                if (slot != null) {
                    output.writeInt(slot.pattern.ordinal, true)
                }
            }
        }
    }
}