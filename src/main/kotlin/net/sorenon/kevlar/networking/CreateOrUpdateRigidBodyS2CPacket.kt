package net.sorenon.kevlar.networking

import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import net.minecraft.network.PacketByteBuf
import net.sorenon.kevlar.shapes.CollisionShapeWrapper
import net.sorenon.kevlar.readVec
import net.sorenon.kevlar.writeVec

class CreateOrUpdateRigidBodyS2CPacket {
    var id: Short = -1
    var mass = -1f
    lateinit var pos: Vector3
    lateinit var rot: Quaternion
    lateinit var shapeWrapper: CollisionShapeWrapper

    constructor()

    constructor(id: Short, mass: Float, rigidBody: btRigidBody, shapeWrapper: CollisionShapeWrapper) {
        this.id = id
        this.mass = mass
        this.pos = rigidBody.centerOfMassPosition
        this.shapeWrapper = shapeWrapper
    }

    fun serialize(buf: PacketByteBuf) {
        buf.writeShort(id.toInt())
        buf.writeFloat(mass)
        writeVec(pos, buf)
        shapeWrapper.serializeWithId(buf)
    }

    fun deserialize(buf: PacketByteBuf) {
        id = buf.readShort()
        mass = buf.readFloat()
        pos = readVec(buf)
        shapeWrapper = CollisionShapeWrapper.deserialize(buf)
    }

    fun apply() {

    }
}