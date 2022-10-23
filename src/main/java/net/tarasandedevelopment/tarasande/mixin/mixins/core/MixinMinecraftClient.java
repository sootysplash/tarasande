package net.tarasandedevelopment.tarasande.mixin.mixins.core;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import net.tarasandedevelopment.tarasande.base.screen.clientmenu.accountmanager.account.Account;
import net.tarasandedevelopment.tarasande.screen.clientmenu.ElementMenuScreenAccountManager;
import net.tarasandedevelopment.tarasande.util.render.RenderUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    public int attackCooldown;

    @Shadow
    @Final
    private Window window;

    @Unique
    private long tarasande_startTime = 0L;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;createUserApiService(Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;Lnet/minecraft/client/RunArgs;)Lcom/mojang/authlib/minecraft/UserApiService;"))
    public void preLoadClient(RunArgs args, CallbackInfo ci) {
        TarasandeMain.Companion.get().onPreLoad();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V", shift = At.Shift.AFTER))
    public void lateLoadClient(RunArgs args, CallbackInfo ci) {
        TarasandeMain.Companion.get().onLateLoad();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void unloadClient(CallbackInfo ci) {
        TarasandeMain.Companion.get().onUnload();
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void trackRenderStart(boolean tick, CallbackInfo ci) {
        this.tarasande_startTime = System.nanoTime();
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void calculateDeltaTime(boolean tick, CallbackInfo ci) {
        RenderUtil.INSTANCE.setDeltaTime((System.nanoTime() - this.tarasande_startTime) / 1000000.0);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    public int unlockTicksPerFrame(int a, int b) {
        if (!TarasandeMain.Companion.get().getDisabled())
            if (TarasandeMain.Companion.get().getClientValues().getUnlockTicksPerFrame().getValue()) {
                return b;
            }
        return Math.min(a, b);
    }

    @Inject(method = "getSessionService", at = @At("RETURN"), cancellable = true)
    public void hookAccountManager(CallbackInfoReturnable<MinecraftSessionService> cir) {
        Account account = TarasandeMain.Companion.get().getManagerClientMenu().get(ElementMenuScreenAccountManager.class).getScreenBetterAccountManager().getCurrentAccount();
        if (account != null)
            cir.setReturnValue(account.getSessionService());
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;resetDebugHudChunk()V"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V")))
    public Screen passEventsInScreens(MinecraftClient instance) {
        if (!TarasandeMain.Companion.get().getDisabled()) {
            if (TarasandeMain.Companion.get().getClientValues().getPassEventsInScreens().getValue())
                if (MinecraftClient.getInstance().player != null)
                    return null;
        }
        return instance.currentScreen;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10000))
    public int ignoreCooldown(int constant) {
        if (!TarasandeMain.Companion.get().getDisabled()) {
            if (TarasandeMain.Companion.get().getClientValues().getPassEventsInScreens().getValue())
                return attackCooldown;
        }
        return constant;
    }
}