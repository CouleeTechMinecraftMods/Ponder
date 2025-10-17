package net.createmod.ponder.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.RenderStateShard;

/**
 * MC 1.21.5: RenderStateShard constants became protected/private, need accessor
 * Return types are just RenderStateShard because the inner classes are also protected
 */
@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
	@Accessor("RENDERTYPE_ENTITY_SOLID_SHADER")
	static RenderStateShard getRENDERTYPE_ENTITY_SOLID_SHADER() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER")
	static RenderStateShard getRENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("RENDERTYPE_ENTITY_TRANSLUCENT_SHADER")
	static RenderStateShard getRENDERTYPE_ENTITY_TRANSLUCENT_SHADER() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("RENDERTYPE_TRANSLUCENT_SHADER")
	static RenderStateShard getRENDERTYPE_TRANSLUCENT_SHADER() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("CULL")
	static RenderStateShard getCULL() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("NO_CULL")
	static RenderStateShard getNO_CULL() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("TRANSLUCENT_TRANSPARENCY")
	static RenderStateShard getTRANSLUCENT_TRANSPARENCY() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("COLOR_WRITE")
	static RenderStateShard getCOLOR_WRITE() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("LIGHTMAP")
	static RenderStateShard getLIGHTMAP() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("OVERLAY")
	static RenderStateShard getOVERLAY() {
		throw new AssertionError("Mixin application failed!");
	}

	@Accessor("BLOCK_SHEET_MIPPED")
	static RenderStateShard getBLOCK_SHEET_MIPPED() {
		throw new AssertionError("Mixin application failed!");
	}
}
