@file:Suppress("JoinDeclarationAndAssignment")

package net.sorenon.kevlar.networking

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import net.minecraft.network.PacketByteBuf

class RigidBodyMinimalSyncData(buf: PacketByteBuf) {
    companion object {
        val matrix4 = Matrix4()
        val vec = Vector3()
        val quat = Quaternion()

        fun write(rb: btRigidBody, buf: PacketByteBuf) {
            rb.getWorldTransform(matrix4)
            matrix4.getTranslation(vec)
            matrix4.getRotation(quat)
            writeVec(vec, buf)
            writeVec(rb.linearVelocity, buf)
            buf.writeFloat(quat.x)
            buf.writeFloat(quat.y)
            buf.writeFloat(quat.z)
            buf.writeFloat(quat.w)
            writeVec(rb.angularVelocity, buf)
        }

        private fun writeVec(vec: Vector3, buf: PacketByteBuf) {
            buf.writeFloat(vec.x)
            buf.writeFloat(vec.y)
            buf.writeFloat(vec.z)
        }
    }

    val id: Int
    val pos: Vector3
    val vel: Vector3
    val rot: Quaternion
    val aVel: Vector3

    init {
        id = buf.readInt()
        pos = readVec(buf)
        vel = readVec(buf)
        rot = Quaternion(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        aVel = readVec(buf)
    }

    private fun readVec(buf: PacketByteBuf): Vector3 {
        return Vector3(buf.readFloat(), buf.readFloat(), buf.readFloat())
    }
}