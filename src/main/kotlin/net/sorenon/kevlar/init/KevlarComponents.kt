package net.sorenon.kevlar.init

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer
import net.sorenon.kevlar.PhysicsChunkComp
import net.minecraft.util.Identifier
import net.sorenon.kevlar.PhysicsWorldComponent

class KevlarComponents : ChunkComponentInitializer, WorldComponentInitializer {
    public companion object {
        @JvmStatic
        val PHYS_CHUNK: ComponentKey<PhysicsChunkComp> = ComponentRegistry.getOrCreate(
            Identifier("nbulletmc", "physics_chunk"),
            PhysicsChunkComp::class.java
        )

        @JvmStatic
        val PHYS_WORLD: ComponentKey<PhysicsWorldComponent> = ComponentRegistry.getOrCreate(
            Identifier("kevlar", "physics_world"),
            PhysicsWorldComponent::class.java
        )
    }

    override fun registerChunkComponentFactories(registry: ChunkComponentFactoryRegistry) {
        registry.register(PHYS_CHUNK, ::PhysicsChunkComp)
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(PHYS_WORLD, ::PhysicsWorldComponent)
    }
}