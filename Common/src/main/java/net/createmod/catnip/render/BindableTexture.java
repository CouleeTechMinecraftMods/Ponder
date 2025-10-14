package net.createmod.catnip.render;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.resources.ResourceLocation;

public interface BindableTexture {

	default void bind() {
		// setShaderTexture now requires GpuTexture instead of ResourceLocation
		// This is handled by RenderType system in 1.21.5
		// RenderSystem.setShaderTexture(0, getLocation());
	}

	ResourceLocation getLocation();

}
