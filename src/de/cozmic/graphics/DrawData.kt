package de.cozmic.graphics

import com.badlogic.gdx.utils.BufferUtils
import java.nio.ByteBuffer

sealed class DrawData(private val stride: Int, size: Int) {
    var size = size
        private set

    val count get() = position / stride

    protected var buffer = BufferUtils.newByteBuffer(size * stride)
    protected var position = 0

    protected open fun grow(oldSize: Int) = (oldSize * 3) / 2

    fun ensureSize(size: Int) {
        while (count + size > this.size) {
            val newSize = grow(this.size)
            val newBuffer = BufferUtils.newByteBuffer(newSize * stride)
            BufferUtils.copy(buffer, newBuffer, position)
            buffer = newBuffer
            this.size = newSize
        }
    }

    fun flush(block: (ByteBuffer, Int) -> Unit) {
        block(buffer, position)
        clear()
    }

    fun clear() {
        position = 0
    }

    open class Vertices(size: Int) : DrawData(5 shl 2, size) {
        fun addVertex(x: Float, y: Float, color: Int, u: Float = 0.0f, v: Float = 0.0f) {
            ensureSize(1)

            buffer.putFloat(position, x)
            position += 4

            buffer.putFloat(position, y)
            position += 4

            buffer.putInt(position, color)
            position += 4

            buffer.putFloat(position, u)
            position += 4

            buffer.putFloat(position, v)
            position += 4
        }
    }

    open class Indices(size: Int) : DrawData(1 shl 2, size) {
        fun addIndex(index: Int) {
            ensureSize(1)

            buffer.putInt(position, index)
            position += 4
        }
    }
}