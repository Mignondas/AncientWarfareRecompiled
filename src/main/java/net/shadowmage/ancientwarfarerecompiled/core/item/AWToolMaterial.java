package net.shadowmage.ancientwarfarerecompiled.core.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Matériaux des outils du module Core (marteaux et plumes).
 *
 * Valeurs identiques aux matériaux de base de Minecraft (bois, pierre, fer, or, diamant).
 * Remplace l'énumération Item.ToolMaterial de 1.12.
 */
public enum AWToolMaterial implements Tier {
	WOOD(59, 2.0F, 0.0F, 15, BlockTags.INCORRECT_FOR_WOODEN_TOOL, () -> Ingredient.of(ItemTags.PLANKS)),
	STONE(131, 4.0F, 1.0F, 5, BlockTags.INCORRECT_FOR_STONE_TOOL, () -> Ingredient.of(ItemTags.STONE_TOOL_MATERIALS)),
	IRON(250, 6.0F, 2.0F, 14, BlockTags.INCORRECT_FOR_IRON_TOOL, () -> Ingredient.of(Items.IRON_INGOT)),
	GOLD(32, 12.0F, 0.0F, 22, BlockTags.INCORRECT_FOR_GOLD_TOOL, () -> Ingredient.of(Items.GOLD_INGOT)),
	DIAMOND(1561, 8.0F, 3.0F, 10, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, () -> Ingredient.of(Items.DIAMOND));

	private final int uses;
	private final float speed;
	private final float attackDamageBonus;
	private final int enchantmentValue;
	private final TagKey<Block> incorrectBlocks;
	private final java.util.function.Supplier<Ingredient> repairIngredient;

	AWToolMaterial(int uses, float speed, float attackDamageBonus, int enchantmentValue, TagKey<Block> incorrectBlocks, java.util.function.Supplier<Ingredient> repairIngredient) {
		this.uses = uses;
		this.speed = speed;
		this.attackDamageBonus = attackDamageBonus;
		this.enchantmentValue = enchantmentValue;
		this.incorrectBlocks = incorrectBlocks;
		this.repairIngredient = repairIngredient;
	}

	@Override
	public int getUses() {
		return uses;
	}

	@Override
	public float getSpeed() {
		return speed;
	}

	@Override
	public float getAttackDamageBonus() {
		return attackDamageBonus;
	}

	@Override
	public TagKey<Block> getIncorrectBlocksForDrops() {
		return incorrectBlocks;
	}

	@Override
	public int getEnchantmentValue() {
		return enchantmentValue;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return repairIngredient.get();
	}
}
