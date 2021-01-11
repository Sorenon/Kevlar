package net.sorenon.kevlar

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.InternalTickCallback
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.chunk.Chunk
import kotlin.math.max
import kotlin.math.min

class PhysicsTickCallback(val dynamicsWorld: btDynamicsWorld, isPreTick: Boolean) :
    InternalTickCallback(dynamicsWorld, isPreTick) {

    private val matrix4 = Matrix4()
    private var aabbMax = Vector3()
    private var aabbMin = Vector3()
    val blockCollisionObjects = hashMapOf<BlockPos, btCollisionObject>()
    val blockShapes = hashMapOf<BlockState, btCollisionShape>()
    val boxShapeCache = hashMapOf<Vector3, btBoxShape>()

    val aabbCallback = object : btBroadphaseAabbCallback() {
        override fun process(proxy: btBroadphaseProxy): Boolean {
            val obj = btCollisionObject.getInstance(proxy.clientObject)
            obj.activate()
            return true //Return value is unused
        }
    }

    override fun onInternalTick(dynamicsWorld: btDynamicsWorld, timeStep: Float) {
        val arr = dynamicsWorld.collisionObjectArrayConst
        for (i in 0 until arr.size()) {
            val colObj = arr.atConst(i)
            if (colObj.isStaticObject || !colObj.isActive) {
                continue
            }

            colObj.getWorldTransform(matrix4)
            //Set aabb to colObj
            colObj.collisionShape.getAabb(matrix4, aabbMin, aabbMax)

            val out = 0.5
            val startPos: BlockPos.Mutable = BlockPos.Mutable(
                aabbMin.x - out,
                aabbMin.y - out,
                aabbMin.z - out
            )
            val endPos: BlockPos.Mutable = BlockPos.Mutable(
                aabbMax.x + out,
                aabbMax.y + out,
                aabbMax.z + out
            )
            val scanPos: BlockPos.Mutable = BlockPos.Mutable()

            val world = MinecraftClient.getInstance().world!!

            if (world.isRegionLoaded(startPos, endPos)) {
                for (x in startPos.x..endPos.x) {
                    for (y in startPos.y..endPos.y) {
                        for (z in startPos.z..endPos.z) {
                            scanPos.set(x, y, z)
                            var block = blockCollisionObjects[scanPos]
                            if (block == null) {
                                val chunk: Chunk = world.getChunk(scanPos)
                                val iblockstate = chunk.getBlockState(scanPos)
                                val blockShape = iblockstate.getCollisionShape(world, scanPos)
                                if (!blockShape.isEmpty) {
                                    block = makeBlock(scanPos, blockShape, iblockstate)

                                    dynamicsWorld.addCollisionObject(block)

                                    blockCollisionObjects[scanPos.toImmutable()] = block
                                }
                            } else {
                                block.userValue = 20
                            }
                        }
                    }
                }
            }
        }

        blockCollisionObjects.entries.removeAll { blockPair: MutableMap.MutableEntry<BlockPos, btCollisionObject> ->
            if (blockPair.value.userValue-- < 0) {
                dynamicsWorld.removeCollisionObject(blockPair.value)
                blockPair.value.dispose()
                true
            } else {
                false
            }
        }
    }

    //    private val blockShape = btBoxShape(Vector3(0.5f, 0.5f, 0.5f))
    private fun makeBlock(pos: BlockPos, voxelShape: VoxelShape, state: BlockState): btCollisionObject {
        val obj = btCollisionObject()
        val blockAABB = voxelShape.boundingBox
        val shape = blockShapes.getOrPut(state, {
            val boxes = voxelShape.boundingBoxes
            if (boxes.size == 1) {
                getOrMakeBoxShape(blockAABB)
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

        if (shape !is btCompoundShape) {
            val center = blockAABB.center
            matrix4.set(
                pos.x + center.x.toFloat(), pos.y + center.y.toFloat(), pos.z + center.z.toFloat(),
                0f, 0f, 0f, 1f
            )
        } else {
            matrix4.set(
                pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(),
                0f, 0f, 0f, 1f
            )
        }

        obj.worldTransform = matrix4
        obj.collisionShape = shape
        obj.restitution = 0.1f
        obj.userValue = 20
        return obj
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
        return boxShapeCache.getOrPut(
            boxHalfExtents, {
                btBoxShape(
                    boxHalfExtents
                )
            }
        )
    }

    fun onChangeBlockState(pos: BlockPos, state: BlockState) {
        val aabbMinNew = Vector3(pos.x + 0f, pos.y + 0f, pos.z + 0f)
        val aabbMaxNew = Vector3(pos.x + 1f, pos.y + 1f, pos.z + 1f)

        val block = blockCollisionObjects[pos]
        if (block != null) {
            block.collisionShape.getAabb(matrix4, aabbMin, aabbMax)
            aabbMax.x = max(aabbMax.x, aabbMaxNew.x)
            aabbMax.y = max(aabbMax.y, aabbMaxNew.y)
            aabbMax.z = max(aabbMax.z, aabbMaxNew.z)

            aabbMin.x = min(aabbMin.x, aabbMinNew.x)
            aabbMin.y = min(aabbMin.y, aabbMinNew.y)
            aabbMin.z = min(aabbMin.z, aabbMinNew.z)

            dynamicsWorld.removeCollisionObject(block)
            blockCollisionObjects.remove(pos)
            block.userValue = -1 //Set block for disposal
        } else {
            aabbMax = aabbMaxNew
            aabbMin = aabbMinNew
        }

        dynamicsWorld.broadphase.aabbTest(aabbMin, aabbMax, aabbCallback)
    }
}