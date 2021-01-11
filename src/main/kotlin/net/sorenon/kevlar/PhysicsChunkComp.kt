package net.sorenon.kevlar

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentV3
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk

class PhysicsChunkComp(val chunk: Chunk): ComponentV3, Component {
    val blockCollisionObjects = hashMapOf<BlockPos, btCollisionObject>()

    override fun readFromNbt(p0: CompoundTag) {

    }

    override fun writeToNbt(p0: CompoundTag) {

    }
}