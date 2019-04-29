package de.cozmic.scene

import kotlin.reflect.KClass

abstract class TileType<T : Tile> {
    companion object {
        private val registered = hashMapOf<String, TileType<*>>()

        fun <T> getDefaultSupplier(cls: Class<T>): (World, Int, Int) -> T {
            val ctor = cls.constructors.find {
                it.parameters.size == 3 && it.parameters[0].type == World::class.java && it.parameters[1].type == Int::class.java && it.parameters[2].type == Int::class.java
            } ?: throw RuntimeException()
            ctor.isAccessible = true
            return { world, x, y -> ctor.newInstance(world, x, y) as T }
        }

        operator fun get(name: String) = registered[name]

        fun <T : Tile> getFor(tile: T) = get(tile::class.java.name)

        inline fun <reified T : Tile> register(noinline supplier: ((World, Int, Int) -> T)? = null) = register(T::class, supplier)

        fun <T : Tile> register(cls: KClass<T>, supplier: ((World, Int, Int) -> T)? = null) {
            registered[cls.java.name] = object : TileType<T>() {
                val supplier = supplier ?: getDefaultSupplier(cls.java)
                override fun create(world: World, x: Int, y: Int) = supplier(world, x, y)
            }
        }
    }

    abstract fun create(world: World, x: Int, y: Int): T
}