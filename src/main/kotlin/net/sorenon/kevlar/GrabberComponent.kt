package net.sorenon.kevlar

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btEmptyShape
import com.badlogic.gdx.physics.bullet.dynamics.btFixedConstraint
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentV3
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.sorenon.kevlar.init.KevlarComponents
import kotlin.math.max

class GrabberComponent(val player: PlayerEntity) : ComponentV3, Component, PlayerComponent<GrabberComponent>,
    AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {

    var distance = 0.5
    val grabBody: btRigidBody = btRigidBody(0f, btDefaultMotionState(), btEmptyShape())
    val trans = Matrix4()
    var savedMass = -1f

    var otherBody: btRigidBody? = null
    var constraint: btFixedConstraint? = null

    override fun readFromNbt(p0: CompoundTag) {
        distance = p0.getDouble("distance")
    }

    override fun writeToNbt(p0: CompoundTag) {
        p0.putDouble("distance", distance)
    }

    override fun clientTick() {
        tick()
    }

    override fun serverTick() {
        tick()
    }

    fun grabRigidBody(rigidBody: btRigidBody) {
        val phys = KevlarComponents.PHYS_WORLD.get(player.world)
        val cameraPos = player.getCameraPosVec(1.0f)

        otherBody = rigidBody

        val centerPos = rigidBody.worldTransform.getTranslation(Vector3())

        trans.setToTranslation(centerPos)
        distance = cameraPos.distanceTo(vec(centerPos))

        grabBody.worldTransform = trans
        constraint = btFixedConstraint(rigidBody, grabBody, Matrix4(), Matrix4())
        phys.dynamicsWorld.addRigidBody(grabBody)
        phys.dynamicsWorld.addConstraint(constraint)

        savedMass = 1 / rigidBody.invMass
        val smallerMass = 1f
        val interia = Vector3()
        rigidBody.collisionShape.calculateLocalInertia(smallerMass, interia)
        rigidBody.setMassProps(smallerMass, interia)
    }

    private fun tick() {
        if (isHoldingRigidBody() && (player is ServerPlayerEntity || player is ClientPlayerEntity)) {
            distance = max(0.5, distance)

            val cameraPos = player.getCameraPosVec(1.0f)
            val look = player.getRotationVec(1.0f)
            val pos = cameraPos.add(look.x * distance, look.y * distance, look.z * distance)

            trans.setToTranslation(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())

            grabBody.worldTransform = trans
            grabBody.activate()
            otherBody!!.activate()
        }
    }

    fun drop() {
        if (isHoldingRigidBody()) {
            val phys = KevlarComponents.PHYS_WORLD.get(player.world)

            phys.dynamicsWorld.removeRigidBody(otherBody!!)
            val interia = Vector3()
            otherBody!!.collisionShape.calculateLocalInertia(savedMass, interia)
            otherBody!!.setMassProps(savedMass, interia)
            phys.dynamicsWorld.addRigidBody(otherBody!!)

            phys.dynamicsWorld.removeConstraint(constraint)
            phys.dynamicsWorld.removeRigidBody(grabBody)
            constraint!!.dispose()
            constraint = null
            otherBody = null
        } else {
//            KevlarMod.LOGGER.warn("$player Tried dropping a rigidbody that doesn't exist")
        }
    }

    fun isHoldingRigidBody(): Boolean {
        return if (otherBody?.isDisposed == false) {
            true
        } else {
            if (constraint?.isDisposed == false) {
                KevlarComponents.PHYS_WORLD.get(player.world).dynamicsWorld.removeConstraint(constraint)
                constraint!!.dispose()
                constraint = null
            }
            false
        }
    }
}