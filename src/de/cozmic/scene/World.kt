package de.cozmic.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game
import de.cozmic.Job
import de.cozmic.Pattern
import de.cozmic.graphics.Graphics
import de.cozmic.scene.items.BatteryItem
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

class World(val pattern: Pattern, val file: String) {
    val player: Player
    val jobManager = pattern.getJobManager()
    val jobs = ArrayDeque<Job>(8)
    val currentJob: Job? get() = jobs.first

    var solved = false
    var failed = false

    private val tiles: Array<Tile>
    private val entities: ArrayList<Entity>

    init {
        Game.worldDrawSize = min(32.0f * 16.0f / pattern.size, 64.0f)
        player = Player(pattern.playerX.toFloat() * Game.worldDrawSize, pattern.playerY.toFloat() * Game.worldDrawSize, this)
        tiles = Array(pattern.size * pattern.size) {
            val x = it % pattern.size
            val y = it / pattern.size
            pattern.generate(this, x, y)
        }
        entities = arrayListOf(player)

        repeat(10000) {
            entities += BatteryItem(this, 1000.0f, 0.0f, 0.5f)
        }

        load()
    }

    fun getTile(x: Int, y: Int) = tiles[x + y * pattern.size]

    fun setTile(x: Int, y: Int, tile: Tile) {
        tiles[x + y * pattern.size] = tile
    }

    fun render() {
        jobs.forEach {
            it.isNew = false
        }

        val job = jobManager.update()
        if (job != null)
            jobs += job

        tiles.forEach {
            it.render()
        }

        entities.forEach {
            it.render()
        }
    }

    fun load() {
        val file = Gdx.files.local(file)
        if (file.exists()) {
            file.read().use {
                val data = DataInput(it)

                val numJobs = data.readInt(true)
                repeat(numJobs) {
                    val job = Job(data.readFloat(), data.readFloat())
                    job.isCreated = data.readBoolean()
                    jobs += job
                }

                entities.forEach { entity ->
                    entity.read(data)
                }

                repeat(pattern.size * pattern.size) {
                    val x = data.readInt(true)
                    val y = data.readInt(true)
                    val type = TileType[data.readString()]!!
                    val tile = type.create(this, x, y)
                    tile.read(data)
                    setTile(x, y, tile)
                }
            }
        }
    }

    fun save() {
        val file = Gdx.files.local(file)
        file.write(false).use {
            val data = DataOutput(it)

            data.writeInt(jobs.size, true)
            jobs.forEach { job ->
                data.writeFloat(job.prepareTime)
                data.writeFloat(job.price)
                data.writeBoolean(job.isCreated)
            }

            entities.forEach { entity ->
                entity.write(data)
            }

            tiles.forEachIndexed { index, tile ->
                data.writeInt(index % pattern.size, true)
                data.writeInt(index / pattern.size, true)
                data.writeString(tile::class.java.name)
                tile.write(data)
            }
        }
    }

    fun forEachObject(range: Rectangle, block: (WorldObject) -> Unit) {
        var x = 0
        while (x < ceil(range.width / Game.worldDrawSize)) {
            var y = 0
            while (y < ceil(range.height / Game.worldDrawSize)) {
                val xx = floor(range.x / Game.worldDrawSize).toInt() + x
                val yy = floor(range.y / Game.worldDrawSize).toInt() + y
                if (xx >= 0 && xx < pattern.size && yy >= 0 && yy < pattern.size)
                    block(tiles[xx + yy * pattern.size])
                y++
            }
            x++
        }

        entities.forEach {
            if (range.contains(it.rect) || it.rect.overlaps(range))
                block(it)
        }
    }

    fun getNextItem(): Item? {
        val item = entities.find { it is Item && !it.isActive } as Item?
        item?.isActive = true
        return item
    }

    fun completeJob(x: Int, y: Int): Boolean {
        val job = currentJob ?: return false

        Graphics.addSmoke(x * Game.worldDrawSize + Game.worldDrawSize * 0.5f, y * Game.worldDrawSize + Game.worldDrawSize * 0.5f)

        val battery = entities.find { it is BatteryItem && !it.isActive }!! as BatteryItem
        battery.charge = job.price
        battery.rect.x = x * Game.worldDrawSize + (Game.worldDrawSize - battery.rect.width) * 0.5f
        battery.rect.y = y * Game.worldDrawSize + (Game.worldDrawSize - battery.rect.height) * 0.5f
        battery.isActive = true

        jobs.removeFirst()

        return true
    }
}