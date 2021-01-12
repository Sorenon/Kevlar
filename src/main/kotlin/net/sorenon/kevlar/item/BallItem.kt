package net.sorenon.kevlar.item

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.sorenon.kevlar.init.KevlarComponents

class BallItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
//        if (!context.world.isClient) {
            val pos = context.blockPos.offset(context.side)
            val phys = KevlarComponents.PHYS_WORLD.get(context.world)

            val trans = Matrix4()
            trans.setToTranslation(pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f)
            val fallMotionState = btDefaultMotionState(trans)
            val mass = 1f
            val fallInertia = Vector3(0f, 0f, 0f)
            phys.ballShape.calculateLocalInertia(mass, fallInertia)
            val fallRigidBodyCI = btRigidBody.btRigidBodyConstructionInfo(mass, fallMotionState, phys.ballShape, fallInertia)
            val ball = btRigidBody(fallRigidBodyCI)
            ball.restitution = 0.1f
            phys.addRegisteredRigidBody(ball)
//        }
        return ActionResult.SUCCESS
    }
}