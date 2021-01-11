package net.sorenon.kevlar

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import net.minecraft.client.render.BufferBuilder

class PhysDebugDrawer: btIDebugDraw() {

    val buffer = BufferBuilder(256)

    override fun drawLine(v1: Vector3, v2: Vector3, color: Vector3) {
        buffer.vertex(v1.x.toDouble(), v1.y.toDouble(), v1.z.toDouble()).color(color.x, color.y, color.z, 1.0f).next()
        buffer.vertex(v2.x.toDouble(), v2.y.toDouble(), v2.z.toDouble()).color(color.x, color.y, color.z, 1.0f).next()
    }

    override fun getDebugMode(): Int {
        return DebugDrawModes.DBG_DrawWireframe /*or DebugDrawModes.DBG_DrawAabb*/
    }
}