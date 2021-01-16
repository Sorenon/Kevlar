package net.sorenon.kevlar

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.linearmath.btTransform
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentV3
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.sorenon.kevlar.init.KevlarMod
import net.sorenon.kevlar.init.KevlarModClient
import net.sorenon.kevlar.networking.CreateOrUpdateRigidBodyS2CPacket
import net.sorenon.kevlar.networking.RigidBodyMinimalSyncData
import net.sorenon.kevlar.shapes.CollisionShapeWrapper

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
    var nextID: Short = 0 //TODO remove this
    var ticks = 0;

    var lastPos = Vector3()

    val explosions = arrayListOf<Vec3d>()

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
        PhysicsCollisionCallback().enableOnStarted()
        dynamicsWorld.debugDrawer = KevlarModClient.drawer
    }

    override fun readFromNbt(p0: CompoundTag) {

    }

    override fun writeToNbt(p0: CompoundTag) {

    }

    fun addRegisteredRigidBody(rb: btRigidBody, mass: Float, shapeWrapper: CollisionShapeWrapper) {
        if (!world.isClient) {
            dynamicsWorld.addRigidBody(rb)
            val id = nextID++
            registeredRigidBodies[id] = rb
            rb.flags = rb.flags or BulletPhysicsGlobals.FLAG_SERVERSIDE
            rb.userValue = id.toInt()
            val buf = PacketByteBufs.create()
            CreateOrUpdateRigidBodyS2CPacket(id, mass, rb, shapeWrapper).serialize(buf)
            println("Rigidbody created with id:$id")
            PlayerLookup.world(world as ServerWorld).forEach {
                ServerPlayNetworking.send(it, KevlarMod.S2C_CREATE_OR_UPDATE_RB, buf)
            }
        }
    }

    fun addRegisteredRigidBody2(transform: Matrix4, mass: Float, shapeWrapper: CollisionShapeWrapper): btRigidBody {
        if (!world.isClient) {//btDefaultMotionState(transform)
            //Make rigidbody
            val shape = shapeWrapper.getShape()
            val fallInertia = Vector3()
            shape.calculateLocalInertia(mass, fallInertia)
            val constructionInfo = btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, fallInertia)
            constructionInfo.startWorldTransform = btTransform(transform)

            val rigidBody = btRigidBody(constructionInfo)
            rigidBody.restitution = 0.1f

            //Register rigidbody
            val id = nextID++
            dynamicsWorld.addRigidBody(rigidBody)
            registeredRigidBodies[id] = rigidBody
            rigidBody.flags = rigidBody.flags or BulletPhysicsGlobals.FLAG_SERVERSIDE
            rigidBody.userValue = id.toInt()

            //Tell clients about rigidbody
            val buf = PacketByteBufs.create()
            CreateOrUpdateRigidBodyS2CPacket(id, mass, rigidBody, shapeWrapper).serialize(buf)
            PlayerLookup.world(world as ServerWorld).forEach {
                ServerPlayNetworking.send(it, KevlarMod.S2C_CREATE_OR_UPDATE_RB, buf)
            }

            println("Rigidbody created with id:$id")
            return rigidBody
        }
        throw RuntimeException("This aint meant to be called on the client m8")
    }

    override fun serverTick() {
        stepSimulation(1f / 20, 10)
        ticks += 1

        val buf = PacketByteBufs.create()
        for (pair in registeredRigidBodies) {
            buf.writeShort(pair.key.toInt())
            RigidBodyMinimalSyncData.write(pair.value, buf)
        }
        PlayerLookup.world(world as ServerWorld).forEach {
            ServerPlayNetworking.send(it, KevlarMod.S2C_UPDATE_RIGIDBODY_STATES, buf)
        }
    }

    fun stepSimulation(delta: Float, maxSubSteps: Int) {
        dynamicsWorld.stepSimulation(delta, maxSubSteps, 1 / 60f)
    }

    override fun clientTick() {
//        stepSimulation(1 / 20f, 10)
    }
}

