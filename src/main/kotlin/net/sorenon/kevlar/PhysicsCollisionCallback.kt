package net.sorenon.kevlar

import com.badlogic.gdx.physics.bullet.collision.ContactListener
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import net.minecraft.client.MinecraftClient
import net.sorenon.kevlar.init.KevlarComponents

class PhysicsCollisionCallback : ContactListener() {

    override fun onContactStarted(manifold: btPersistentManifold) {
        val body = when {
            manifold.body0 is btRigidBody -> {
                manifold.body0 as btRigidBody
            }
            manifold.body1 is btRigidBody -> {
                manifold.body1 as btRigidBody
            }
            else -> {
                return
            }
        }
        var flags = body.flags
        if (flags and BulletPhysicsGlobals.FLAG_THROWN_BY_GRAVGUN != 0) {
            if (flags and BulletPhysicsGlobals.FLAG_SERVERSIDE != 0) {
                if (body.linearVelocity.len() > 4.0f) {
                    val pos = vec(body.centerOfMassPosition)
//                    MinecraftClient.getInstance().server!!.execute {
//                        MinecraftClient.getInstance().server!!.overworld.createExplosion(null, pos.x, pos.y, pos.z, 4.0f, Explosion.DestructionType.BREAK)
//                    }
                    KevlarComponents.PHYS_WORLD.get(MinecraftClient.getInstance().server!!.overworld).explosions.add(pos)

                    flags = flags or BulletPhysicsGlobals.FLAG_MARKED_FOR_DELETION
                }
                body.flags = flags - BulletPhysicsGlobals.FLAG_THROWN_BY_GRAVGUN
            }
        }
    }
}