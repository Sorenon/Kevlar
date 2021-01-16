package net.sorenon.kevlar

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import net.minecraft.block.BlockState
import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShape
import net.sorenon.kevlar.init.KevlarMod

object BulletPhysicsGlobals {
    const val FLAG_SERVERSIDE          = 0x1
    const val FLAG_MARKED_FOR_DELETION = 0x2
    const val FLAG_THROWN_BY_GRAVGUN   = 0x4

    val matrix4 = Matrix4()

    val blockShapes = hashMapOf<BlockState, btCollisionShape?>()
    val boxShapes = hashMapOf<Vector3, btBoxShape>()

    val activateAABBCallback = object : btBroadphaseAabbCallback() {
        override fun process(proxy: btBroadphaseProxy): Boolean {
            val obj = btCollisionObject.getInstance(proxy.clientObject)
            obj.activate()
            return true
        }
    }

    fun getOrMakeBoxShape(box: Box): btBoxShape {
        return getOrMakeBoxShape(
            Vector3(
                (box.xLength / 2).toFloat(),
                (box.yLength / 2).toFloat(),
                (box.zLength / 2).toFloat()
            )
        )
    }

    fun getOrMakeBoxShape(boxHalfExtents: Vector3): btBoxShape {
        return boxShapes.getOrPut(
            boxHalfExtents, {
                btBoxShape(
                    boxHalfExtents
                )
            }
        )
    }

    fun getOrMakeBlockShape(blockState: BlockState): btCollisionShape? {
        return blockShapes.getOrPut(blockState, {
            val voxelShape = tryGetCollisionShape(blockState) ?: return@getOrPut null
            val boxes = voxelShape.boundingBoxes
            if (boxes.size == 1) {
                getOrMakeBoxShape(boxes[0])
            } else {
                val shape = btCompoundShape()
                for (box in boxes) {
                    val boxShape = getOrMakeBoxShape(box)
                    val center = box.center
                    matrix4.set(
                        center.x.toFloat(), center.y.toFloat(), center.z.toFloat(),
                        0f, 0f, 0f, 1f
                    )
                    shape.addChildShape(matrix4, boxShape)
                }
                shape
            }
        })
    }

    fun tryGetCollisionShape(blockState: BlockState): VoxelShape? {
        return try {
            blockState.getCollisionShape(null, null)
        } catch (e: Exception) {
            KevlarMod.LOGGER.error("Support for dynamic colliders not implemented $blockState")
            null
        }
    }
}