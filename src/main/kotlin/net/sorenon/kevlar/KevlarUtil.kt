package net.sorenon.kevlar

import com.badlogic.gdx.math.Vector3
import net.minecraft.util.math.Vec3d
import net.sorenon.kevlar.init.KevlarMod

fun vec(mcVec: Vec3d): Vector3 {
    return Vector3(mcVec.x.toFloat(), mcVec.y.toFloat(), mcVec.z.toFloat())
}

fun vec(gdxVec: Vector3): Vec3d {
    return Vec3d(gdxVec.x.toDouble(), gdxVec.y.toDouble(), gdxVec.z.toDouble())
}

fun println(message: Any? = null) {
    KevlarMod.LOGGER.info(message)
}