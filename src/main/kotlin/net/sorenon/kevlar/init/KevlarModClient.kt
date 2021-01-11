package net.sorenon.kevlar.init

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet.Bullet
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.MinecraftClient
import net.sorenon.kevlar.PhysDebugDrawer
import net.sorenon.kevlar.PhysicsWorldComponent
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs

import net.minecraft.network.PacketByteBuf
import net.sorenon.kevlar.networking.RigidBodyMinimalSyncData


class KevlarModClient : ClientModInitializer {
    companion object {
        lateinit var drawer: PhysDebugDrawer
    }

    override fun onInitializeClient() {
        Bullet.init()
        drawer = PhysDebugDrawer()

        ClientPlayNetworking.registerGlobalReceiver(
            KevlarMod.S2C_UPDATE_RIGIDBODY_STATES
        ) { client, handler, buf, responseSender ->
            val rbs = arrayListOf<RigidBodyMinimalSyncData>()
            while (buf.readableBytes() > 0) {
                rbs.add(RigidBodyMinimalSyncData(buf))
            }

            if (rbs.isNotEmpty()) {
                client.execute {
                    val mat = Matrix4()
                    val phys = KevlarComponents.PHYS_WORLD.get(client.world)
                    for (syncData in rbs) {
                        val rb = phys.registeredRigidBodies[syncData.id]!!
//                        rb.getWorldTransform(mat)
                        //TODO interpolate between transforms
                        mat.idt()
                        mat.setTranslation(syncData.pos)
                        mat.rotate(syncData.rot)
                        rb.worldTransform = mat
                        rb.linearVelocity = syncData.vel
                        rb.angularVelocity = syncData.aVel
                    }
                }
            }
        }
    }
}