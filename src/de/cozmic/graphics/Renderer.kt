package de.cozmic.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.*
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import de.cozmic.graphics.DrawData
import de.cozmic.graphics.Path
import de.cozmic.scene.Direction

class Renderer internal constructor() : Disposable {
    companion object {
        var defaultLineThickness = 4.0f
    }

    private val tempPath = Path()
    private val vertices = DrawData.Vertices(1000)
    private val indices = DrawData.Indices(1000)
    private var currentTexture = 0
    private var currentIndex = 0
    private var flushThreshold = 1000
    private var active = false

    private val vbo = Gdx.gl.glGenBuffer()
    private val ebo = Gdx.gl.glGenBuffer()
    private val shader = run {
        val shader = ShaderProgram("""
        attribute vec2 aPosition;
        attribute vec4 aColor;
        attribute vec2 aTexcoord;

        varying vec4 vColor;
        varying vec2 vTexcoord;

        uniform mat4 uProjView;

        void main() {
            gl_Position = uProjView * vec4(aPosition, 0.0, 1.0);
            vColor = aColor;
            vTexcoord = aTexcoord;
        }
    """, """
        #ifdef GL_ES
        precision mediump float;
        #endif

        varying vec4 vColor;
        varying vec2 vTexcoord;

        uniform sampler2D uTexture;

        void main() {
            gl_FragColor = texture2D(uTexture, vTexcoord) * vColor;
        }
    """)
        if (!shader.isCompiled)
            throw RuntimeException(shader.log)
        shader
    }

    private val defaultTexture: TextureRegion

    init {
        val pixmap = Pixmap(2, 2, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        defaultTexture = TextureRegion(Texture(pixmap))
        pixmap.dispose()
    }

    private fun setTexture(texture: Int) {
        if (texture == currentTexture)
            return

        flush()

        Gdx.gl.glBindTexture(GL_TEXTURE_2D, texture)
        currentTexture = texture
    }

    private fun checkFlush(numVertices: Int, numIndices: Int) {
        if (numVertices > flushThreshold)
            flushThreshold = numVertices

        if (numIndices > flushThreshold)
            flushThreshold = numIndices

        if (vertices.count + numVertices > flushThreshold || indices.count + numIndices > flushThreshold)
            flush()

        vertices.ensureSize(numVertices)
        indices.ensureSize(numIndices)
    }

    private fun flush() {
        val numIndices = indices.count

        if (numIndices > 0) {
            vertices.flush { data, size ->
                Gdx.gl.glBufferData(GL_ARRAY_BUFFER, size, data, GL_STREAM_DRAW)
            }

            indices.flush { data, size ->
                Gdx.gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, size, data, GL_STREAM_DRAW)
            }

            Gdx.gl.glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0)

            currentIndex = 0
        }
    }

    fun draw(region: TextureRegion, x: Float, y: Float, width: Float, height: Float = width, direction: Direction = Direction.NONE, color: Color = Color.WHITE) {
        setTexture(region.texture.textureObjectHandle)

        when (direction) {
            Direction.NONE,
            Direction.NORTH -> drawRect(x, y, width, height, region.u, region.v, region.u2, region.v2, 0.0f, color)
            Direction.EAST -> drawRect(x, y, width, height, region.u, region.v, region.u2, region.v2, 90.0f, color)
            Direction.SOUTH -> drawRect(x, y, width, height, region.u, region.v, region.u2, region.v2, 180.0f, color)
            Direction.WEST -> drawRect(x, y, width, height, region.u, region.v, region.u2, region.v2, 270.0f, color)
        }
    }

    fun drawRotated(region: TextureRegion, x: Float, y: Float, width: Float, height: Float = width, rotation: Float = 0.0f, color: Color = Color.WHITE) {
        setTexture(region.texture.textureObjectHandle)
        drawRect(x, y, width, height, region.u, region.v, region.u2, region.v2, rotation, color)
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float, u0: Float = 0.0f, v0: Float = 0.0f, u1: Float = 1.0f, v1: Float = 1.0f, rotation: Float = 0.0f, color: Color = Color.WHITE) {
        require(active)

        checkFlush(4, 6)

        val colorBits = color.toIntBits()

        var x0 = x
        var y0 = y
        var x1 = x + width
        var y1 = y
        var x2 = x + width
        var y2 = y + height
        var x3 = x
        var y3 = y + height

        if (!isEqual(rotation, 0.0f)) {
            val u = cosDeg(rotation)
            val v = sinDeg(rotation)

            val hw = width * 0.5f
            val hh = height * 0.5f

            val rx0 = -hw
            val ry0 = -hh
            val rx1 = hw
            val ry1 = hh

            x0 = x + hw + (u * rx0 - v * ry0)
            y0 = y + hh + (v * rx0 + u * ry0)

            x1 = x + hw + (u * rx1 - v * ry0)
            y1 = y + hh + (v * rx1 + u * ry0)

            x2 = x + hw + (u * rx1 - v * ry1)
            y2 = y + hh + (v * rx1 + u * ry1)

            x3 = x + hw + (u * rx0 - v * ry1)
            y3 = y + hh + (v * rx0 + u * ry1)
        }

        vertices.addVertex(x0, y0, colorBits, u0, v0)
        vertices.addVertex(x1, y1, colorBits, u1, v0)
        vertices.addVertex(x2, y2, colorBits, u1, v1)
        vertices.addVertex(x3, y3, colorBits, u0, v1)

        indices.addIndex(currentIndex)
        indices.addIndex(currentIndex + 1)
        indices.addIndex(currentIndex + 2)
        indices.addIndex(currentIndex)
        indices.addIndex(currentIndex + 2)
        indices.addIndex(currentIndex + 3)

        currentIndex += 4
    }

    fun addPathFilled(color: Color = Color.WHITE, block: Path.() -> Unit) {
        val path = tempPath
        path.reset()
        block(path)
        drawFillPath(path, color)
    }

    fun addPathStroke(color: Color = Color.WHITE, thickness: Float = defaultLineThickness, block: Path.() -> Unit) {
        val path = tempPath
        path.reset()
        block(path)
        drawStrokePath(path, thickness, color)
    }

    fun drawFillPath(path: Path, color: Color = Color.WHITE) {
        if (!active)
            throw IllegalStateException()

        setTexture(defaultTexture.texture.textureObjectHandle)

        val vertexCount = path.size
        val indexCount = (vertexCount - 2) * 3

        checkFlush(vertexCount, indexCount)

        val colorBits = color.toIntBits()
        path.process { index, point ->
            vertices.addVertex(point.x, point.y, colorBits)

            if (index >= 2) {
                indices.addIndex(currentIndex)
                indices.addIndex(currentIndex + index - 1)
                indices.addIndex(currentIndex + index)
            }
        }

        currentIndex += vertexCount
    }

    fun drawStrokePath(path: Path, thickness: Float = defaultLineThickness, color: Color = Color.WHITE) {
        if (!active)
            throw IllegalStateException()

        setTexture(defaultTexture.texture.textureObjectHandle)

        val pointsCount = path.size
        val indexCount = pointsCount * 6
        val vertexCount = pointsCount * 2

        checkFlush(vertexCount, indexCount)

        val colorBits = color.toIntBits()

        var directionX = 0.0f
        var directionY = 0.0f

        fun computeDirection(x0: Float, y0: Float, x1: Float, y1: Float) {
            directionX = x0 - x1
            directionY = y0 - y1
            val invLength = 1.0f / Vector2.len(directionX, directionY)
            directionX *= invLength
            directionY *= invLength
        }

        var normalX = 0.0f
        var normalY = 0.0f

        fun computeNormal(x0: Float, y0: Float, x1: Float, y1: Float): Float {
            normalX = x0 + x1
            normalY = y0 + y1
            val invLength = 1.0f / Vector2.len(normalX, normalY)
            normalX *= invLength
            normalY *= invLength

            return thickness * 0.5f / Vector2.dot(normalX, normalY, -y0, x0)
        }

        path.process { points ->
            (0 until pointsCount).forEach {
                val prevPointIndex = if (it - 1 >= 0) it - 1 else pointsCount - 1
                val pointIndex = it
                val nextPointIndex = if (it + 1 == pointsCount) 0 else it + 1

                val prevPoint = points[prevPointIndex]
                val point = points[pointIndex]
                val nextPoint = points[nextPointIndex]

                computeDirection(prevPoint.x, prevPoint.y, point.x, point.y)
                val toPrevX = directionX
                val toPrevY = directionY

                computeDirection(nextPoint.x, nextPoint.y, point.x, point.y)
                val toNextX = directionX
                val toNextY = directionY

                val extrudeLength = computeNormal(toPrevX, toPrevY, toNextX, toNextY)

                vertices.addVertex(point.x + normalX * extrudeLength, point.y + normalY * extrudeLength, colorBits)
                vertices.addVertex(point.x - normalX * extrudeLength, point.y - normalY * extrudeLength, colorBits)

                val prevPointDrawIndex = prevPointIndex * 2
                val pointDrawIndex = pointIndex * 2

                indices.addIndex(currentIndex + prevPointDrawIndex)
                indices.addIndex(currentIndex + prevPointDrawIndex + 1)
                indices.addIndex(currentIndex + pointDrawIndex + 1)
                indices.addIndex(currentIndex + prevPointDrawIndex)
                indices.addIndex(currentIndex + pointDrawIndex + 1)
                indices.addIndex(currentIndex + pointDrawIndex)
            }
        }

        currentIndex += pointsCount * 2
    }

    fun <T> render(camera: OrthographicCamera, block: (Renderer) -> T): T {
        if (active)
            throw IllegalStateException()
        active = true

        Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, vbo)
        Gdx.gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)

        shader.begin()
        shader.setUniformMatrix("uProjView", camera.combined)
        shader.setUniformi("uTexture", 0)

        setTexture(defaultTexture.texture.textureObjectHandle)

        val positionLocation = shader.getAttributeLocation("aPosition")
        val colorLocation = shader.getAttributeLocation("aColor")
        val texcoordLocation = shader.getAttributeLocation("aTexcoord")

        Gdx.gl.glEnableVertexAttribArray(positionLocation)
        Gdx.gl.glEnableVertexAttribArray(colorLocation)
        Gdx.gl.glEnableVertexAttribArray(texcoordLocation)

        Gdx.gl.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 5 shl 2, 0)
        Gdx.gl.glVertexAttribPointer(colorLocation, 4, GL_UNSIGNED_BYTE, true, 5 shl 2, 2 shl 2)
        Gdx.gl.glVertexAttribPointer(texcoordLocation, 2, GL_FLOAT, false, 5 shl 2, 3 shl 2)

        val result = block(this)
        flush()

        Gdx.gl.glDisableVertexAttribArray(positionLocation)
        Gdx.gl.glDisableVertexAttribArray(colorLocation)
        Gdx.gl.glDisableVertexAttribArray(texcoordLocation)

        setTexture(0)

        shader.end()

        Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, 0)
        Gdx.gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        active = false
        return result
    }

    override fun dispose() {
        Gdx.gl.glDeleteBuffer(vbo)
        Gdx.gl.glDeleteBuffer(ebo)
        shader.dispose()
        defaultTexture.texture.dispose()
    }
}