package net.sorenon.kevlar.item

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World
import net.sorenon.kevlar.BulletPhysicsGlobals
import net.sorenon.kevlar.init.KevlarComponents
import net.sorenon.kevlar.init.KevlarMod
import net.sorenon.kevlar.networking.EntityGrabRigidBodyS2CPacket
import net.sorenon.kevlar.vec

class GravityGun(settings: Settings) : Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!world.isClient) {
            val phys = KevlarComponents.PHYS_WORLD.get(world)
            val cameraPos = user.getCameraPosVec(1.0f)
            val look = user.getRotationVec(1.0f)
            val lookDist = 20
            val result = ClosestRayResultCallback(
                vec(cameraPos),
                vec(cameraPos.add(look.x * lookDist, look.y * lookDist, look.z * lookDist))
            )
            phys.dynamicsWorld.rayTest(
                vec(cameraPos),
                vec(cameraPos.add(look.x * lookDist, look.y * lookDist, look.z * lookDist)),
                result
            )

//            val hitPos = Vector3()
//            result.getHitPointWorld(hitPos)
//            println(hitPos)


            val rBody = result.collisionObject
            if (rBody !is btRigidBody || rBody.isStaticObject) {
                return super.use(world, user, hand)
            }
            KevlarComponents.GRABBER.get(user).grabRigidBody(rBody)
            KevlarComponents.GRABBER.get(user).distance = 1.2
            val buf = PacketByteBufs.create()
            val packet = EntityGrabRigidBodyS2CPacket(user.entityId, rBody.userValue.toShort())
            packet.serialize(buf)
            PlayerLookup.world(world as ServerWorld).forEach {
                ServerPlayNetworking.send(it, KevlarMod.S2C_GRAB_RB, buf)
            }
        }

        user.setCurrentHand(hand)
        return TypedActionResult.consume(user.activeItem)
    }

    override fun getMaxUseTime(stack: ItemStack?): Int {
        return 72000
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BOW
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        val grab = KevlarComponents.GRABBER.get(user)
        val heldBody = grab.otherBody
        grab.drop()

        if (heldBody?.isDisposed == false) {
            heldBody.applyCentralImpulse(vec(user.getRotationVec(1.0f)).scl(200f))
            heldBody.applyTorqueImpulse(Vector3(1f, 0f, 0f))
            heldBody.flags = heldBody.flags or BulletPhysicsGlobals.FLAG_THROWN_BY_GRAVGUN
        }
    }
}