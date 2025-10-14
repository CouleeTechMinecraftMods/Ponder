package net.createmod.ponder.enums;

import com.mojang.blaze3d.systems.RenderSystem;

import net.createmod.catnip.render.BindableTexture;
import net.createmod.catnip.util.TextureUtil;
import net.createmod.ponder.Ponder;
import net.minecraft.resources.ResourceLocation;

public enum PonderSpecialTextures implements BindableTexture {

	BLANK("blank.png"),

	;

	public static final String ASSET_PATH = "textures/special/";
	private final ResourceLocation location;

	PonderSpecialTextures(String filename) {
		location = Ponder.asResource(ASSET_PATH + filename);
	}

	@Override
	public void bind() {
		// MC 1.21.5: setShaderTexture now requires GpuTexture, use TextureUtil reflection
		RenderSystem.setShaderTexture(0, TextureUtil.getGpuTexture(net.minecraft.client.Minecraft.getInstance().getTextureManager().getTexture(location)));
	}

	@Override
	public ResourceLocation getLocation() {
		return location;
	}

}
