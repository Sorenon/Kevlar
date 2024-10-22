package net.sorenon.kevlar.item

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.sorenon.kevlar.entity.ExplosiveBarrelEntity
import net.sorenon.kevlar.init.KevlarComponents
import net.sorenon.kevlar.init.KevlarMod
import net.sorenon.kevlar.shapes.CylinderShapeWrapper

class BarrelItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (!context.world.isClient) {
            val pos = context.blockPos.offset(context.side)
//            val phys = KevlarComponents.PHYS_WORLD.get(context.world)

            val h = 18.0 / 16

//            val trans = Matrix4()
//            trans.setToTranslation(pos.x + 0.5f, pos.y + h / 2, pos.z + 0.5f)
//
//            phys.addRegisteredRigidBody2(trans, 10f, CylinderShapeWrapper(Vector3(w / 2, h / 2, w / 2)))
            val barrelEntity = ExplosiveBarrelEntity(KevlarMod.BARREL_ENTITY, context.world)
            barrelEntity.setPos(pos.x + 0.5, pos.y + h / 2, pos.z + 0.5)
            context.world.spawnEntity(barrelEntity)
        }
        return ActionResult.SUCCESS
    }
}