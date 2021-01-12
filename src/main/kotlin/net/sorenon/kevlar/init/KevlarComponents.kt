package net.sorenon.kevlar.init

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer
import net.minecraft.util.Identifier
import net.sorenon.kevlar.GrabberComponent
import net.sorenon.kevlar.PhysicsWorldComponent

class KevlarComponents : EntityComponentInitializer, WorldComponentInitializer {
    companion object {
        @JvmStatic
        val GRABBER: ComponentKey<GrabberComponent> = ComponentRegistry.getOrCreate(
            Identifier("kevlar", "grabber"),
            GrabberComponent::class.java
        )

        @JvmStatic
        val PHYS_WORLD: ComponentKey<PhysicsWorldComponent> = ComponentRegistry.getOrCreate(
            Identifier("kevlar", "physics_world"),
            PhysicsWorldComponent::class.java
        )
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(PHYS_WORLD, ::PhysicsWorldComponent)
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(GRABBER, ::GrabberComponent)
    }
}