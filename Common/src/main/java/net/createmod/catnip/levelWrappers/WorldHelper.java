package net.createmod.catnip.levelWrappers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

public class WorldHelper {
	public static ResourceLocation getDimensionID(LevelAccessor world) {
		// MC 1.21.5: Registry API changed - use lookup() instead of registryOrThrow()
		return world.registryAccess()
			.lookup(Registries.DIMENSION_TYPE)
			.flatMap(registry -> registry.getResourceKey(world.dimensionType()).map(key -> key.location()))
			.orElse(null);
	}
}
