package net.tarasandedevelopment.tarasande.screen.cheatmenu.command

import net.minecraft.text.Text
import net.tarasandedevelopment.tarasande.base.screen.cheatmenu.command.Command
import net.tarasandedevelopment.tarasande.screen.cheatmenu.panel.impl.elements.impl.terminal.PanelElementsTerminal
import net.tarasandedevelopment.tarasande.util.player.chat.CustomChat

class CommandNotificationTest : Command("notification") {

    private val example = Text.literal("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam id viverra velit, aliquam porta arcu. In vitae malesuada tellus. Maecenas rutrum sem sed semper tincidunt. Mauris id feugiat libero, a lacinia ante. Vestibulum pulvinar quis erat at porttitor. Maecenas euismod tincidunt leo quis varius. Aenean aliquet metus eu neque dapibus, sed pharetra diam consectetur. ")

    override fun execute(args: Array<String>, panel: PanelElementsTerminal): Boolean {
        CustomChat.print(example)
        return false
    }
}
