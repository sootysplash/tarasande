package net.tarasandedevelopment.tarasande.module.misc

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.tarasandedevelopment.tarasande.base.event.Event
import net.tarasandedevelopment.tarasande.base.module.Module
import net.tarasandedevelopment.tarasande.base.module.ModuleCategory
import net.tarasandedevelopment.tarasande.event.EventChat
import net.tarasandedevelopment.tarasande.event.EventPollEvents
import net.tarasandedevelopment.tarasande.util.math.TimeUtil
import net.tarasandedevelopment.tarasande.util.player.PlayerUtil
import net.tarasandedevelopment.tarasande.value.ValueBoolean
import net.tarasandedevelopment.tarasande.value.ValueMode
import net.tarasandedevelopment.tarasande.value.ValueNumber
import net.tarasandedevelopment.tarasande.value.ValueText
import org.apache.commons.lang3.RandomStringUtils
import java.util.function.Consumer
import kotlin.math.round
import kotlin.math.sqrt

class ModuleSpammer : Module("Spammer", "Spams something into the chat", ModuleCategory.MISC) {

    private val delay = ValueNumber(this, "Delay", 0.0, 2000.0, 10000.0, 500.0)
    private val noArbitraryTexts = ValueBoolean(this, "No arbitrary texts", false)
    private val garbage = object : ValueBoolean(this, "Garbage", false) {
        override fun isEnabled() = !noArbitraryTexts.value
    }
    private val garbageAmount = object : ValueNumber(this, "Garbage amount", 0.0, 5.0, 10.0, 1.0) {
        override fun isEnabled() = !noArbitraryTexts.value && garbage.value
    }
    private val garbageCase = object : ValueMode(this, "Garbage case", false, "Uppercase", "Random", "Lowercase") {
        override fun isEnabled() = !noArbitraryTexts.value && garbage.value
    }
    private val mode = object : ValueMode(this, "Mode", false, "Custom message", "Position broadcast") {
        override fun isEnabled() = !noArbitraryTexts.value
    }
    private val message = object : ValueText(this, "Message", "") {
        override fun isEnabled() = !noArbitraryTexts.value && mode.isSelected(0)
    }
    private val target = object : ValueText(this, "Target", "") {
        override fun isEnabled() = !noArbitraryTexts.value && mode.isSelected(1)
    }

    private val timeUtil = TimeUtil()
    private val priorityMessages = ArrayList<String>()

    override fun onDisable() {
        priorityMessages.clear()
    }

    val eventConsumer = Consumer<Event> { event ->
        when (event) {
            is EventPollEvents -> {
                if (event.fake) return@Consumer

                if (timeUtil.hasReached(delay.value.toLong())) {
                    if (priorityMessages.isNotEmpty()) {
                        PlayerUtil.sendChatMessage(priorityMessages.removeFirst())
                        timeUtil.reset()
                        return@Consumer
                    }
                    if (noArbitraryTexts.value) return@Consumer
                    var text = when {
                        mode.isSelected(0) -> message.value
                        mode.isSelected(1) -> {
                            var target: Entity? = null
                            for (entity in mc.world?.entities!!) {
                                if (entity is PlayerEntity && entity.gameProfile.name.equals(this.target.value, true)) {
                                    target = entity
                                    break
                                }
                            }

                            if (target != null) {
                                var closest: PlayerEntity? = null
                                var dist = 0.0
                                for (entity in mc.world?.entities!!) {
                                    if (entity is PlayerEntity && target != entity) {
                                        val dist2 = target.squaredDistanceTo(entity)
                                        if (closest == null || dist2 < dist) {
                                            closest = entity
                                            dist = dist2
                                        }
                                    }
                                }

                                var string = "X: " + (round(target.x * 10) / 10.0) + " Y: " + (round(target.y * 10) / 10.0) + " Z: " + (round(target.z * 10) / 10.0)
                                if (closest != null) {
                                    string += " " + closest.gameProfile.name + " (" + (round(sqrt(dist) * 10) / 10) + "m)"
                                }
                                string
                            } else {
                                "Target is not in render distance"
                            }
                        }

                        else -> null
                    }
                    if (text != null) {
                        if (garbage.value) {
                            text = formatGarbage(RandomStringUtils.randomAlphanumeric(garbageAmount.value.toInt())) + " $text " + formatGarbage(RandomStringUtils.randomAlphanumeric(garbageAmount.value.toInt()))
                        }
                        PlayerUtil.sendChatMessage(text)
                    }
                    timeUtil.reset()
                }
            }

            is EventChat -> {
                priorityMessages.add(event.chatMessage)
                event.cancelled = true
            }
        }
    }

    private fun formatGarbage(string: String): String {
        return when {
            garbageCase.isSelected(0) -> string.uppercase()
            garbageCase.isSelected(1) -> string
            garbageCase.isSelected(2) -> string.lowercase()
            else -> string
        }
    }

}
