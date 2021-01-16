package net.sorenon.kevlar.shapes

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import com.badlogic.gdx.physics.bullet.collision.btSphereShape
import net.minecraft.network.PacketByteBuf

class SphereShapeWrapper: CollisionShapeWrapper {
    var radius = -1f

    constructor()

    constructor(radius: Float) {
        this.radius = radius
    }

    override fun serialize(buf: PacketByteBuf) {
        buf.writeFloat(radius)
    }

    override fun deserialize(buf: PacketByteBuf) {
        radius = buf.readFloat()
    }

    override fun getShape(): btCollisionShape {
        return btSphereShape(radius)
    }
}