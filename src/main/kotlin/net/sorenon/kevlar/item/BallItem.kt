package net.sorenon.kevlar.item

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.sorenon.kevlar.init.KevlarComponents
import net.sorenon.kevlar.shapes.CylinderShapeWrapper
import net.sorenon.kevlar.shapes.SphereShapeWrapper
import kotlin.math.PI

class BallItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (!context.world.isClient) {
            val pos = context.blockPos.offset(context.side)
            val phys = KevlarComponents.PHYS_WORLD.get(context.world)

            val trans = Matrix4()
            trans.setToTranslation(pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f)

            phys.addRegisteredRigidBody2(trans, 10f, SphereShapeWrapper(0.5f))
        }
        return ActionResult.SUCCESS
    }
}