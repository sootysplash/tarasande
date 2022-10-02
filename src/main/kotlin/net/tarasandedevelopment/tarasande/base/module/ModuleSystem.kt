package net.tarasandedevelopment.tarasande.base.module

import net.minecraft.client.MinecraftClient
import net.tarasandedevelopment.tarasande.TarasandeMain
import net.tarasandedevelopment.tarasande.base.Manager
import net.tarasandedevelopment.tarasande.event.EventTick
import net.tarasandedevelopment.tarasande.module.combat.*
import net.tarasandedevelopment.tarasande.module.exploit.*
import net.tarasandedevelopment.tarasande.module.ghost.*
import net.tarasandedevelopment.tarasande.module.misc.*
import net.tarasandedevelopment.tarasande.module.movement.*
import net.tarasandedevelopment.tarasande.module.player.*
import net.tarasandedevelopment.tarasande.module.qol.ModuleInstantWorld
import net.tarasandedevelopment.tarasande.module.render.*
import net.tarasandedevelopment.tarasande.value.ValueBind
import net.tarasandedevelopment.tarasande.value.ValueBoolean
import org.lwjgl.glfw.GLFW

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
            ModuleDeadByDaylightEscape(),
            ModuleSpeed(),
            ModuleVehicleFlight(),
            ModuleNoSlowdown(),
            ModuleTeams(),
            ModuleInventoryMove(),
            ModuleBlink(),
            ModuleFlight(),
            ModuleTickBaseManipulation(),
            ModuleNoCooldown(),
            ModuleWTap(),
            ModuleTrajectories(),
            ModuleAntiBot(),
            ModuleNoFov(),
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
            ModuleDisableSequencePackets(),
            ModuleAntiBindingCurse(),
            ModuleBungeeHack(),
            ModuleCommandBlockBypass(),
            ModuleAntiParticleHide(),
            ModulePrivateMsgDetector(),
            ModuleNoChatContext(),
            ModuleInstantWorld(),
            ModuleFurnaceProgress(),
            ModuleNoMiningTrace(),
            ModuleDisableTelemetry(),
            ModuleBlockChangeTracker(),
            ModuleTrueSight()
        )
        TarasandeMain.get().managerEvent.add { event ->
            if (event is EventTick)
                if (event.state == EventTick.State.POST) {
                    for (module in list)
                        for (i in 0 until module.bind.wasPressed())
                            module.switchState()
                }
        }
    }

}

open class Module(val name: String, val description: String, val category: ModuleCategory) {
    var visibleInMenu = true
    val visible = ValueBoolean(this, "Visible in ArrayList", true)

    @Suppress("PropertyName")
    var _enabled = false
        private set
    var enabled: Boolean
        set(value) {
            if (_enabled != value) if (value) {
                onEnable()
                TarasandeMain.get().managerEvent.addObject(this)
            } else {
                TarasandeMain.get().managerEvent.remObject(this)
                onDisable()
            }

            _enabled = value
        }
        get() = _enabled && isEnabled()

    val bind = ValueBind(this, "Bind", ValueBind.Type.KEY, GLFW.GLFW_KEY_UNKNOWN)

    val mc: MinecraftClient = MinecraftClient.getInstance()

    fun switchState() {
        enabled = !enabled
    }

    open fun onEnable() {}
    open fun onDisable() {}

    open fun isEnabled() = true
}

enum class ModuleCategory {
    COMBAT, MOVEMENT, PLAYER, RENDER, MISC, GHOST, EXPLOIT, QUALITY_OF_LIFE
}