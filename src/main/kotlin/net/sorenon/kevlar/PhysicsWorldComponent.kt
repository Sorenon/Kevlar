package net.sorenon.kevlar

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentV3
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import net.sorenon.kevlar.init.KevlarMod
import net.sorenon.kevlar.init.KevlarModClient
import net.sorenon.kevlar.networking.RigidBodyMinimalSyncData

class PhysicsWorldComponent(val world: World) : ComponentV3, Component, ClientTickingComponent, ServerTickingComponent {
    val planeShape: btCollisionShape
    val ballShape: btCollisionShape
    val plane: btCollisionObject
    val collisionConfig: btCollisionConfiguration
    val dispatcher: btDispatcher
    val broadphase: btBroadphaseInterface
    val dynamicsWorld: btDynamicsWorld
    val constraintSolver: btConstraintSolver
    val tickCallback: PhysicsTickCallback
    var ticks = 0;

    val registeredRigidBodies = hashMapOf<Short, btRigidBody>()

    init {
        planeShape = btStaticPlaneShape(Vector3(0f, 1f, 0f), 1f)
        ballShape = btSphereShape(0.5f)
        collisionConfig = btDefaultCollisionConfiguration()
        dispatcher = btCollisionDispatcher(collisionConfig)
        broadphase = btDbvtBroadphase()
        constraintSolver = btSequentialImpulseConstraintSolver()
        dynamicsWorld = btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig)
        dynamicsWorld.gravity = Vector3(0f, -9.81f, 0f)
        plane = btCollisionObject()
        plane.collisionShape = planeShape
        dynamicsWorld.addCollisionObject(plane)
        tickCallback = PhysicsTickCallback(this, true)
        tickCallback.attach()
        dynamicsWorld.debugDrawer = KevlarModClient.drawer
    }

    override fun readFromNbt(p0: CompoundTag) {

    }

    override fun writeToNbt(p0: CompoundTag) {

    }

    fun addRegisteredRigidBody(rb: btRigidBody) {
        dynamicsWorld.addRigidBody(rb)
        registeredRigidBodies[registeredRigidBodies.keys.size.toShort()] = rb
    }

    override fun serverTick() {
        stepSimulation(1f / 20, 10)
        ticks += 1

        if (ticks % 2 == 0) {
            val buf = PacketByteBufs.create()
            for (pair in registeredRigidBodies) {
                buf.writeShort(pair.key.toInt())
                RigidBodyMinimalSyncData.write(pair.value, buf)
            }

            PlayerLookup.world(world as ServerWorld).forEach {
                ServerPlayNetworking.send(it, KevlarMod.S2C_UPDATE_RIGIDBODY_STATES, buf)
            }
        }
    }

    fun stepSimulation(delta: Float, maxSubSteps: Int) {
        dynamicsWorld.stepSimulation(delta, maxSubSteps, 1 / 80f)
    }

    override fun clientTick() {
//        dynamicsWorld.stepSimulation(1 / 20f, 10)
    }
}

