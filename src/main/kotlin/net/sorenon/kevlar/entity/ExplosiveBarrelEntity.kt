package net.sorenon.kevlar.entity

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import net.sorenon.kevlar.BulletPhysicsGlobals
import net.sorenon.kevlar.init.KevlarComponents
import net.sorenon.kevlar.init.KevlarMod
import net.sorenon.kevlar.shapes.CylinderShapeWrapper
import net.sorenon.kevlar.vec

class ExplosiveBarrelEntity(type: EntityType<*>, world: World) : Entity(type, world) {
    companion object {
        val RIGID_BODY_ID: TrackedData<Int> =
            DataTracker.registerData(ExplosiveBarrelEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
    }

    var health = 20f

    override fun initDataTracker() {
        dataTracker.startTracking(RIGID_BODY_ID, -1)
    }

    override fun readCustomDataFromTag(tag: CompoundTag) {

    }

    override fun writeCustomDataToTag(tag: CompoundTag) {

    }

    fun getRigidBodyID(): Int {
        return dataTracker.get(RIGID_BODY_ID)
    }

    override fun tick() {
        super.tick()
        val phys = KevlarComponents.PHYS_WORLD.get(world)
        val rbID = getRigidBodyID()
        if (rbID == -1) {
            if (!world.isClient) {
                val w = 14f / 16f
                val h = /*18f / 16f*/ 1f
                val trans = Matrix4()
                trans.setToTranslation(vec(this.pos))
                dataTracker.set(
                    RIGID_BODY_ID,
                    phys.addRegisteredRigidBody2(
                        trans,
                        10f,
                        CylinderShapeWrapper(Vector3(w / 2, h / 2, w / 2))
                    ).userValue
                )
            }
        } else {
            val rb = phys.registeredRigidBodies[rbID.toShort()]
            if (rb?.isDisposed == false) {
                val trans = Matrix4()
                rb.getWorldTransform(trans)
                val rbPos = trans.getTranslation(Vector3())
                setPos(rbPos.x.toDouble(), rbPos.y.toDouble(), rbPos.z.toDouble())
                val min = Vector3()
                val max = Vector3()
                rb.getAabb(min, max)
                boundingBox = Box(vec(min), vec(max))
            }
        }
        if (!world.isClient) {
            if (this.isOnFire) {
                damage(DamageSource.ON_FIRE, 2.5f / 20)
            }
        }
    }

    override fun damage(source: DamageSource, amountIn: Float): Boolean {
        if (isInvulnerableTo(source) || world.isClient || this.removed) {
            return false
        }
        var amount = amountIn
        if (source.isExplosive) {
            if (this.isOnFire) {
                amount *= 0.01f
            } else {
                amount *= 0.35f
            }
            super.setFireTicks(1)
        }

        health -= amount
        if (health <= 0) {
            this.remove()
            world.createExplosion(this, x, y, z, 4.0f, Explosion.DestructionType.BREAK)
        } else if (health <= 12) {
            super.setFireTicks(1)
        }
        return true
    }

    override fun setFireTicks(ticks: Int) {
        if (ticks > 0) {
            super.setFireTicks(ticks)
        }
    }

    override fun extinguish() {

    }

    override fun move(type: MovementType, movement: Vec3d) {

    }

    override fun moveToBoundingBoxCenter() {

    }

    override fun collides(): Boolean {
        return true
    }

    override fun pushAwayFrom(entity: Entity) {

    }

    override fun isPushable(): Boolean {
        return true
    }

    override fun remove() {
        super.remove()
        if (!world.isClient) {
            val phys = KevlarComponents.PHYS_WORLD.get(world)
            val rbID = getRigidBodyID()
            val rb = phys.registeredRigidBodies[rbID.toShort()]
            rb?.flags = rb!!.flags or BulletPhysicsGlobals.FLAG_MARKED_FOR_DELETION
        }
    }

    override fun createSpawnPacket(): Packet<*> {
        val buf = PacketByteBufs.create()
        EntitySpawnS2CPacket(this).write(buf)
        return ServerPlayNetworking.createS2CPacket(KevlarMod.S2C_SPAWN_ENTITY, buf)
    }
}