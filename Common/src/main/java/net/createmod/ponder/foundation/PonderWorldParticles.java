package net.createmod.ponder.foundation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;

public class PonderWorldParticles {

	private final Map<ParticleRenderType, Queue<Particle>> byType = Maps.newIdentityHashMap();
	private final Queue<Particle> queue = Queues.newArrayDeque();

	PonderLevel world;

	public PonderWorldParticles(PonderLevel world) {
		this.world = world;
	}

	public void addParticle(Particle p) {
		this.queue.add(p);
	}

	public void tick() {
		this.byType.forEach((p_228347_1_, p_228347_2_) -> this.tickParticleList(p_228347_2_));

		Particle particle;
		if (queue.isEmpty())
			return;
		while ((particle = this.queue.poll()) != null)
			this.byType.computeIfAbsent(particle.getRenderType(), $ -> EvictingQueue.create(16384))
				.add(particle);
	}

	private void tickParticleList(Collection<Particle> p_187240_1_) {
		if (p_187240_1_.isEmpty())
			return;

		Iterator<Particle> iterator = p_187240_1_.iterator();
		while (iterator.hasNext()) {
			Particle particle = iterator.next();
			particle.tick();
			if (!particle.isAlive())
				iterator.remove();
		}
	}

	public void renderParticles(PoseStack ms, MultiBufferSource buffer, Camera renderInfo, float pt) {
		Minecraft mc = Minecraft.getInstance();
		LightTexture lightTexture = mc.gameRenderer.lightTexture();

		lightTexture.turnOnLightLayer();
		// enableDepthTest removed - now controlled by RenderStateShard
		Matrix4fStack stack = RenderSystem.getModelViewStack();
		stack.pushMatrix();
		stack.mul(ms.last().pose());
		// applyModelViewMatrix removed in 1.21.5

		for (ParticleRenderType iparticlerendertype : this.byType.keySet()) {
			if (iparticlerendertype == ParticleRenderType.NO_RENDER)
				continue;
			Iterable<Particle> iterable = this.byType.get(iparticlerendertype);
			if (iterable != null) {
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				// getParticleShader removed - particles now use RenderType system
				// RenderSystem.setShader(GameRenderer::getParticleShader);

				Tesselator tesselator = Tesselator.getInstance();
				// begin() signature changed - now returns BufferSource
				// BufferBuilder bufferBuilder = iparticlerendertype.begin(tesselator, mc.getTextureManager());

				// Particle rendering now uses MultiBufferSource instead
				for (Particle particle : iterable) {
					particle.render(buffer, renderInfo, pt);
				}
			}
		}

		stack.popMatrix();
		// applyModelViewMatrix removed in 1.21.5
		// depthMask and disableBlend removed - now controlled by RenderStateShard
		lightTexture.turnOffLightLayer();
	}

	public void clearEffects() {
		this.byType.clear();
	}

}
