package net.tarasandedevelopment.tarasande.screen.menu.panel.impl.fixed

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.tarasandedevelopment.tarasande.screen.menu.ScreenCheatMenu
import net.tarasandedevelopment.tarasande.screen.menu.panel.Panel
import net.tarasandedevelopment.tarasande.util.math.rotation.RotationUtil
import net.tarasandedevelopment.tarasande.util.render.RenderUtil
import java.awt.Color
import kotlin.math.*

class PanelRadar(x: Double, y: Double, screenCheatMenu: ScreenCheatMenu) : Panel("Radar", x, y, 100.0, 100.0, background = true, fixed = true) {

	override fun renderContent(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
		if (MinecraftClient.getInstance().player == null)
			return

		val pos = MinecraftClient.getInstance().player?.getLerpedPos(MinecraftClient.getInstance().tickDelta)!!
		for (entity in MinecraftClient.getInstance().world?.entities!!) {
			val otherPos = entity.getLerpedPos(MinecraftClient.getInstance().tickDelta)!!
			val dist = sqrt((otherPos.x - pos.x).pow(2.0) + (otherPos.z - pos.z).pow(2.0))

			if (dist > (panelWidth + panelHeight) / 2)
				continue

			val yawDelta = RotationUtil.getYaw(pos.x, pos.z, otherPos.x, otherPos.z) - MathHelper.wrapDegrees(MinecraftClient.getInstance().player?.yaw!!) + 180

			val x = -sin(yawDelta / 360.0 * PI * 2) * dist
			val y = cos(yawDelta / 360.0 * PI * 2) * dist

			RenderUtil.fillCircle(matrices, this.x + panelWidth / 2 + x, this.y + panelHeight / 2 + y, 2.0, Color(entity.teamColorValue).rgb /* alpha ignore */)
		}
	}
}