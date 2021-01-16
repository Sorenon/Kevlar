package net.sorenon.kevlar.networking

import net.minecraft.network.PacketByteBuf

class EntityGrabRigidBodyS2CPacket {
    var entityID = -1
    var rigidBodyID: Short = -1

    constructor()

    constructor(entityID: Int, rigidBodyID: Short) {
        this.entityID = entityID
        this.rigidBodyID = rigidBodyID
    }

    fun serialize(buf: PacketByteBuf) {
        buf.writeInt(entityID)
        buf.writeShort(rigidBodyID.toInt())
    }

    fun deserialize(buf: PacketByteBuf) {
        entityID = buf.readInt()
        rigidBodyID = buf.readShort()
    }
}