package net.sorenon.kevlar.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.sorenon.kevlar.init.KevlarModClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Shadow private long debugCrashStartTime;

    @Shadow protected abstract void debugWarn(String string, Object... objects);

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "processF3", cancellable = true)
    public void processF3(int key, CallbackInfoReturnable<Boolean> cir){
        if (this.debugCrashStartTime > 0L && this.debugCrashStartTime < Util.getMeasuringTimeMs() - 100L) {
            return;
        }

        if (key == 'E'/* && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 340)*/) {
            this.debugWarn("debug.show_client_phys.message");
            KevlarModClient.Companion.setDebugDrawEnabled(!KevlarModClient.Companion.getDebugDrawEnabled());
            cir.setReturnValue(true);
        } else if (key == 'Q') {
            ChatHud chatHud = this.client.inGameHud.getChatHud();
            chatHud.addMessage(new TranslatableText("debug.show_client_phys.help"));
        }
    }
}
