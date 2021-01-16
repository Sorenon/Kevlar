package net.sorenon.kevlar.shapes

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import net.minecraft.network.PacketByteBuf

interface CollisionShapeWrapper {
    companion object {
        fun deserialize(buf: PacketByteBuf): CollisionShapeWrapper {
            val id = buf.readByte()

            val wrapper = when (id.toInt()) { //TODO make this not bad
                0 -> {
                    SphereShapeWrapper()
                }
                1 -> {
                    BoxShapeWrapper()
                }
                2 -> {
                    CylinderShapeWrapper()
                }
                else -> throw RuntimeException("Tried to deserialize shape with bad id:$id")
            }
            wrapper.deserialize(buf)

            return wrapper
        }
    }

    fun serializeWithId(buf: PacketByteBuf) {
        if (this is SphereShapeWrapper) {
            buf.writeByte(0)
        }
        else if (this is BoxShapeWrapper) {
            buf.writeByte(1)
        }
        else if (this is CylinderShapeWrapper) {
            buf.writeByte(2)
        }
        this.serialize(buf)
    }

    fun serialize(buf: PacketByteBuf)

    fun deserialize(buf: PacketByteBuf)

    fun getShape(): btCollisionShape
}