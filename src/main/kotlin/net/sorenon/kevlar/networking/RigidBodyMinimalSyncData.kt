@file:Suppress("JoinDeclarationAndAssignment")

package net.sorenon.kevlar.networking

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import net.minecraft.network.PacketByteBuf
import net.sorenon.kevlar.readVec
import net.sorenon.kevlar.writeVec

class RigidBodyMinimalSyncData(buf: PacketByteBuf) {
    companion object {
        val matrix4 = Matrix4()
        val pos = Vector3()
        val quat = Quaternion()

        fun write(rb: btRigidBody, buf: PacketByteBuf) {
            val activationState = rb.activationState
            buf.writeByte(activationState)
            rb.getWorldTransform(matrix4)
            matrix4.getTranslation(pos)
            matrix4.getRotation(quat)
            writeVec(pos, buf)
            buf.writeFloat(quat.x)
            buf.writeFloat(quat.y)
            buf.writeFloat(quat.z)
            buf.writeFloat(quat.w)
            if (activationState != 0) {
                writeVec(rb.linearVelocity, buf)
                writeVec(rb.angularVelocity, buf)
            }
        }
    }

    val activationState: Int
    val id: Short
    val pos: Vector3
    val rot: Quaternion
    var vel: Vector3? = null
    var aVel: Vector3? = null

    init {
        id = buf.readShort()
        activationState = buf.readByte().toInt()
        pos = readVec(buf)
        rot = Quaternion(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        if (activationState != 0) {
            vel = readVec(buf)
            aVel = readVec(buf)
        }
    }
}