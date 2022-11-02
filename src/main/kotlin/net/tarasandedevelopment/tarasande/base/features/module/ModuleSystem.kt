package net.tarasandedevelopment.tarasande.base.features.module

import net.minecraft.client.MinecraftClient
import net.tarasandedevelopment.tarasande.TarasandeMain
import net.tarasandedevelopment.tarasande.base.Manager
import net.tarasandedevelopment.tarasande.base.event.Event
import net.tarasandedevelopment.tarasande.event.EventTick
import net.tarasandedevelopment.tarasande.features.module.ModuleNoteBot
import net.tarasandedevelopment.tarasande.features.module.combat.*
import net.tarasandedevelopment.tarasande.features.module.exploit.*
import net.tarasandedevelopment.tarasande.features.module.ghost.*
import net.tarasandedevelopment.tarasande.features.module.misc.*
import net.tarasandedevelopment.tarasande.features.module.movement.*
import net.tarasandedevelopment.tarasande.features.module.player.*
import net.tarasandedevelopment.tarasande.features.module.render.*
import net.tarasandedevelopment.tarasande.value.ValueBind
import net.tarasandedevelopment.tarasande.value.ValueBoolean
import org.lwjgl.glfw.GLFW
import java.util.function.Consumer

class ManagerModule : Manager<Module>() {

    init {
        add(
            ModuleSprint(),
            ModuleESP(),
            ModuleKillAura(),
            ModuleVelocity(),
            ModuleTimer(),
            ModuleScaffoldWalk(),
            ModuleSafeWalk(),
            ModuleFullBright(),
            ModuleSpammer(),
            ModuleSpeed(),
            ModuleVehicleFlight(),
            ModuleNoSlowdown(),
            ModuleTeams(),
            ModuleInventoryMove(),
            ModuleBlink(),
            ModuleFlight(),
            ModuleTickBaseManipulation(),
            ModuleWTap(),
            ModuleTrajectories(),
            net.tarasandedevelopment.tarasande.features.module.combat.ModuleAntiBot(),
            ModuleNoFOV(),
            ModuleKeepSprint(),
            ModuleMurderMystery(),
            ModuleNoSwing(),
            ModuleColorCorrection(),
            ModuleWorldTime(),
            ModuleNuker(),
            ModuleMovementRecorder(),
            ModuleBedESP(),
            ModuleFog(),
            ModuleProjectileAimBot(),
            ModuleNoHunger(),
            ModuleNoRotate(),
            ModuleQuakeAura(),
            ModuleBlockBot(),
            ModuleChestStealer(),
            ModuleAutoTool(),
            ModuleFreeCam(),
            ModuleRain(),
            ModuleNoFall(),
            ModuleCriticals(),
            ModuleReach(),
            ModuleAutoClicker(),
            ModuleAimAssist(),
            ModuleNoFriends(),
            ModuleBlockESP(),
            ModuleParkour(),
            ModuleNameProtect(),
            ModuleNoCramming(),
            ModuleStep(),
            ModuleResourcePackSpoofer(),
            ModuleMidClick(),
            ModuleTargetStrafe(),
            ModuleSneak(),
            ModuleAirStuck(),
            ModuleTNTBlock(),
            ModuleNoWeb(),
            ModuleClickTP(),
            ModuleBacktrace(),
            ModuleAntiFall(),
            ModuleHitBox(),
            ModuleFastPlace(),
            ModuleFastUse(),
            ModuleAntiBindingCurse(),
            ModuleCommandBlockBypass(),
            ModuleAntiParticleHide(),
            ModulePrivateMsgDetector(),
            ModuleNoChatContext(),
            ModuleNoMiningTrace(),
            ModuleDisableTelemetry(),
            ModuleBlockChangeTracker(),
            ModuleTrueSight(),
            ModulePhase(),
            ModuleInventoryCleaner(),
            ModuleLecternCrash(),
            ModuleCraftingDupe(),
            ModulePrediction(),
            ModulePerfectHorseJump(),
            ModuleTridentBoost(),
            ModuleAutoRespawn(),
            ModulePortalScreen(),
            ModuleAutoFish(),
            ModuleJesus(),
            ModuleFastClimb(),
            ModuleAntiAFK(),
            ModuleEntityControl(),
            ModuleFastBreak(),
            ModuleCivBreak(),
            ModuleHealingBot(),
            ModuleVehicleSpeed(),
            ModuleIgnoreResourcePackHash(),
            ModuleNoJumpCooldown(),
            ModuleBrigadierIgnoreCase(),
            ModuleEveryItemOnArmor(),
            ModuleNoRender(),
            ModuleCommands(),

            ModuleNoteBot()
        )
        TarasandeMain.get().managerEvent.add(EventTick::class.java) {
            if (it.state == EventTick.State.POST) {
                for (module in list)
                    for (i in 0 until module.bind.wasPressed())
                        module.switchState()
            }
        }
    }
}

open class Module(val name: String, val description: String, val category: String) {
    private val eventListeners = HashSet<Triple<Class<Event>, Int, Consumer<Event>>>()

    @Suppress("PropertyName")
    var _enabled = false
        private set
    var enabled: Boolean
        set(value) {
            if (_enabled != value) if (value) {
                onEnable()
                eventListeners.forEach { TarasandeMain.get().managerEvent.add(it.first, it.second, it.third) }
            } else {
                eventListeners.forEach { TarasandeMain.get().managerEvent.rem(it.first, it.third) }
                onDisable()
            }

            _enabled = value
        }
        get() = _enabled && isEnabled()

    val visible = ValueBoolean(this, "Visible in ArrayList", true)
    val bind = ValueBind(this, "Bind", ValueBind.Type.KEY, GLFW.GLFW_KEY_UNKNOWN)

    val mc: MinecraftClient = MinecraftClient.getInstance()

    fun switchState() {
        enabled = !enabled
    }

    open fun onEnable() {}
    open fun onDisable() {}

    open fun isEnabled() = true

    fun <T : Event> registerEvent(clazz: Class<T>, priority: Int = 1000, c: Consumer<T>) {
        @Suppress("UNCHECKED_CAST") // BYPASS GENERICS ONCE AGAIN $$$$$$$
        eventListeners.add(Triple(clazz as Class<Event>, priority, c as Consumer<Event>))
    }
}

object ModuleCategory {
    const val COMBAT = "Combat"
    const val MOVEMENT = "Movement"
    const val PLAYER = "Player"
    const val RENDER = "Render"
    const val MISC = "Misc"
    const val GHOST = "Ghost"
    const val EXPLOIT = "Exploit"
}