/*
 * Copyright (c) FlorianMichael as EnZaXD 2022
 * Created on 7/16/22, 12:37 AM
 *
 * --FLORIAN MICHAEL PRIVATE LICENCE v1.0--
 *
 * This file / project is protected and is the intellectual property of Florian Michael (aka. EnZaXD),
 * any use (be it private or public, be it copying or using for own use, be it publishing or modifying) of this
 * file / project is prohibited. It requires in that use a written permission with official signature of the owner
 * "Florian Michael". "Florian Michael" receives the right to control and manage this file / project. This right is not
 * cancelled by copying or removing the license and in case of violation a criminal consequence is to be expected.
 * The owner "Florian Michael" is free to change this license.
 */

package com.mcf.davidee.nbtedit.gui;

import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

public class GuiNBTButton extends DrawableHelper {
	
	public static final int WIDTH = 9, HEIGHT = 9;
	
	private MinecraftClient mc = MinecraftClient.getInstance();
	
	private byte id;
	public int x, y;
	private boolean enabled;
	
	private long hoverTime;
	
	public GuiNBTButton(byte id, int x, int y){
		this.id = id;
		this.x = x; 
		this.y = y;
	}
	public void draw(MatrixStack matrices, int mx, int my) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, GuiNBTNode.WIDGET_TEXTURE);

		if(inBounds(mx,my)){
			fill(matrices, x, y, x+WIDTH, y+HEIGHT, 0x80ffffff);
			if (hoverTime == -1)
				hoverTime = System.currentTimeMillis();
		}
		else
			hoverTime = -1;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexture(matrices, x, y, (id-1) * 9, 18, WIDTH, HEIGHT);
		if (!enabled){
			fill(matrices, x, y, x+WIDTH, y+HEIGHT, 0xc0222222);
		}
		else if (hoverTime != -1 && System.currentTimeMillis() - hoverTime > 300){
			drawToolTip(matrices, mx, my);
		}
	}
	private void drawToolTip(MatrixStack matrices, int mx, int my) {
		String s = NBTStringHelper.getButtonName(id);
		int width = mc.textRenderer.getWidth(s);
		fill(matrices, mx+4, my+7, mx+5+width,my+17, 0xff000000);
		mc.textRenderer.draw(matrices, s, mx+5, my+8, 0xffffff);
	}
	public void setEnabled(boolean aFlag){
		enabled = aFlag;
	}
	public boolean isEnabled(){
		return enabled;
	}
	public boolean inBounds(int mx, int my){
		return enabled && mx >= x && my >= y && mx < x + WIDTH && my < y + HEIGHT;
	}
	public byte getId(){
		return id;
	}
}