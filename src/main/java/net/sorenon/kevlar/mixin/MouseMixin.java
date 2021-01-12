package net.sorenon.kevlar.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.sorenon.kevlar.init.KevlarMod;
import net.sorenon.kevlar.item.PhysGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;eventDeltaWheel:D", ordinal = 0), method = "onMouseScroll", cancellable = true)
    void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        assert player != null;
        if (player.getActiveItem().getItem() instanceof PhysGun) {
//            KevlarMod.Companion.getPHYS_GUN_ITEM().setDistance(KevlarMod.Companion.getPHYS_GUN_ITEM().getDistance() + vertical);
            ci.cancel();
        }
    }
}
