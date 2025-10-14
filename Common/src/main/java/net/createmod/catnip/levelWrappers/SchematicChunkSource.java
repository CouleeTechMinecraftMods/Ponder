package net.createmod.catnip.levelWrappers;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

public class SchematicChunkSource extends ChunkSource {
	private final Level fallbackWorld;

	public SchematicChunkSource(Level world) {
		fallbackWorld = world;
	}

	@Nullable
	@Override
	public LightChunk getChunkForLighting(int x, int z) {
		return getChunk(x, z);
	}

	@Override
	public Level getLevel() {
		return fallbackWorld;
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean p_212849_4_) {
		return getChunk(x, z);
	}

	public ChunkAccess getChunk(int x, int z) {
		return new EmptierChunk(fallbackWorld);
	}

	@Override
	public String gatherStats() {
		return "WrappedChunkProvider";
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return fallbackWorld.getLightEngine();
	}

	@Override
	public void tick(BooleanSupplier p_202162_, boolean p_202163_) {}

	@Override
	public int getLoadedChunksCount() {
		return 0;
	}

	public static class EmptierChunk extends LevelChunk {

		private static final class DummyLevel extends Level {
			// MC 1.21.5: Level constructor changed, removed ProfilerSupplier parameter
			private DummyLevel(WritableLevelData pLevelData, ResourceKey<Level> pDimension,
							   RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration,
							   boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed,
							   int pMaxChainedNeighborUpdates) {
				super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pIsClientSide, pIsDebug,
					  pBiomeZoomSeed, pMaxChainedNeighborUpdates);
				access = pRegistryAccess;
			}

			private final RegistryAccess access;

			private DummyLevel(Level level) {
				this(null, null, level.registryAccess(), level.dimensionTypeRegistration(), false, false, 0, 0);
			}

			@Override
			public ChunkSource getChunkSource() {
				return null;
			}

			// MC 1.21.5: levelEvent removed from Level
			public void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData) {}

			@Override
			public void gameEvent(@Nullable Entity entity, Holder<GameEvent> gameEvent, Vec3 pos) {}

			@Override
			public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {}

			@Override
			public RegistryAccess registryAccess() {
				return access;
			}

			@Override
			public PotionBrewing potionBrewing() {
				return null;
			}

			@Override
			public List<? extends Player> players() {
				return null;
			}

			@Override
			public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
				return null;
			}

			@Override
			public float getShade(Direction pDirection, boolean pShade) {
				return 0;
			}

			@Override
			public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {}

			// MC 1.21.5: playSound methods changed - only Holder<SoundEvent> versions remain
			public void playSound(Player pPlayer, double pX, double pY, double pZ, SoundEvent pSound,
				SoundSource pCategory, float pVolume, float pPitch) {}

			public void playSound(Player pPlayer, Entity pEntity, SoundEvent pEvent, SoundSource pCategory,
				float pVolume, float pPitch) {}

			// MC 1.21.5: Changed from Player to Entity for first parameter
			@Override
			public void playSeededSound(@Nullable Entity entity, double x, double y, double z,
										Holder<SoundEvent> sound, SoundSource source,
										float volume, float pitch, long seed) {}

			// MC 1.21.5: SoundEvent version removed
			public void playSeededSound(Player p_220363_, double p_220364_, double p_220365_, double p_220366_,
										SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {}

			// MC 1.21.5: Changed from Player to Entity for first parameter
			@Override
			public void playSeededSound(@Nullable Entity entity1, Entity entity2, Holder<SoundEvent> sound,
										SoundSource source, float volume, float pitch, long seed) {}

			@Override
			public String gatherChunkSourceStats() {
				return null;
			}

			@Override
			public Entity getEntity(int pId) {
				return null;
			}

			@Nullable
			@Override
			public MapItemSavedData getMapData(MapId mapId) {
				return null;
			}

			// MC 1.21.5: setMapData removed
			public void setMapData(MapId mapId, MapItemSavedData mapItemSavedData) {}

			// MC 1.21.5: getFreeMapId removed
			public MapId getFreeMapId() {
				return new MapId(0);
			}

			@Override
			public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {}

			@Override
			public Scoreboard getScoreboard() {
				return null;
			}

			// MC 1.21.5: getRecipeManager removed
			public RecipeManager getRecipeManager() {
				return null;
			}

			@Override
			public net.minecraft.world.item.crafting.RecipeAccess recipeAccess() {
				return net.minecraft.world.item.crafting.RecipeAccess.EMPTY;
			}

			@Override
			protected LevelEntityGetter<Entity> getEntities() {
				return null;
			}

			@Override
			public LevelTickAccess<Block> getBlockTicks() {
				return BlackholeTickAccess.emptyLevelList();
			}

			@Override
			public LevelTickAccess<Fluid> getFluidTicks() {
				return BlackholeTickAccess.emptyLevelList();
			}

			@Override
			public FeatureFlagSet enabledFeatures() {
				return FeatureFlagSet.of();
			}

			@Override
			public TickRateManager tickRateManager() {
				return null;
			}

			@Override
			public FuelValues fuelValues() {
				return FuelValues.vanillaBurnTimes(access);
			}

			@Override
			public java.util.Collection<EnderDragonPart> dragonParts() {
				return java.util.Collections.emptyList();
			}

			@Override
			public void explode(@Nullable Entity entity, @Nullable DamageSource damageSource,
								@Nullable ExplosionDamageCalculator calculator, double x, double y, double z,
								float radius, boolean fire, Level.ExplosionInteraction interaction,
								ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles,
								Holder<SoundEvent> explosionSound) {
			}

			// Neo's patched methods
			public void setDayTimeFraction(float var1) {}

			public float getDayTimeFraction() { return 0; }

			public float getDayTimePerTick() { return 0; }

			public void setDayTimePerTick(float var1) {}
		}

		public EmptierChunk(Level level) {
			super(new DummyLevel(level), ChunkPos.ZERO);
		}

		public BlockState getBlockState(BlockPos p_180495_1_) {
			return Blocks.VOID_AIR.defaultBlockState();
		}

		@Nullable
		public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_) {
			return null;
		}

		public FluidState getFluidState(BlockPos p_204610_1_) {
			return Fluids.EMPTY.defaultFluidState();
		}

		public int getLightEmission(BlockPos p_217298_1_) {
			return 0;
		}

		@Nullable
		public BlockEntity getBlockEntity(BlockPos p_177424_1_, EntityCreationType p_177424_2_) {
			return null;
		}

		public void addAndRegisterBlockEntity(BlockEntity p_150813_1_) {}

		public void setBlockEntity(BlockEntity p_177426_2_) {}

		public void removeBlockEntity(BlockPos p_177425_1_) {}

		public void markUnsaved() {}

		public boolean isEmpty() {
			return true;
		}

		public boolean isYSpaceEmpty(int p_76606_1_, int p_76606_2_) {
			return true;
		}

		public FullChunkStatus getFullStatus() {
			return FullChunkStatus.FULL;
		}
	}
}
