package net.sorenon.kevlar.shapes

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import net.minecraft.network.PacketByteBuf
import net.sorenon.kevlar.BulletPhysicsGlobals
import net.sorenon.kevlar.readVec
import net.sorenon.kevlar.writeVec

class BoxShapeWrapper: CollisionShapeWrapper {
    lateinit var halfExtents: Vector3

    constructor()

    constructor(halfExtents: Vector3) {
        this.halfExtents = halfExtents
    }

    override fun serialize(buf: PacketByteBuf) {
        writeVec(halfExtents, buf)
    }

    override fun deserialize(buf: PacketByteBuf) {
        halfExtents = readVec(buf)
    }

    override fun getShape(): btCollisionShape {
        return BulletPhysicsGlobals.getOrMakeBoxShape(halfExtents)
    }
}