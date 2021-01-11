package net.sorenon.kevlar

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentV3
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import io.netty.buffer.Unpooled
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.sorenon.kevlar.init.KevlarMod
import net.sorenon.kevlar.init.KevlarModClient
import java.util.zip.Deflater


class PhysicsWorldComponent : ComponentV3, Component, ClientTickingComponent {
    val planeShape: btCollisionShape
    val ballShape: btCollisionShape
    val plane: btCollisionObject
    val collisionConfig: btCollisionConfiguration
    val dispatcher: btDispatcher
    val broadphase: btBroadphaseInterface
    val dynamicsWorld: btDynamicsWorld
    val constraintSolver: btConstraintSolver
    val tickCallback: PhysicsTickCallback

    init {
        planeShape = btStaticPlaneShape(Vector3(0f, 1f, 0f), 1f)
        ballShape = btSphereShape(0.5f)
        collisionConfig = btDefaultCollisionConfiguration()
        dispatcher = btCollisionDispatcher(collisionConfig)
        broadphase = btDbvtBroadphase()
        constraintSolver = btSequentialImpulseConstraintSolver()
        dynamicsWorld = btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig)
        dynamicsWorld.gravity = Vector3(0f, -15f, 0f)
        plane = btCollisionObject()
        plane.collisionShape = planeShape
        dynamicsWorld.addCollisionObject(plane)
        tickCallback = PhysicsTickCallback(dynamicsWorld, true)
        tickCallback.attach()
        dynamicsWorld.debugDrawer = KevlarModClient.drawer
        KevlarMod.PHYS_GUN_ITEM.grabBody = btRigidBody(0f, btDefaultMotionState(), btEmptyShape())
    }

    override fun readFromNbt(p0: CompoundTag) {

    }

    override fun writeToNbt(p0: CompoundTag) {

    }

    override fun clientTick() {
        dynamicsWorld.stepSimulation(1 / 20f)

        val matrix4 = Matrix4()

//        val data = PacketByteBuf(Unpooled.buffer())
//        val arr = dynamicsWorld.collisionObjectArrayConst
//        for (i in 0 until arr.size()) {
//            val colObj = arr.atConst(i)
//            if (colObj.isStaticOrKinematicObject) {
//                continue
//            }
//
//            colObj.getWorldTransform(matrix4)
//            matrix4.values.forEach { data.writeFloat(it) }
//            data.writeInt(colObj.activationState)
//            data.writeFloat(colObj.deactivationTime)
//            data.writeFloat(colObj.friction)
//            data.writeFloat(colObj.rollingFriction)
//            data.writeFloat(colObj.restitution)
//
//            if (colObj is btRigidBody) {
//                var vec = colObj.linearVelocity
//                data.writeFloat(vec.x)
//                data.writeFloat(vec.y)
//                data.writeFloat(vec.z)
//                vec = colObj.angularVelocity
//                data.writeFloat(vec.x)
//                data.writeFloat(vec.y)
//                data.writeFloat(vec.z)
//                vec = colObj.totalForce
//                data.writeFloat(vec.x)
//                data.writeFloat(vec.y)
//                data.writeFloat(vec.z)
//                vec = colObj.totalTorque
//                data.writeFloat(vec.x)
//                data.writeFloat(vec.y)
//                data.writeFloat(vec.z)
//            }
//
//            data.clear()
//        }
    }
}

