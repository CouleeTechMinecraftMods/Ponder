package net.createmod.catnip.platform.services;

import net.createmod.catnip.annotations.Environment;

import net.createmod.catnip.annotations.Environment.EnvType;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

public interface ModFluidHelper<R> {
	@Environment(EnvType.CLIENT)
	int getColor(R fluid, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos);

	int getLuminosity(R fluid);

	@Environment(EnvType.CLIENT)
	@Nullable
	TextureAtlasSprite getStillTexture(R fluid);

	@Environment(EnvType.CLIENT)
	default TextureAtlasSprite getStillTextureOrMissing(R fluid) {
		TextureAtlasSprite texture = this.getStillTexture(fluid);
		if (texture != null)
			return texture;

		// MC 1.21.5: InventoryMenu.BLOCK_ATLAS renamed to TextureAtlas.LOCATION_BLOCKS
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
	}

	boolean isLighterThanAir(R fluid);

	R toStack(FluidState state);
}
