package net.createmod.catnip.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.renderer.texture.AbstractTexture;

public final class TextureUtil {
	private static final Method GET_TEXTURE_METHOD;
	private static final Field ID_FIELD;
	private static final Field FRAMEBUFFER_ID_FIELD;

	static {
		Method getTextureMethod = null;
		Field idField = null;
		Field framebufferIdField = null;
		try {
			// Try to find getTexture() method in AbstractTexture
			getTextureMethod = AbstractTexture.class.getDeclaredMethod("getTexture");
			getTextureMethod.setAccessible(true);

			// Then get the id field from the returned object (GpuTexture)
			Class<?> gpuTextureClass = Class.forName("com.mojang.blaze3d.platform.GpuTexture");
			idField = gpuTextureClass.getDeclaredField("id");
			idField.setAccessible(true);

			// Get framebuffer ID field from RenderTarget
			framebufferIdField = RenderTarget.class.getDeclaredField("frameBufferId");
			framebufferIdField.setAccessible(true);
		} catch (Exception e) {
			// Fall back - shouldn't happen but handle gracefully
			e.printStackTrace();
		}
		GET_TEXTURE_METHOD = getTextureMethod;
		ID_FIELD = idField;
		FRAMEBUFFER_ID_FIELD = framebufferIdField;
	}

	/**
	 * Gets the GpuTexture object from an AbstractTexture.
	 * This is needed for RenderSystem.setShaderTexture() in MC 1.21.5.
	 */
	public static Object getGpuTexture(AbstractTexture texture) {
		try {
			return GET_TEXTURE_METHOD.invoke(texture);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get GPU texture", e);
		}
	}

	/**
	 * Gets the OpenGL texture ID from an AbstractTexture.
	 * Use this for direct OpenGL calls, but prefer getGpuTexture() for Minecraft API.
	 */
	public static int getTextureId(AbstractTexture texture) {
		try {
			Object gpuTexture = GET_TEXTURE_METHOD.invoke(texture);
			return ID_FIELD.getInt(gpuTexture);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get texture ID", e);
		}
	}

	public static int getGpuTextureId(Object gpuTexture) {
		try {
			return ID_FIELD.getInt(gpuTexture);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get GPU texture ID", e);
		}
	}

	public static int getFramebufferId(RenderTarget renderTarget) {
		try {
			return FRAMEBUFFER_ID_FIELD.getInt(renderTarget);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get framebuffer ID", e);
		}
	}

	private TextureUtil() {
	}
}
