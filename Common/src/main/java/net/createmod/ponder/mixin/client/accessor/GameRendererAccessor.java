package net.createmod.ponder.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	// MC 1.21.2: getFov now returns float instead of double
	@Invoker("getFov")
	float catnip$callGetFov(Camera camera, float partialTicks, boolean useFOVSetting);
}
