package de.cozmic.graphics

import com.badlogic.gdx.utils.BufferUtils
import java.nio.ByteBuffer

sealed class DrawData(private val stride: Int, size: Int) {
    var size = size
        private set

    val count get() = buffer.position() / stride

    protected var buffer = BufferUtils.newByteBuffer(size * stride)

    protected open fun grow(oldSize: Int) = (oldSize * 3) / 2

    fun ensureSize(size: Int) {
        while (count + size > this.size) {
            val newSize = grow(this.size)
            val newBuffer = BufferUtils.newByteBuffer(newSize * stride)
            newBuffer.put(buffer)
            buffer = newBuffer
            this.size = newSize
        }
    }

    fun flush(block: (ByteBuffer) -> Unit) {
        buffer.limit(buffer.position())
        buffer.position(0)
        block(buffer)
        buffer.limit(buffer.capacity())
    }

    fun clear() {
        buffer.position(0)
    }

    open class Vertices(size: Int) : DrawData(5 shl 2, size) {
        fun addVertex(x: Float, y: Float, color: Int, u: Float = 0.0f, v: Float = 0.0f) {
            ensureSize(1)

            buffer.putFloat(x)
            buffer.putFloat(y)
            buffer.putInt(color)
            buffer.putFloat(u)
            buffer.putFloat(v)
        }
    }

    open class Indices(size: Int) : DrawData(1 shl 2, size) {
        fun addIndex(index: Int) {
            ensureSize(1)

            buffer.putInt(index)
        }
    }
}