package de.cozmic.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.clamp
import com.badlogic.gdx.math.MathUtils.isEqual
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import de.cozmic.Game
import de.cozmic.getStretched
import de.cozmic.resolveCollisionX
import de.cozmic.resolveCollisionY
import de.cozmic.scene.tiles.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Player(x: Float, y: Float, world: World) : Entity(world, x, y) {
    companion object {
        private val tempObjects = arrayListOf<WorldObject>()
    }

    var charge = 1.0f
        set(value) {
            field = clamp(value, 0.0f, 1.0f)
        }

    private val selector = Game.assets.textures.findRegion("selector")
    private val selection = Game.assets.textures.findRegion("selection")
    private val player = Game.assets.textures.findRegion("player")

    private var selectedTile: Tile? = null

    val collisionRect: Rectangle
        get() {
            if (selectedTile == null)
                return rect

            return rect.getStretched(direction.x * Game.worldDrawSize, direction.y * Game.worldDrawSize)
        }

    private var movementX = 0.0f
    private var movementY = 0.0f

    var speed = 300.0f
    var movementCost = 0.05f

    private var decrementCharge = false

    var isMoving = false
        private set

    var direction = Direction.NONE
        private set

    private fun draw() {
        /*
        Game.batch.draw(player, rect.x, rect.y, Game.worldDrawSize * 0.5f, Game.worldDrawSize * 0.5f, Game.worldDrawSize, Game.worldDrawSize, 1.0f, 1.0f, when (direction) {
            Direction.NONE,
            Direction.NORTH -> 0.0f
            Direction.SOUTH -> 180.0f
            Direction.EAST -> 90.0f
            Direction.WEST -> 270.0f
        })
        */

        Game.renderer.draw(player, rect.x, rect.y, Game.worldDrawSize, Game.worldDrawSize, direction)
    }

    fun moveX(positive: Boolean) {
        if (isMoving)
            return

        decrementCharge = true

        movementX = if (positive) 1.0f else -1.0f
    }

    fun moveY(positive: Boolean) {
        if (isMoving)
            return

        decrementCharge = true

        movementY = if (positive) 1.0f else -1.0f
    }

    override fun render() {
        updateMovement()

        draw()
        drawSelector()
        drawSelectedTile()
    }

    private fun updateMovement() {
        val delta = Gdx.graphics.deltaTime
        val movementAmountX = movementX * speed * delta
        val movementAmountY = movementY * speed * delta
        isMoving = movementAmountX != 0.0f || movementAmountY != 0.0f

        if (isMoving) {
            if (movementAmountX != 0.0f) {
                val oldDirection = direction
                direction = if (movementAmountX > 0.0f)
                    Direction.EAST
                else
                    Direction.WEST

                var amount = movementAmountX

                tempObjects.clear()
                world.forEachObject(collisionRect, tempObjects::plusAssign)
                if (tempObjects.any { if (it is Tile) it.isCollidable else it != this }) {
                    direction = oldDirection
                    amount = 0.0f
                }

                val movementRange = collisionRect.getStretched(x = amount)

                tempObjects.clear()
                world.forEachObject(movementRange, tempObjects::plusAssign)
                tempObjects.sortBy { center.dst2(it.center) }

                for (obj in tempObjects) {
                    if (obj == this)
                        continue

                    val newAmount = if (selectedTile is CrateTile && obj is CounterTile)
                        rect.resolveCollisionX(obj.rect, amount)
                    else
                        collisionRect.resolveCollisionX(obj.rect, amount)

                    if (obj is Tile) {
                        if (obj.isCollidable && isEqual(newAmount, 0.0f)) {
                            if (oldDirection == direction) {
                                obj.onInteraction(this)
                                onInteraction(obj)
                            }
                            amount = 0.0f
                            break
                        }

                        if (newAmount != amount && !isEqual(newAmount, 0.0f)) {
                            if (obj.isCollidable && obj.onCollision(this)) {
                                onCollision(obj)
                                amount = newAmount
                                break
                            }
                        }
                    } else if (obj is Item) {
                        obj.onCollection(this)
                    } else {
                        amount = newAmount
                        break
                    }
                }

                if (amount != movementAmountX)
                    movementX = 0.0f

                if (!isEqual(amount, 0.0f) && decrementCharge) {
                    charge -= movementCost
                    if (charge <= 0.0f)
                        world.failed = true
                    else
                        Game.assets.move.play()
                    decrementCharge = false
                }

                rect.x += amount
            }

            if (movementAmountY != 0.0f) {
                val oldDirection = direction
                direction = if (movementAmountY > 0.0f)
                    Direction.SOUTH
                else
                    Direction.NORTH


                var amount = movementAmountY

                tempObjects.clear()
                world.forEachObject(collisionRect, tempObjects::plusAssign)
                if (tempObjects.any { if (it is Tile) it.isCollidable else it != this }) {
                    direction = oldDirection
                    amount = 0.0f
                }

                val movementRange = collisionRect.getStretched(y = amount)

                tempObjects.clear()
                world.forEachObject(movementRange, tempObjects::plusAssign)
                tempObjects.sortBy { center.dst2(it.center) }

                for (obj in tempObjects) {
                    if (obj == this)
                        continue

                    val newAmount = if (selectedTile is CrateTile && obj is CounterTile)
                        rect.resolveCollisionY(obj.rect, amount)
                    else
                        collisionRect.resolveCollisionY(obj.rect, amount)

                    if (obj is Tile) {
                        if (obj.isCollidable && isEqual(newAmount, 0.0f)) {
                            if (oldDirection == direction) {
                                obj.onInteraction(this)
                                onInteraction(obj)
                            }
                            amount = 0.0f
                            break
                        }

                        if (newAmount != amount && !isEqual(newAmount, 0.0f)) {
                            if (obj.isCollidable && obj.onCollision(this)) {
                                onCollision(obj)
                                amount = newAmount
                                break
                            }
                        }
                    } else if (obj is Item) {
                        obj.onCollection(this)
                    } else {
                        amount = newAmount
                        break
                    }
                }

                if (amount != movementAmountY)
                    movementY = 0.0f

                if (!isEqual(amount, 0.0f) && decrementCharge) {
                    charge -= movementCost
                    if (charge <= 0.0f)
                        world.failed = true
                    else
                        Game.assets.move.play()
                    decrementCharge = false
                }

                rect.y += amount
            }
        }
    }

    private fun drawSelector() {
        if (direction == Direction.NONE)
            return

        if (isMoving)
            return

        if (selectedTile != null)
            return

        val tile = world.getTile((rect.x / Game.worldDrawSize + direction.x).toInt(), (rect.y / Game.worldDrawSize + direction.y).toInt())
        if (!tile.isSelectable && tile !is TankTile)
            return

        val scale = 1.0f + ((sin(Game.time * 5.0f) + 1.0f) * 0.5f) * 0.1f

        val width = rect.width * scale
        val height = rect.height * scale

        val x = rect.x + direction.x * Game.worldDrawSize + (Game.worldDrawSize - width) * 0.5f
        val y = rect.y + direction.y * Game.worldDrawSize + (Game.worldDrawSize - height) * 0.5f

        //Game.batch.draw(selector, x, y, width, height)
        Game.renderer.draw(selector, x, y, width, height)
    }

    private fun drawSelectedTile() {
        val tile = selectedTile ?: return

        val x = rect.x + direction.x * Game.worldDrawSize
        val y = rect.y + direction.y * Game.worldDrawSize

        //Game.batch.draw(tile.texture, x, y, Game.worldDrawSize, Game.worldDrawSize)
        Game.renderer.draw(tile.texture, x, y, Game.worldDrawSize, Game.worldDrawSize)

        if (tile is CrateTile)
            tile.drawDamage(x, y)


        val scale = 1.0f + ((sin(Game.time * 5.0f) + 1.0f) * 0.5f) * 0.05f

        val width = rect.width * scale
        val height = rect.height * scale

        val selectionX = rect.x + direction.x * Game.worldDrawSize + (Game.worldDrawSize - width) * 0.5f
        val selectionY = rect.y + direction.y * Game.worldDrawSize + (Game.worldDrawSize - height) * 0.5f
        Game.renderer.draw(selection, selectionX, selectionY, width, height)
    }

    private fun onInteraction(obj: WorldObject) {
        if (obj !is Tile)
            return

        if (!obj.isSelectable)
            Game.assets.falseInteract.play()

        if (selectedTile != null) {
            val tile = selectedTile!!

            val tileX = (rect.x / Game.worldDrawSize + direction.x).toInt()
            val tileY = (rect.y / Game.worldDrawSize + direction.y).toInt()

            tile.rect.x = tileX * Game.worldDrawSize
            tile.rect.y = tileY * Game.worldDrawSize

            val previousTile = world.getTile(tileX, tileY)
            if (tile is CrateTile && previousTile is CounterTile) {
                if (world.completeJob(tileX, tileY)) {
                    Game.assets.sellBox.play()
                    selectedTile = null
                }
            } else {
                tile.previousTile = previousTile
                world.setTile(tileX, tileY, tile)
                selectedTile = null

                Game.assets.interact.play()
            }
        } else if (obj.isSelectable) {
            selectedTile = obj

            val tileX = (obj.rect.x / Game.worldDrawSize).toInt()
            val tileY = (obj.rect.y / Game.worldDrawSize).toInt()

            world.setTile(tileX, tileY, obj.previousTile ?: BackgroundTile(world, tileX, tileY))

            Game.assets.interact.play()
        }
    }

    private fun onCollision(obj: WorldObject) {
        if (selectedTile is CrateTile && obj is WallTile) {
            val crate = (selectedTile!! as CrateTile)
            crate.damage++
            if (crate.damage >= 4) {
                selectedTile = null

                val x = rect.x + direction.x * Game.worldDrawSize
                val y = rect.y + direction.y * Game.worldDrawSize

                crate.destroy(x, y)
            }
        }
    }

    override fun read(data: DataInput) {
        direction = Direction.values()[clamp(data.readInt(true), 0, Direction.values().size)]
        rect.x = data.readFloat()
        rect.y = data.readFloat()
        charge = data.readFloat()

        if (data.readBoolean()) {
            val type = TileType[data.readString()]!!
            val selectedTileX = data.readInt(true)
            val selectedTileY = data.readInt(true)
            selectedTile = type.create(world, selectedTileX, selectedTileY)
            selectedTile!!.read(data)
        }
    }

    override fun write(data: DataOutput) {
        data.writeInt(direction.ordinal, true)
        data.writeFloat(rect.x)
        data.writeFloat(rect.y)
        data.writeFloat(charge)

        data.writeBoolean(selectedTile != null)
        if (selectedTile != null) {
            data.writeString(selectedTile!!::class.java.name)
            data.writeInt((rect.x / Game.worldDrawSize + direction.x).toInt(), true)
            data.writeInt((rect.y / Game.worldDrawSize + direction.y).toInt(), true)
            selectedTile!!.write(data)
        }
    }
}