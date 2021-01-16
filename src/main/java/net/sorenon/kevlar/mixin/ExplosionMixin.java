package net.sorenon.kevlar.mixin;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.sorenon.kevlar.CollectRigidBodiesAABBCallback;
import net.sorenon.kevlar.PhysicsWorldComponent;
import net.sorenon.kevlar.init.KevlarComponents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private double x;

    @Shadow
    @Final
    private double y;

    @Shadow
    @Final
    private double z;

    @Shadow
    @Final
    private float power;

    @Inject(at = @At("RETURN"), method = "collectBlocksAndDamageEntities")
    void damagePhysics(CallbackInfo ci) {
        PhysicsWorldComponent phys = KevlarComponents.getPHYS_WORLD().get(world);

        Vector3 explosionPos = new Vector3((float) x, (float) y, (float) z);
//        Vector3 posO = new Vector3((float) x, (float) y - 0.4f, (float) z);

        float q = this.power * 2.0F;
        int x1 = MathHelper.floor(this.x - (double) q - 1.0D);
        int x2 = MathHelper.floor(this.x + (double) q + 1.0D);
        int y1 = MathHelper.floor(this.y - (double) q - 1.0D);
        int y2 = MathHelper.floor(this.y + (double) q + 1.0D);
        int z1 = MathHelper.floor(this.z - (double) q - 1.0D);
        int z2 = MathHelper.floor(this.z + (double) q + 1.0D);
        Vector3 min = new Vector3(x1, y1, z1);
        Vector3 max = new Vector3(x2, y2, z2);


        CollectRigidBodiesAABBCallback callback = new CollectRigidBodiesAABBCallback();
        phys.getBroadphase().aabbTest(min, max, callback);

        Matrix4 explosionTransform = new Matrix4();
        explosionTransform.setToTranslation(explosionPos);

        Matrix4 rbTransform = new Matrix4();

        for (btRigidBody rb : callback.getRigidBodies()) {
            System.out.println(rb.getUserValue());
            rb.getWorldTransform(rbTransform);

            Vector3 rbPos = rbTransform.getTranslation(new Vector3());
            ClosestRayResultCallback result = new ClosestRayResultCallback(explosionPos, rbPos);


            Matrix4 transFrom = new Matrix4();
            Matrix4 transTo = new Matrix4();
            transFrom.setToTranslation(explosionPos);
            transTo.setToTranslation(rbPos);
            Vector3 rayDir = rbPos.cpy().sub(explosionPos).add(0.05f, 0.1f, 0.05f).nor(); //Sub 0.2 from y to induce spin and to kick things up

            System.out.println(rb.getWorldTransform().getTranslation(new Vector3()));
            System.out.println(rbTransform.getTranslation(new Vector3()));

            System.out.println("E Pos" + explosionPos);
            System.out.println("E Dir" + rbPos.cpy().sub(explosionPos).nor());
            System.out.println("E Dir" + rayDir);

            btCollisionWorld.rayTestSingle(
                    transFrom,
                    transTo,
                    rb,
                    rb.getCollisionShape(),
                    rbTransform,
                    result);


            System.out.println(result.hasHit());
            Vector3 hitPos = new Vector3();
            result.getHitPointWorld(hitPos);
            System.out.println(hitPos);
            phys.setLastPos(hitPos.cpy());

            hitPos.sub(rbPos);
            System.out.println(hitPos);
            System.out.println(rbPos.cpy().add(hitPos));

            rb.activate();
            rb.applyImpulse(rayDir.scl(190f), hitPos);
        }
    }
}
