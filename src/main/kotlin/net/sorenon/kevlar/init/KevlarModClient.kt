package net.sorenon.kevlar.init

import com.badlogic.gdx.physics.bullet.Bullet
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.sorenon.kevlar.PhysDebugDrawer
import net.sorenon.kevlar.PhysicsWorldComponent

class KevlarModClient : ClientModInitializer {
    companion object {
        lateinit var drawer: PhysDebugDrawer
    }

    override fun onInitializeClient() {
        Bullet.init()
        drawer = PhysDebugDrawer()

//        ClientTickEvents.START_WORLD_TICK.register(ClientTickEvents.StartWorldTick {
//            phys.dynamicsWorld.stepSimulation(1 / 20f)
//        })
    }
}