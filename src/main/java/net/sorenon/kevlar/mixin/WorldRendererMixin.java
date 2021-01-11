package net.sorenon.kevlar.mixin;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectArray;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.monarkhēs.myron.api.Myron;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.sorenon.kevlar.PhysicsWorldComponent;
import net.sorenon.kevlar.init.KevlarComponents;
import net.sorenon.kevlar.init.KevlarModClient;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin {
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private ClientWorld world;

    @Unique
    private long time = System.currentTimeMillis();

    @Inject(at = @At("HEAD"), method = "render")
    public void tickPhysics(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        long newTime = System.currentTimeMillis();
        PhysicsWorldComponent phys = KevlarComponents.getPHYS_WORLD().get(world);
        phys.getDynamicsWorld().stepSimulation((newTime - time) / 1000.0f, 10);
        time = newTime;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw(Lnet/minecraft/client/render/RenderLayer;)V", ordinal = 0), method = "render")
    public void postRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
//        if (true)return;
        VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();

        Vec3d vec3d = camera.getPos();
        double d = vec3d.getX();
        double e = vec3d.getY();
        double f = vec3d.getZ();

        PhysicsWorldComponent phys = KevlarComponents.getPHYS_WORLD().get(world);

        matrices.push();
        matrices.translate(-d, -e, -f);
//        RenderSystem.pushMatrix();
//        RenderSystem.multMatrix(matrices.peek().getModel());
//        BufferBuilder buffer = KevlarModClient.Companion.getDrawer().getBuffer();
//        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
//        phys.getDynamicsWorld().debugDrawWorld();
//        buffer.end();
//
//        RenderSystem.disableTexture();
//        RenderSystem.enableDepthTest();
//        BufferRenderer.draw(buffer);
//        RenderSystem.disableDepthTest();
//        RenderSystem.enableTexture();
//
//        RenderSystem.popMatrix();

        btCollisionObjectArray arr = phys.getDynamicsWorld().getCollisionObjectArrayConst();
        BakedModel model = Myron.getModel(new Identifier("kevlar", "models/misc/sphere"));

        for (int i = 0; i < arr.size(); i++) {
            btCollisionObject obj = arr.atConst(i);
            if (obj.getCollisionShape() == phys.getBallShape() && obj instanceof btRigidBody) {
                matrices.push();
                Matrix4 trans = new Matrix4();
                ((btRigidBody) obj).getMotionState().getWorldTransform(trans);
                Quaternion quat = trans.getRotation(new Quaternion());
                Vector3 pos = trans.getTranslation(new Vector3());
                BlockPos blockPos = new BlockPos(pos.x, pos.y, pos.z);
                matrices.translate(pos.x, pos.y, pos.z);
                matrices.multiply(new net.minecraft.util.math.Quaternion(quat.x, quat.y, quat.z, quat.w));
//                matrices.translate(-0.5D, -0.5d, -0.5D);
//                BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
//                blockRenderManager.getModelRenderer().render(MinecraftClient.getInstance().world, blockRenderManager.getModel(Blocks.OAK_LOG.getDefaultState()), Blocks.OAK_LOG.getDefaultState(), blockPos, matrices, immediate.getBuffer(RenderLayers.getMovingBlockLayer(Blocks.OAK_LOG.getDefaultState())), false, new Random(), 0, OverlayTexture.DEFAULT_UV);

                VertexConsumer consumer = immediate.getBuffer(RenderLayer.getSolid());
                int light = LightmapTextureManager.pack(world.getLightLevel(LightType.BLOCK, blockPos), world.getLightLevel(LightType.SKY, blockPos));
                MatrixStack.Entry entry = matrices.peek();
                model.getQuads(null, null, new Random()).forEach(quad -> consumer.quad(entry, quad, 1F, 1F, 1F, light, OverlayTexture.DEFAULT_UV));

                matrices.pop();
            }
        }
        matrices.pop();
    }
}
