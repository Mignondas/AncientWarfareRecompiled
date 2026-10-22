package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfarerecompiled.automation.block.TorqueTier;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.core.config.AWCoreConfig;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.TorqueCell;

import javax.annotation.Nullable;

/**
 * Générateur Stirling : brûle du combustible (un emplacement) et ajoute de l'énergie de couple sur la face FACING.
 */
public class TileStirlingGenerator extends TileTorqueBase {
	private static final String OUTPUT_TAG = "torqueEnergy";
	private static final String BURN_TAG = "burnTicks";
	private static final String BURN_BASE_TAG = "burnTicksBase";
	private static final String INVENTORY_TAG = "inventory";

	public final ItemStackHandler fuelHandler = new ItemStackHandler(1) {
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return burnDuration(stack) > 0 ? super.insertItem(slot, stack, simulate) : stack;
		}
	};

	private final TorqueCell torqueCell;
	private int burnTime = 0;
	private int burnTimeBase = 0;

	public TileStirlingGenerator(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.STIRLING_GENERATOR.get(), pos, state);
		torqueCell = new TorqueCell(0, 4, 1600, TorqueTier.MEDIUM.efficiency);
	}

	/** Durée de combustion en ticks d'un objet, 0 s'il ne brûle pas. */
	public static int burnDuration(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
	}

	public void serverTick() {
		torqueIn = torqueCell.getEnergy() - prevEnergy;
		if (burnTime <= 0 && torqueCell.getEnergy() < torqueCell.getMaxEnergy()) {
			int ticks = burnDuration(fuelHandler.getStackInSlot(0));
			if (ticks > 0) {
				fuelHandler.extractItem(0, 1, false);
				burnTime = ticks;
				burnTimeBase = ticks;
				setChanged();
			}
		} else if (burnTime > 0) {
			torqueCell.setEnergy(torqueCell.getEnergy() + AWCoreConfig.STIRLING_GENERATOR_OUTPUT.get());
			burnTime--;
			if (burnTime == 0) {
				setChanged();
			}
		}
		torqueOut = transferPowerTo(getPrimaryFacing());
		torqueLoss = applyPowerDrain(torqueCell);
		prevEnergy = torqueCell.getEnergy();
	}

	public int getBurnTime() {
		return burnTime;
	}

	public int getBurnTimeBase() {
		return burnTimeBase;
	}

	@Override
	public boolean canOutputTorque(Direction towards) {
		return towards == getPrimaryFacing();
	}

	@Override
	public boolean canInputTorque(@Nullable Direction from) {
		return false;
	}

	@Override
	public double getMaxTorque(@Nullable Direction from) {
		return torqueCell.getMaxEnergy();
	}

	@Override
	public double getTorqueStored(@Nullable Direction from) {
		return torqueCell.getEnergy();
	}

	@Override
	public double addTorque(@Nullable Direction from, double energy) {
		return 0;
	}

	@Override
	public double drainTorque(Direction from, double energy) {
		return from == getPrimaryFacing() ? torqueCell.drainEnergy(energy) : 0;
	}

	@Override
	public double getMaxTorqueOutput(Direction from) {
		return from == getPrimaryFacing() ? torqueCell.getMaxTickOutput() : 0;
	}

	@Override
	public double getMaxTorqueInput(@Nullable Direction from) {
		return 0;
	}

	@Override
	protected double getTotalTorque() {
		return torqueCell.getEnergy();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putDouble(OUTPUT_TAG, torqueCell.getEnergy());
		tag.putInt(BURN_TAG, burnTime);
		tag.putInt(BURN_BASE_TAG, burnTimeBase);
		tag.put(INVENTORY_TAG, fuelHandler.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		torqueCell.setEnergy(tag.getDouble(OUTPUT_TAG));
		burnTime = tag.getInt(BURN_TAG);
		burnTimeBase = tag.getInt(BURN_BASE_TAG);
		if (tag.contains(INVENTORY_TAG)) {
			fuelHandler.deserializeNBT(registries, tag.getCompound(INVENTORY_TAG));
		}
	}
}
