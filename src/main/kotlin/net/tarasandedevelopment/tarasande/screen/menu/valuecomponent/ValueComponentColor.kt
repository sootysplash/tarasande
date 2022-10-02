package net.tarasandedevelopment.tarasande.screen.menu.valuecomponent

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.tarasandedevelopment.tarasande.TarasandeMain
import net.tarasandedevelopment.tarasande.base.screen.menu.valuecomponent.ValueComponent
import net.tarasandedevelopment.tarasande.base.value.Value
import net.tarasandedevelopment.tarasande.screen.menu.utils.DragInfo
import net.tarasandedevelopment.tarasande.util.render.RenderUtil.fill
import net.tarasandedevelopment.tarasande.util.render.RenderUtil.fillCircle
import net.tarasandedevelopment.tarasande.util.render.RenderUtil.fillHorizontalGradient
import net.tarasandedevelopment.tarasande.util.render.RenderUtil.fillVerticalGradient
import net.tarasandedevelopment.tarasande.util.render.RenderUtil.isHovered
import net.tarasandedevelopment.tarasande.util.render.RenderUtil.outlinedCircle
import net.tarasandedevelopment.tarasande.util.render.RenderUtil.outlinedFill
import net.tarasandedevelopment.tarasande.value.ValueColor
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class ValueComponentColor(value: Value) : ValueComponent(value) {

    private val wheelDragInfo = DragInfo()
    private val rectDragInfo = DragInfo()
    private val alphaDragInfo = DragInfo()
    private var lastWheelClick = 0L

    private val lockToAccentColorText = "Lock to accent color"

    override fun init() {
    }

    // Make sure Accent Color doesn't handle itself
    private fun isAccent() = value == TarasandeMain.get().clientValues.accentColor

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        val valueColor = value as ValueColor
        val white = Color.white.let { if (valueColor.isEnabled() && !valueColor.locked) it else it.darker().darker() }
        val unblockedWhite = Color.white.let { if (valueColor.isEnabled()) it else it.darker().darker() }

        val x1 = width - (getPickerHeight() - 5) / 2.0 - sin(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)
        val y1 = (getPickerHeight() - 5) / 2.0 - sin(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)

        val x2 = width - (getPickerHeight() - 5) / 2.0 + cos(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)
        val y2 = (getPickerHeight() - 5) / 2.0 + cos(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)

        if (alphaDragInfo.dragging) {
            valueColor.alpha = 1.0f - MathHelper.clamp((mouseY + (mouseY / (getPickerHeight() - 5) * 2 - 1) * 5) / (getPickerHeight() - 5), 0.0, 1.0).toFloat()
            valueColor.onChange()
        }
        if (rectDragInfo.dragging) {
            valueColor.sat = MathHelper.clamp((mouseX - x1) / (x2 - x1), 0.0, 1.0).toFloat()
            valueColor.bri = 1.0f - MathHelper.clamp((mouseY - y1) / (y2 - y1), 0.0, 1.0).toFloat()
            valueColor.onChange()
        }
        if (wheelDragInfo.dragging) {
            val mousePos = Vec2f(mouseX.toFloat(), mouseY.toFloat())
            val middle = Vec2f((x1 + (x2 - x1) * 0.5).toFloat(), (y1 + (y2 - y1) * 0.5).toFloat())
            val mouseDir = mousePos.add(middle.multiply(-1.0f)).normalize() // large subtraction
            valueColor.hue = ((atan2(mouseDir.y, mouseDir.x) + PI - PI / 2) / (2 * PI)).toFloat()
            valueColor.onChange()
        }

        matrices?.push()
        matrices?.translate(0.0, (getPickerHeight() - 5) / 2.0, 0.0)
        matrices?.scale(0.5f, 0.5f, 1.0f)
        matrices?.translate(0.0, -(getPickerHeight() - 5) / 2.0, 0.0)
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, value.name, 0.0f, ((getPickerHeight() - 5) / 2.0f - MinecraftClient.getInstance().textRenderer.fontHeight / 2.0f).toFloat(), white.rgb)
        matrices?.pop()

        val matrix4f = matrices?.peek()?.positionMatrix!!
        val bufferBuilder = Tessellator.getInstance().buffer
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }

        val nextHue = if (valueColor.locked) TarasandeMain.get().clientValues.accentColor.hue else valueColor.hue
        val hsb = Color.getHSBColor(nextHue, 1.0f, 1.0f).let { if (valueColor.isEnabled()) it else it.darker().darker() }
        fill(matrices, x1, y1, x2, y2, hsb.rgb)
        fillHorizontalGradient(matrices, x1, y1, x2, y2, Color.white.let { Color(it.red, it.green, it.blue, 0) }.rgb, unblockedWhite.rgb)
        fillVerticalGradient(matrices, x1, y1, x2, y2, Color.black.let { Color(it.red, it.green, it.blue, 0) }.rgb, Color.black.let { if (valueColor.isEnabled()) it else it.darker().darker() }.rgb)

        if (!isAccent()) {
            matrices.push()
            matrices.scale(0.5f, 0.5f, 0.5f)

            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices,
                lockToAccentColorText,
                (width * 2.0f - MinecraftClient.getInstance().textRenderer.getWidth(lockToAccentColorText)).toFloat(),
                getPickerHeight().toFloat() * 2.0f,
                (if (!valueColor.locked)
                    white
                else if (valueColor.isEnabled())
                    TarasandeMain.get().clientValues.accentColor.getColor()
                else
                    TarasandeMain.get().clientValues.accentColor.getColor().darker().darker()).rgb)
            matrices.pop()
        }

        outlinedFill(matrices, x1, y1, x2, y2, 2.0f, unblockedWhite.rgb)
        outlinedCircle(matrices, x1 + (x2 - x1) * valueColor.sat, y1 + (y2 - y1) * (1.0 - valueColor.bri), 2.0, 2.0f, unblockedWhite.rgb)
        fillCircle(matrices, x1 + (x2 - x1) * valueColor.sat, y1 + (y2 - y1) * (1.0 - valueColor.bri), 2.0, Color.getHSBColor(nextHue, valueColor.sat, valueColor.bri).let { if (valueColor.isEnabled()) it else it.darker().darker() }.rgb)
        outlinedCircle(matrices, width - (getPickerHeight() - 5) / 2.0, (getPickerHeight() - 5) / 2.0, (getPickerHeight() - 5) / 2.0 - 5, 2.0f, white.rgb)
        outlinedCircle(matrices, width - (getPickerHeight() - 5) / 2.0, (getPickerHeight() - 5) / 2.0, (getPickerHeight() - 5) / 2.0, 2.0f, white.rgb)

        val innerRadius = (getPickerHeight() - 5) / 2.0 - 5
        val outerRadius = (getPickerHeight() - 5) / 2.0
        val middleRadius = innerRadius + (outerRadius - innerRadius) * 0.5
        val width = outerRadius - innerRadius
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR)
        run {
            var circle = 0.0
            while (circle <= 1.01) {
                var hsb2 = Color.getHSBColor((circle.toFloat() + 0.5f) % 1.0f, 1.0f, 1.0f)
                if (!valueColor.isEnabled() || valueColor.locked) hsb2 = hsb2.darker().darker()
                val f2 = (hsb2.rgb shr 24 and 0xFF) / 255.0f
                val g2 = (hsb2.rgb shr 16 and 0xFF) / 255.0f
                val h2 = (hsb2.rgb shr 8 and 0xFF) / 255.0f
                val i = (hsb2.rgb and 0xFF) / 255.0f
                bufferBuilder.vertex(matrix4f, (this.width - (getPickerHeight() - 5) / 2.0 - sin(circle * PI * 2) * innerRadius).toFloat(), ((getPickerHeight() - 5) / 2.0 + cos(circle * PI * 2) * innerRadius).toFloat(), 0.0f).color(g2, h2, i, f2).next()
                bufferBuilder.vertex(matrix4f, (this.width - (getPickerHeight() - 5) / 2.0 - sin(circle * PI * 2) * outerRadius).toFloat(), ((getPickerHeight() - 5) / 2.0 + cos(circle * PI * 2) * outerRadius).toFloat(), 0.0f).color(g2, h2, i, f2).next()
                circle += 0.01
            }
        }
        BufferRenderer.drawWithShader(bufferBuilder.end())

        outlinedCircle(matrices, this.width - (getPickerHeight() - 5) / 2.0 - sin((valueColor.hue + 0.5f) * PI * 2) * middleRadius, (getPickerHeight() - 5) / 2.0 + cos((valueColor.hue + 0.5f) * PI * 2) * middleRadius, width / 2.0, 3.0f, white.rgb)
        fillCircle(matrices, this.width - (getPickerHeight() - 5) / 2.0 - sin((valueColor.hue + 0.5f) * PI * 2) * middleRadius, (getPickerHeight() - 5) / 2.0 + cos((valueColor.hue + 0.5f) * PI * 2) * middleRadius, width / 2.0, Color.getHSBColor(valueColor.hue, 1.0f, 1.0f).let { if (valueColor.isEnabled() && !valueColor.locked) it else it.darker().darker() }.rgb)
        if (valueColor.alpha != null) {
            val alpha = valueColor.alpha!!
            fillVerticalGradient(matrices, this.width - (getPickerHeight() - 5) - 10, 0.0, this.width - (getPickerHeight() - 5) - 5, getPickerHeight() - 5, unblockedWhite.rgb, Color.black.let { if (valueColor.isEnabled()) it else it.darker().darker() }.rgb)
            outlinedFill(matrices, this.width - (getPickerHeight() - 5) - 10, 0.0, this.width - (getPickerHeight() - 5) - 5, getPickerHeight() - 5, 2.0f, unblockedWhite.rgb)
            fillCircle(matrices, this.width - (getPickerHeight() - 5) - 7.5, (getPickerHeight() - 5) * (1.0 - alpha) + (alpha * 2 - 1) * 2.5, 2.5, Color(alpha, alpha, alpha).let { if (valueColor.isEnabled()) it else it.darker().darker() }.rgb)
            outlinedCircle(matrices, this.width - (getPickerHeight() - 5) - 7.5, (getPickerHeight() - 5) * (1.0 - alpha) + (alpha * 2 - 1) * 2.5, 2.5, 2.0f, unblockedWhite.rgb)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isHovered(mouseX, mouseY, 0.0, 0.0, width, getHeight())) return false

        val valueColor = value as ValueColor

        if (!this.isAccent() && isHovered(mouseX, mouseY, width - MinecraftClient.getInstance().textRenderer.getWidth(lockToAccentColorText) / 2.0f, getPickerHeight(), width, getPickerHeight() + MinecraftClient.getInstance().textRenderer.fontHeight / 2.0f)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                val accent = TarasandeMain.get().clientValues.accentColor

                valueColor.hue = accent.hue
                valueColor.bri = accent.bri
                valueColor.sat = accent.sat
                return true
            } else {
                valueColor.locked = !valueColor.locked
            }
        }

        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false

        if (valueColor.alpha != null && isHovered(mouseX, mouseY, width - (getPickerHeight() - 5) - 10, 0.0, width - (getPickerHeight() - 5) - 5, getPickerHeight() - 5)) {
            alphaDragInfo.setDragInfo(true, mouseX, mouseY)
            return true
        }

        val x1 = width - (getPickerHeight() - 5) / 2.0 - sin(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)
        val y1 = (getPickerHeight() - 5) / 2.0 - sin(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)
        val x2 = width - (getPickerHeight() - 5) / 2.0 + cos(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)
        val y2 = (getPickerHeight() - 5) / 2.0 + cos(0.75) * ((getPickerHeight() - 5) / 2.0 - 5)
        if (isHovered(mouseX, mouseY, x1, y1, x2, y2)) {
            rectDragInfo.setDragInfo(true, mouseX, mouseY)
            return true
        }

        if (!valueColor.locked) {
            val innerRadius = (getPickerHeight() - 5) / 2.0 - 5
            val outerRadius = (getPickerHeight() - 5) / 2.0
            if (Vec2f(mouseX.toFloat(), mouseY.toFloat()).distanceSquared(Vec2f((x1 + (x2 - x1) * 0.5).toFloat(), (y1 + (y2 - y1) * 0.5).toFloat())) in (innerRadius * innerRadius)..(outerRadius * outerRadius) && !valueColor.locked) {
                if (System.currentTimeMillis() - lastWheelClick < 250L) {
                    valueColor.rainbow = !valueColor.rainbow
                } else {
                    valueColor.rainbow = false
                }
                wheelDragInfo.setDragInfo(true, mouseX, mouseY)
                lastWheelClick = System.currentTimeMillis()
                return true
            }
        }
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        this.wheelDragInfo.setDragInfo(false, 0.0, 0.0)
        this.rectDragInfo.setDragInfo(false, 0.0, 0.0)
        this.alphaDragInfo.setDragInfo(false, 0.0, 0.0)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double) = false

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = false

    override fun charTyped(chr: Char, modifiers: Int) {
    }

    override fun tick() {
    }

    override fun onClose() {
        this.wheelDragInfo.setDragInfo(false, 0.0, 0.0)
        this.rectDragInfo.setDragInfo(false, 0.0, 0.0)
        this.alphaDragInfo.setDragInfo(false, 0.0, 0.0)
    }

    private fun getPickerHeight() = 55.0
    override fun getHeight() = getPickerHeight() + if (!isAccent()) MinecraftClient.getInstance().textRenderer.fontHeight else 0
}
