package net.tarasandedevelopment.tarasande.screen.cheatmenu.panel.impl.minecraftbutton

import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.tarasandedevelopment.tarasande.screen.cheatmenu.panel.Panel
import net.tarasandedevelopment.tarasande.screen.widget.panel.ClickableWidgetPanel
import net.tarasandedevelopment.tarasande.util.render.RenderUtil

class PanelButton(x: Int, y: Int, val width: Int, val height: Int, private val text: String, private val pressAction: Runnable) : Panel("Button", x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), scissor = true) {

    companion object {
        fun createButton(x: Int, y: Int, width: Int, height: Int, text: String, pressAction: Runnable): ClickableWidgetPanel {
            return ClickableWidgetPanel(PanelButton(x, y, width, height, text, pressAction))
        }
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        panelWidth = width.toDouble()
        panelHeight = height.toDouble()
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun renderContent(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        val middleX = x + panelWidth / 2.0
        val middleY = y + titleBarHeight + (panelHeight - titleBarHeight) / 2.0

        matrices?.push()
        matrices?.translate(middleX, middleY, 0.0)
        matrices?.scale(0.75f, 0.75f, 1.0f)
        matrices?.translate(-(middleX), -(middleY), 0.0)

        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices,
            text,
            (middleX - MinecraftClient.getInstance().textRenderer.getWidth(text) / 2.0).toFloat(),
            (middleY - MinecraftClient.getInstance().textRenderer.fontHeight / 2.0).toFloat(),
            -1)
        matrices?.pop()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (RenderUtil.isHovered(mouseX, mouseY, x, y + titleBarHeight, x + panelWidth, y + panelHeight)) {
            MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            pressAction.run()
            true
        } else
            false
    }
}