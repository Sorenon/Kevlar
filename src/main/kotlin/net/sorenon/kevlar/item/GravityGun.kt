package net.sorenon.kevlar.item

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.sorenon.kevlar.init.KevlarModClient

class GravityGun(settings: Settings) : Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
//        if (world.isClient) {
//            val transform = Matrix4()
//            val ball = KevlarModClient.phys.ball
////            ball.getWorldTransform(transform)
//
//            val distance = 1.5;
//            val cameraPos: Vec3d = user.getCameraPosVec(1.0f)
//            val look: Vec3d = user.getRotationVec(1.0f)
//            val pos = cameraPos.add(look.x * distance, look.y * distance, look.z * distance)
//
//            transform.setTranslation(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
//
//            KevlarModClient.phys.playerGrab.worldTransform = transform
//            KevlarModClient.phys.playerGrab.activate()
//            ball.activate()
////            ball.worldTransform = transform
////            ball.linearVelocity = Vector3.Zero
////            ball.angularVelocity = Vector3.Zero
////            ball.activate()
//        }
        return super.use(world, user, hand)
    }
}