package net.createmod.ponder.mixin.accessor;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Accessor("deltaTracker")
	DeltaTracker catnip$getDeltaTracker();
}
