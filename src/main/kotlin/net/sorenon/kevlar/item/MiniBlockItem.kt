package net.sorenon.kevlar.item

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.sorenon.kevlar.BulletPhysicsGlobals
import net.sorenon.kevlar.init.KevlarComponents
import net.sorenon.kevlar.shapes.BoxShapeWrapper
import net.sorenon.kevlar.shapes.SphereShapeWrapper

class MiniBlockItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val pos = context.blockPos.offset(context.side)
        val phys = KevlarComponents.PHYS_WORLD.get(context.world)
        val halfExtents = Vector3(0.1f, 0.1f, 0.1f)
        val shape = BulletPhysicsGlobals.getOrMakeBoxShape(halfExtents)

        val trans = Matrix4()
        trans.setToTranslation(pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f)
        val fallMotionState = btDefaultMotionState(trans)
        val mass = 1f
        val fallInertia = Vector3(0f, 0f, 0f)
        shape.calculateLocalInertia(mass, fallInertia)
        val fallRigidBodyCI = btRigidBody.btRigidBodyConstructionInfo(mass, fallMotionState, shape, fallInertia)
        val block = btRigidBody(fallRigidBodyCI)
        block.restitution = 0.1f
        phys.addRegisteredRigidBody(block, mass, BoxShapeWrapper(halfExtents))
        return ActionResult.SUCCESS
    }
}