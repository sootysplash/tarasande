package su.mandora.tarasande.mixin.mixins;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.mandora.tarasande.TarasandeMain;
import su.mandora.tarasande.base.screen.accountmanager.account.Account;
import su.mandora.tarasande.event.EventResolutionUpdate;
import su.mandora.tarasande.event.EventTick;
import su.mandora.tarasande.event.EventTimeTravel;
import su.mandora.tarasande.mixin.accessor.IMinecraftClient;
import su.mandora.tarasande.module.misc.ModuleTickBaseManipulation;
import su.mandora.tarasande.module.player.ModuleTimer;
import su.mandora.tarasande.module.render.ModuleESP;
import su.mandora.tarasande.screen.Screens;
import su.mandora.tarasande.util.render.RenderUtil;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IMinecraftClient {
    @Shadow
    @Final
    public GameOptions options;
    @Shadow
    protected int attackCooldown;
    @Shadow
    @Final
    private Window window;
    @Mutable
    @Shadow
    @Final
    private Session session;
    @Shadow
    @Final
    private MinecraftSessionService sessionService;
    private long startTime;
    @Shadow
    @Final
    private RenderTickCounter renderTickCounter;

    public MixinMinecraftClient() {
        this.startTime = 0L;
    }

    @Shadow
    protected abstract void doItemUse();

    @Shadow
    protected abstract void doAttack();

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;profiler:Lnet/minecraft/util/profiler/Profiler;", ordinal = 0, shift = At.Shift.BEFORE))
    public void injectPreInit(final RunArgs args, final CallbackInfo ci) {
        TarasandeMain.Companion.get().onPreLoad();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void injectPostInit(final RunArgs args, final CallbackInfo ci) {
        TarasandeMain.Companion.get().onLateLoad();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void injectPreTick(final CallbackInfo ci) {
        TarasandeMain.Companion.get().getManagerEvent().call(new EventTick(EventTick.State.PRE));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void injectPostTick(final CallbackInfo ci) {
        TarasandeMain.Companion.get().getManagerEvent().call(new EventTick(EventTick.State.POST));
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10000))
    public int screenAttackCooldown(final int original) {
        return this.attackCooldown;
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void injectStop(final CallbackInfo ci) {
        TarasandeMain.Companion.get().onUnload();
    }

    @Inject(method = "onResolutionChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;resize(IIZ)V", shift = At.Shift.AFTER))
    public void injectOnResolutionChanged(final CallbackInfo ci) {
        TarasandeMain.Companion.get().getManagerEvent().call(new EventResolutionUpdate((float) this.window.getFramebufferWidth(), (float) this.window.getFramebufferHeight()));
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void injectPreRender(final boolean tick, final CallbackInfo ci) {
        this.startTime = System.nanoTime();
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void injectPostRender(final boolean tick, final CallbackInfo ci) {
        RenderUtil.INSTANCE.setDeltaTime((System.nanoTime() - this.startTime) / 1000000.0);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    public int hookedMin(final int a, final int b) {
        if (TarasandeMain.Companion.get().getClientValues().getUnlockTicksPerFrame().getValue()) {
            return b;
        }
        return Math.min(a, b);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J", ordinal = 0))
    public long hookedGetMeasuringTimeMs() {
        EventTimeTravel eventTimeTravel = new EventTimeTravel(Util.getMeasuringTimeMs());
        TarasandeMain.Companion.get().getManagerEvent().call(eventTimeTravel);
        return eventTimeTravel.getTime();
    }

    @Redirect(method = "hasOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isGlowing()Z"))
    public boolean hookedIsGlowing(final Entity entity) {
        return TarasandeMain.Companion.get().getManagerModule().get(ModuleESP.class).getEnabled() || entity.isGlowing();
    }

    @Overwrite
    public MinecraftSessionService getSessionService() {
        final Screens screens = TarasandeMain.Companion.get().getScreens();
        if (screens != null) {
            final Account account = screens.getBetterScreenAccountManager().getCurrentAccount();
            if (account != null && account.getSessionService() != null) {
                return account.getSessionService();
            }
        }
        return this.sessionService;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }

    @Override
    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    @Override
    public void setAttackCooldown(final int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    @Override
    public void invokeDoItemUse() {
        this.doItemUse();
    }

    @Override
    public void invokeDoAttack() {
        this.doAttack();
    }

    @Override
    public RenderTickCounter getRenderTickCounter() {
        return renderTickCounter;
    }
}
