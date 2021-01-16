package net.sorenon.kevlar

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.InternalTickCallback
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.explosion.Explosion
import net.sorenon.kevlar.init.KevlarMod
import kotlin.math.max
import kotlin.math.min

class PhysicsTickCallback(val phys: PhysicsWorldComponent, isPreTick: Boolean) :
    InternalTickCallback(phys.dynamicsWorld, isPreTick) {

    private val matrix4 = Matrix4()
    private var aabbMax = Vector3()
    private var aabbMin = Vector3()
    val blockCollisionObjects = hashMapOf<BlockPos, btCollisionObject>()

    override fun onInternalTick(dynamicsWorld: btDynamicsWorld, timeStep: Float) {

        phys.registeredRigidBodies.entries.removeIf { rbEntry ->
            val rigidBody = rbEntry.value
            if (rigidBody.isStaticOrKinematicObject || !rigidBody.isActive) {
                return@removeIf false
            }

            if (rigidBody.flags and BulletPhysicsGlobals.FLAG_MARKED_FOR_DELETION != 0) {
                phys.dynamicsWorld.removeRigidBody(rigidBody)
                rigidBody.dispose()
                val buf = PacketByteBufs.create()
                buf.writeShort(rbEntry.key.toInt())
                PlayerLookup.world(phys.world as ServerWorld).forEach {
                    ServerPlayNetworking.send(it, KevlarMod.S2C_REMOVE_RB, buf)
                }
                return@removeIf true
            }

            rigidBody.getWorldTransform(matrix4)
            rigidBody.collisionShape.getAabb(matrix4, aabbMin, aabbMax)

            val out = 1.0
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

            val world = phys.world

            if (world.isRegionLoaded(startPos, endPos)) {
                for (x in startPos.x..endPos.x) {
                    for (y in startPos.y..endPos.y) {
                        for (z in startPos.z..endPos.z) {
                            scanPos.set(x, y, z)
                            var block = blockCollisionObjects[scanPos]
                            if (block == null) {
                                val chunk: Chunk = world.getChunk(scanPos)
                                val blockState = chunk.getBlockState(scanPos)
                                if (BulletPhysicsGlobals.tryGetCollisionShape(blockState)?.isEmpty == false) {
                                    block = makeBlock(scanPos, blockState)
                                    if (block != null) {
                                        dynamicsWorld.addCollisionObject(block)
                                        blockCollisionObjects[scanPos.toImmutable()] = block
                                    }
                                }
                            } else {
                                block.userValue = 20
                            }
                        }
                    }
                }
            }
            return@removeIf false
        }

        for (pos in phys.explosions) {
            phys.world.createExplosion(null, pos.x, pos.y, pos.z, 4.0f, Explosion.DestructionType.BREAK)
        }
        phys.explosions.clear()

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

    private fun makeBlock(pos: BlockPos, state: BlockState): btCollisionObject? {
        val shape = BulletPhysicsGlobals.getOrMakeBlockShape(state) ?: return null

        val obj = btCollisionObject()
        if (shape !is btCompoundShape) {
            val center = BulletPhysicsGlobals.tryGetCollisionShape(state)!!.boundingBox.center
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

            phys.dynamicsWorld.removeCollisionObject(block)
            blockCollisionObjects.remove(pos)
            block.dispose()
//            if (!phys.world.isClient) {
//                println("$pos   ${phys.world.getBlockState(pos)}")
//            }
//            block.userValue = -1 //Set block for disposal
        } else {
            aabbMax = aabbMaxNew
            aabbMin = aabbMinNew
        }

        phys.dynamicsWorld.broadphase.aabbTest(aabbMin, aabbMax, BulletPhysicsGlobals.activateAABBCallback)
    }
}