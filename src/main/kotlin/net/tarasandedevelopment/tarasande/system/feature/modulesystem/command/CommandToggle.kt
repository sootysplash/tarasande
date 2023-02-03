package net.tarasandedevelopment.tarasande.system.feature.modulesystem.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.ManagerModule
import net.tarasandedevelopment.tarasande.util.player.chat.CustomChat
import net.tarasandedevelopment.tarasande.system.feature.commandsystem.Command

class CommandToggle : Command("toggle") {

    @Suppress("NAME_SHADOWING")
    override fun builder(builder: LiteralArgumentBuilder<CommandSource>): LiteralArgumentBuilder<CommandSource> {
        return builder.then(this.argument("module", StringArgumentType.greedyString())?.suggests { _, builder ->
            ManagerModule.list.forEach {
                if (it.name.startsWith(builder.remaining)) {
                    builder.suggest(it.name)
                }
            }
            return@suggests builder.buildFuture()
        }?.executes {
            val moduleName = StringArgumentType.getString(it, "module")
            val module = ManagerModule.list.firstOrNull { module -> module.name == moduleName }
            if (module != null) {
                module.switchState()
                CustomChat.printChatMessage("[" + module.name + "] is now " + if (module.enabled.value) "enabled" else "disabled")
                return@executes SUCCESS
            } else {
                CustomChat.printChatMessage("[$moduleName] does not exist")
                return@executes ERROR
            }
        })
    }
}