package net.sorenon.kevlar.item

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback
import com.badlogic.gdx.physics.bullet.dynamics.btFixedConstraint
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World
import net.sorenon.kevlar.vec
import net.sorenon.kevlar.init.KevlarComponents
import kotlin.math.max

class PhysGun(settings: Settings) : Item(settings) {

    var distance = 1.5
    lateinit var grabBody: btRigidBody
    var otherBody: btRigidBody? = null
    var constraint: btFixedConstraint? = null

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!world.isClient) {
            val phys = KevlarComponents.PHYS_WORLD.get(world)
            val cameraPos = user.getCameraPosVec(1.0f)
            val look = user.getRotationVec(1.0f)
            val lookDist = 20
            val result = ClosestRayResultCallback(vec(cameraPos), vec(cameraPos.add(look.x * lookDist, look.y * lookDist, look.z * lookDist)))
            phys.dynamicsWorld.rayTest(
                vec(cameraPos),
                vec(cameraPos.add(look.x * lookDist, look.y * lookDist, look.z * lookDist)),
                result
            )
            val rBody = result.collisionObject
            if (rBody !is btRigidBody || rBody.isStaticObject) {
                return super.use(world, user, hand)
            }
            otherBody = rBody

            val centerPos = rBody.worldTransform.getTranslation(Vector3())

            val trans = Matrix4()
            trans.setToTranslation(centerPos)
            distance = cameraPos.distanceTo(vec(centerPos))

            grabBody.worldTransform = trans
            constraint = btFixedConstraint(rBody, grabBody, Matrix4(), Matrix4())
            phys.dynamicsWorld.addRigidBody(grabBody)
            phys.dynamicsWorld.addConstraint(constraint)
        }

        user.setCurrentHand(hand)
        return TypedActionResult.consume(user.activeItem)
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if (!world.isClient && otherBody?.isDisposed == false) {
            distance = max(1.0, distance)

            val transform = Matrix4()

            val cameraPos = user.getCameraPosVec(1.0f)
            val look = user.getRotationVec(1.0f)
            val pos = cameraPos.add(look.x * distance, look.y * distance, look.z * distance)

            transform.setTranslation(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())

            grabBody.worldTransform = transform
            grabBody.activate()
            otherBody!!.activate()
        }
    }

    override fun getMaxUseTime(stack: ItemStack?): Int {
        return 72000
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BOW
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        if (!world.isClient && otherBody?.isDisposed == false) {
            val phys = KevlarComponents.PHYS_WORLD.get(world)
            phys.dynamicsWorld.removeConstraint(constraint)
            phys.dynamicsWorld.removeRigidBody(grabBody)
            constraint!!.dispose()
            constraint = null
            otherBody = null
        }
    }
}