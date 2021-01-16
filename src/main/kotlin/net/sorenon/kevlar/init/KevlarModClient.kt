package net.sorenon.kevlar.init

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.sorenon.kevlar.PhysDebugDrawer

import net.sorenon.kevlar.networking.CreateOrUpdateRigidBodyS2CPacket
import net.sorenon.kevlar.networking.RigidBodyMinimalSyncData
import com.badlogic.gdx.math.Vector3

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo
import net.sorenon.kevlar.networking.EntityGrabRigidBodyS2CPacket


class KevlarModClient : ClientModInitializer {
    companion object {
        lateinit var drawer: PhysDebugDrawer

        var debugDrawEnabled = false
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
                    val phys = KevlarComponents.PHYS_WORLD.get(handler.world)
                    for (syncData in rbs) {
                        val rb = phys.registeredRigidBodies[syncData.id]!!
//                        rb.getWorldTransform(mat)
                        //TODO interpolate between transforms
                        rb.forceActivationState(syncData.activationState)
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

        ClientPlayNetworking.registerGlobalReceiver(
            KevlarMod.S2C_CREATE_OR_UPDATE_RB
        ) { client, handler, buf, responseSender ->
            val packet = CreateOrUpdateRigidBodyS2CPacket()
            packet.deserialize(buf)

            client.execute {
                println("Rigidbody recived with id:${packet.id}")
                val phys = KevlarComponents.PHYS_WORLD.get(handler.world)
                val shape = packet.shapeWrapper.getShape()
                val inertia = Vector3()
                shape.calculateLocalInertia(packet.mass, inertia)
                val rbInfo = btRigidBodyConstructionInfo(packet.mass, btDefaultMotionState(), shape, inertia)
                val rb = btRigidBody(rbInfo)
                phys.registeredRigidBodies[packet.id] = rb
                phys.dynamicsWorld.addRigidBody(rb)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(
            KevlarMod.S2C_REMOVE_RB
        ) { client, handler, buf, responseSender ->
            val id = buf.readShort()

            client.execute {
                val phys = KevlarComponents.PHYS_WORLD.get(handler.world)
                val rb = phys.registeredRigidBodies.remove(id)
                phys.dynamicsWorld.removeRigidBody(rb)
                rb!!.dispose()
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(
            KevlarMod.S2C_GRAB_RB
        ) { client, handler, buf, responseSender ->
            val packet = EntityGrabRigidBodyS2CPacket()
            packet.deserialize(buf)

            client.execute {
                val world = handler.world
                val phys = KevlarComponents.PHYS_WORLD.get(world)
                val rb = phys.registeredRigidBodies[packet.rigidBodyID]!!
                rb.userValue = packet.rigidBodyID.toInt()
                val entity = world.getEntityById(packet.entityID)!!
                val grabComponent = KevlarComponents.GRABBER.get(entity)
                grabComponent.grabRigidBody(rb) //TODO make forceGrab function
                grabComponent.distance = 1.2
            }
        }
    }
}