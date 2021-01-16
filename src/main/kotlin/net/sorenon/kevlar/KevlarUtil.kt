package net.sorenon.kevlar

import com.badlogic.gdx.math.Vector3
import net.minecraft.network.PacketByteBuf
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

fun writeVec(vec: Vector3, buf: PacketByteBuf) {
    buf.writeFloat(vec.x)
    buf.writeFloat(vec.y)
    buf.writeFloat(vec.z)
}

fun readVec(buf: PacketByteBuf): Vector3 {
    return Vector3(buf.readFloat(), buf.readFloat(), buf.readFloat())
}