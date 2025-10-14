package net.createmod.catnip.gui.element;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.createmod.catnip.client.render.model.BakedModelBufferer;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.impl.client.render.ColoringVertexConsumer;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipClientServices;
import net.createmod.catnip.util.TextureUtil;
import net.createmod.ponder.mixin.client.accessor.ItemRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
// MC 1.21.5: BakedModel class removed, using Object for model types
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class GuiGameElement {

	public static GuiRenderBuilder of(ItemStack stack) {
		return new GuiItemRenderBuilder(stack);
	}

	public static GuiRenderBuilder of(ItemLike itemProvider) {
		return new GuiItemRenderBuilder(itemProvider);
	}

	public static GuiRenderBuilder of(BlockState state) {
		return new GuiBlockStateRenderBuilder(state);
	}

	public static GuiRenderBuilder of(BlockState state, BlockEntity blockEntity) {
		return new GuiBlockEntityRenderBuilder(state, blockEntity);
	}

	public static GuiRenderBuilder of(BlockEntity blockEntity) {
		return of(blockEntity.getBlockState(), blockEntity);
	}

	public static GuiRenderBuilder of(Fluid fluid) {
		return new GuiBlockStateRenderBuilder(fluid.defaultFluidState()
			.createLegacyBlock()
			.setValue(LiquidBlock.LEVEL, 0));
	}

	public static GuiRenderBuilder of(PartialModel partial) {
		return new GuiBlockPartialRenderBuilder(partial);
	}

	public static abstract class GuiRenderBuilder extends AbstractRenderElement {
		protected double xLocal, yLocal, zLocal;
		protected double xRot, yRot, zRot;
		protected double scale = 1;
		protected int color = 0xFFFFFF;
		protected Vec3 rotationOffset = Vec3.ZERO;
		@Nullable protected ILightingSettings customLighting = null;

		public GuiRenderBuilder atLocal(double x, double y, double z) {
			this.xLocal = x;
			this.yLocal = y;
			this.zLocal = z;
			return this;
		}

		public GuiRenderBuilder rotate(double xRot, double yRot, double zRot) {
			this.xRot = xRot;
			this.yRot = yRot;
			this.zRot = zRot;
			return this;
		}

		public GuiRenderBuilder rotateBlock(double xRot, double yRot, double zRot) {
			return this.rotate(xRot, yRot, zRot)
				.withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
		}

		public GuiRenderBuilder scale(double scale) {
			this.scale = scale;
			return this;
		}

		public GuiRenderBuilder color(int color) {
			this.color = color;
			return this;
		}

		public GuiRenderBuilder withRotationOffset(Vec3 offset) {
			this.rotationOffset = offset;
			return this;
		}

		public GuiRenderBuilder lighting(ILightingSettings lighting) {
			customLighting = lighting;
			return this;
		}

		protected void prepareMatrix(PoseStack poseStack) {
			poseStack.pushPose();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			prepareLighting(poseStack);
		}

		protected void transformMatrix(PoseStack poseStack) {
			poseStack.translate(x, y, z);
			poseStack.scale((float) scale, (float) scale, (float) scale);
			poseStack.translate(xLocal, yLocal, zLocal);
			UIRenderHelper.flipForGuiRender(poseStack);
			poseStack.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) zRot));
			poseStack.mulPose(Axis.XP.rotationDegrees((float) xRot));
			poseStack.mulPose(Axis.YP.rotationDegrees((float) yRot));
			poseStack.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
		}

		protected void cleanUpMatrix(PoseStack poseStack) {
			poseStack.popPose();
			cleanUpLighting(poseStack);
		}

		protected void prepareLighting(PoseStack poseStack) {
			if (customLighting != null) {
				customLighting.applyLighting();
			} else {
				Lighting.setupFor3DItems();
			}
		}

		protected void cleanUpLighting(PoseStack poseStack) {
			if (customLighting != null) {
				Lighting.setupFor3DItems();
			}
		}
	}

	protected static class GuiBlockModelRenderBuilder extends GuiRenderBuilder {

		protected Object blockModel; // MC 1.21.5: BakedModel type changed
		protected BlockState blockState;
		@Nullable protected BlockEntity blockEntity;

		public GuiBlockModelRenderBuilder(Object blockmodel, @Nullable BlockState blockState, @Nullable BlockEntity blockEntity) {
			this.blockState = blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
			this.blockModel = blockmodel;
			this.blockEntity = blockEntity;
		}

		@Override
		public void render(GuiGraphics graphics) {
			PoseStack poseStack = graphics.pose();
			prepareMatrix(poseStack);

			Minecraft mc = Minecraft.getInstance();
			BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
			MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

			transformMatrix(poseStack);

			// MC 1.21.5: setShaderTexture now requires GpuTexture, use TextureUtil reflection
			RenderSystem.setShaderTexture(0, TextureUtil.getGpuTexture(mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS)));
			renderModel(blockRenderer, buffer, poseStack);

			cleanUpMatrix(poseStack);
		}

		protected void renderModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer,
								   PoseStack ms) {
			SinglePosVirtualBlockGetter level = SinglePosVirtualBlockGetter.createFullBright();
			level.blockState(blockState);
			level.blockEntity(blockEntity);
			BakedModelBufferer.bufferModel(blockModel, BlockPos.ZERO, level, blockState, ms, (layer, shade) -> {
				// MC 1.21.5: Sheets.translucentCullBlockSheet() removed, use direct RenderType
				layer = layer == RenderType.translucent() ? RenderType.translucent() : Sheets.cutoutBlockSheet();
				return new ColoringVertexConsumer(buffer.getBuffer(layer), ARGB.red(color) / 255f, ARGB.green(color) / 255f, ARGB.blue(color) / 255f, 1);
			});

			buffer.endBatch();
		}

	}

	public static class GuiBlockEntityRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockEntityRenderBuilder(BlockState blockState, BlockEntity blockEntity) {
			super(
					Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState),
					blockState,
					blockEntity
			);
		}

		@Override
		protected void renderModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer, PoseStack ms) {
			renderBlockEntity(blockRenderer, buffer, ms);

			super.renderModel(blockRenderer, buffer, ms);
		}

		private void renderBlockEntity(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer, PoseStack ms) {
            if (blockEntity == null)
				return;

            BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (renderer == null)
				return;

            BlockState stateBefore = blockEntity.getBlockState();
            blockEntity.setBlockState(blockState);
            renderer.render(blockEntity, /*partials*/0, ms, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, Vec3.ZERO);
            blockEntity.setBlockState(stateBefore);
        }
	}

	public static class GuiBlockStateRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockStateRenderBuilder(BlockState blockstate) {
			super(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(blockstate), blockstate, null);
		}

		@Override
		protected void renderModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer, PoseStack poseStack) {
			if (blockState.getBlock() instanceof BaseFireBlock) {
				Lighting.setupForFlatItems();
				super.renderModel(blockRenderer, buffer, poseStack);
				Lighting.setupFor3DItems();
				return;
			}

			super.renderModel(blockRenderer, buffer, poseStack);

			if (blockState.getFluidState().isEmpty())
				return;

			CatnipClientServices.CLIENT_HOOKS.renderFullFluidState(poseStack, buffer, blockState.getFluidState());

			buffer.endBatch();
		}
	}

	public static class GuiItemRenderBuilder extends GuiRenderBuilder {

		private final ItemStack stack;

		public GuiItemRenderBuilder(ItemStack stack) {
			this.stack = stack;
		}

		public GuiItemRenderBuilder(ItemLike provider) {
			this(new ItemStack(provider));
		}

		@Override
		public void render(GuiGraphics graphics) {
			PoseStack poseStack = graphics.pose();
			prepareMatrix(poseStack);
			transformMatrix(poseStack);
			renderItemIntoGUI(poseStack, stack, customLighting == null);
			cleanUpMatrix(poseStack);
		}

		public static void renderItemIntoGUI(PoseStack poseStack, ItemStack stack, boolean useDefaultLighting) {
			ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
			// MC 1.21.5: getItemModel() -> getModel(), and item rendering is simplified
			// Object bakedModel = renderer.getModel(stack, null, null, 0);

			((ItemRendererAccessor) renderer).catnip$getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
			// MC 1.21.5: setShaderTexture now requires GpuTexture, use TextureUtil reflection
			RenderSystem.setShaderTexture(0, TextureUtil.getGpuTexture(Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS)));
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.pushPose();
			poseStack.translate(0, 0, 100.0F);
			poseStack.translate(8.0F, -8.0F, 0.0F);
			poseStack.scale(16.0F, 16.0F, 16.0F);
			MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			// MC 1.21.5: usesBlockLight() needs to be checked differently or assumed
			boolean flatLighting = true; // TODO: Check model lighting properties in new API
			if (useDefaultLighting && flatLighting) {
				Lighting.setupForFlatItems();
			}

			// MC 1.21.5: ItemRenderer.render() signature changed - use renderStatic() instead
			renderer.renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			buffer.endBatch();

			GL11.glEnable(GL11.GL_DEPTH_TEST);
			if (useDefaultLighting && flatLighting) {
				Lighting.setupFor3DItems();
			}

			poseStack.popPose();
		}

	}

	public static class GuiBlockPartialRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockPartialRenderBuilder(PartialModel partial) {
			super(partial.get(), null, null);
		}

	}
}
