package net.sorenon.kevlar

import com.badlogic.gdx.physics.bullet.collision.btBroadphaseAabbCallback
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody

class CollectRigidBodiesAABBCallback: btBroadphaseAabbCallback() {

    val rigidBodies = arrayListOf<btRigidBody>()

    override fun process(proxy: btBroadphaseProxy): Boolean {
        val obj = btCollisionObject.getInstance(proxy.clientObject)
        if (obj is btRigidBody) {
            rigidBodies.add(obj)
        }
        return true
    }
}