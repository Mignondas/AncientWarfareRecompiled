package net.shadowmage.ancientwarfarerecompiled.core.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfarerecompiled.core.block.BlockResearchStation;
import net.shadowmage.ancientwarfarerecompiled.core.config.AWCoreConfig;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreAttachments;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreTileTypes;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IWorker;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemResearchBook;
import net.shadowmage.ancientwarfarerecompiled.core.owner.Owner;
import net.shadowmage.ancientwarfarerecompiled.core.research.PlayerResearch;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchGoal;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchRegistry;
import net.shadowmage.ancientwarfarerecompiled.core.upgrade.WorksiteUpgrade;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Entité de la station de recherche.
 *
 * Le livre lié désigne le chercheur. L'énergie arrive par le marteau ou la plume (clic droit en mode travail),
 * et chaque tick de recherche la consomme pour faire progresser l'objectif en cours du chercheur.
 * Limite : la progression n'avance que si le chercheur est connecté.
 */
public class TileResearchStation extends BlockEntity implements IWorkSite {
	public static final double MAX_ENERGY = 1600.0D;
	private static final int START_CHECK_DELAY_MAX = 40;

	private Owner owner = Owner.EMPTY;
	private double storedEnergy;
	private int startCheckDelay;

	public final ItemStackHandler bookSlot = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
			updateBookState();
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return ItemResearchBook.getResearcherName(stack) != null ? super.insertItem(slot, stack, simulate) : stack;
		}
	};

	public final ItemStackHandler resourceInventory = new ItemStackHandler(9) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	public TileResearchStation(BlockPos pos, BlockState state) {
		super(AWCoreTileTypes.RESEARCH_STATION.get(), pos, state);
	}

	/*
	 * Appelé à chaque tick côté serveur.
	 */
	public static void serverTick(Level level, BlockPos pos, BlockState state, TileResearchStation tile) {
		tile.tickResearch(level);
	}

	private void tickResearch(Level level) {
		String name = getCrafterName();
		if (name == null || !(level instanceof ServerLevel serverLevel)) {
			return;
		}
		ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayerByName(name);
		if (player == null) {
			return;
		}
		PlayerResearch research = player.getData(AWCoreAttachments.PLAYER_RESEARCH.get());
		double cost = AWCoreConfig.ENERGY_PER_RESEARCH_UNIT.get();
		if (research.getCurrent().isPresent()) {
			if (storedEnergy >= cost) {
				research.addProgress(1);
				storedEnergy -= cost;
				setChanged();
			}
		} else if (startCheckDelay > 0) {
			startCheckDelay--;
		} else {
			startCheckDelay = START_CHECK_DELAY_MAX;
			tryStartNextResearch(research);
		}
	}

	private void tryStartNextResearch(PlayerResearch research) {
		List<String> queue = research.getQueue();
		if (queue.isEmpty()) {
			return;
		}
		ResearchGoal goal = ResearchRegistry.getResearch(queue.get(0));
		if (goal != null && goal.tryStart(resourceInventory)) {
			research.startResearch(goal.getName());
		}
	}

	@Nullable
	public String getCrafterName() {
		return ItemResearchBook.getResearcherName(bookSlot.getStackInSlot(0));
	}

	public boolean hasBook() {
		return !bookSlot.getStackInSlot(0).isEmpty();
	}

	/*
	 * Affiche le livre sur le modèle lorsqu'un livre est présent.
	 */
	private void updateBookState() {
		if (level == null || level.isClientSide) {
			return;
		}
		BlockState state = getBlockState();
		if (state.hasProperty(BlockResearchStation.HAS_BOOK) && state.getValue(BlockResearchStation.HAS_BOOK) != hasBook()) {
			level.setBlock(worldPosition, state.setValue(BlockResearchStation.HAS_BOOK, hasBook()), Block.UPDATE_ALL);
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		updateBookState();
	}

	public void dropContents(Level level, BlockPos pos) {
		Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), bookSlot.getStackInSlot(0));
		for (int slot = 0; slot < resourceInventory.getSlots(); slot++) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), resourceInventory.getStackInSlot(slot));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("bookSlot", bookSlot.serializeNBT(registries));
		tag.put("resourceInventory", resourceInventory.serializeNBT(registries));
		tag.putDouble("storedEnergy", storedEnergy);
		tag.putInt("startCheckDelay", startCheckDelay);
		owner.serializeToNBT(tag);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		bookSlot.deserializeNBT(registries, tag.getCompound("bookSlot"));
		resourceInventory.deserializeNBT(registries, tag.getCompound("resourceInventory"));
		storedEnergy = tag.getDouble("storedEnergy");
		startCheckDelay = tag.getInt("startCheckDelay");
		owner = Owner.deserializeFromNBT(tag);
	}

	/*
	 * IWorkSite
	 */
	@Override
	public boolean hasWork() {
		return storedEnergy < MAX_ENERGY;
	}

	@Override
	public void addEnergyFromWorker(IWorker worker) {
		storedEnergy = Math.min(MAX_ENERGY, storedEnergy + AWCoreConfig.ENERGY_PER_WORK_UNIT.get() * worker.getWorkEffectiveness(getWorkType()));
	}

	@Override
	public void addEnergyFromPlayer(Player player) {
		storedEnergy = Math.min(MAX_ENERGY, storedEnergy + AWCoreConfig.ENERGY_PER_WORK_UNIT.get());
	}

	@Override
	public WorkType getWorkType() {
		return WorkType.RESEARCH;
	}

	@Nullable
	@Override
	public Team getTeam() {
		return level == null ? null : level.getScoreboard().getPlayersTeam(owner.getName());
	}

	@Override
	public Set<WorksiteUpgrade> getUpgrades() {
		return EnumSet.noneOf(WorksiteUpgrade.class);
	}

	@Override
	public Set<WorksiteUpgrade> getValidUpgrades() {
		return EnumSet.noneOf(WorksiteUpgrade.class);
	}

	@Override
	public void addUpgrade(WorksiteUpgrade upgrade) {
		// pas d'améliorations pour la station de recherche
	}

	@Override
	public void removeUpgrade(WorksiteUpgrade upgrade) {
		// pas d'améliorations pour la station de recherche
	}

	@Override
	public void onBlockBroken(BlockState state) {
		// le contenu est vidé par le bloc (dropContents)
	}

	/*
	 * IOwnable
	 */
	@Override
	public void setOwner(Player player) {
		owner = new Owner(player);
	}

	@Override
	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public boolean isOwner(Player player) {
		return owner.isOwnerOrSameTeamOrFriend(player);
	}

	/*
	 * ITorqueTile : la station ne gère pas l'énergie mécanique.
	 */
	@Override
	public double getMaxTorque(@Nullable net.minecraft.core.Direction from) {
		return 0;
	}

	@Override
	public double getTorqueStored(@Nullable net.minecraft.core.Direction from) {
		return 0;
	}

	@Override
	public double addTorque(@Nullable net.minecraft.core.Direction from, double energy) {
		return 0;
	}

	@Override
	public double drainTorque(net.minecraft.core.Direction from, double energy) {
		return 0;
	}

	@Override
	public double getMaxTorqueOutput(net.minecraft.core.Direction from) {
		return 0;
	}

	@Override
	public double getMaxTorqueInput(@Nullable net.minecraft.core.Direction from) {
		return 0;
	}

	@Override
	public boolean canOutputTorque(net.minecraft.core.Direction from) {
		return false;
	}

	@Override
	public boolean canInputTorque(net.minecraft.core.Direction from) {
		return false;
	}

	@Override
	public boolean useOutputRotation(@Nullable net.minecraft.core.Direction from) {
		return false;
	}

	@Override
	public float getClientOutputRotation(net.minecraft.core.Direction from, float delta) {
		return 0f;
	}
}
