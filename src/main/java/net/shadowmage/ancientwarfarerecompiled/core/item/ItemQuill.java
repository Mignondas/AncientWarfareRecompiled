package net.shadowmage.ancientwarfarerecompiled.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfarerecompiled.core.util.BlockTools;
import net.shadowmage.ancientwarfarerecompiled.core.util.WorldTools;

import java.util.List;

/**
 * Plume : outil de recherche. Un clic droit sur une station de recherche lui transfère de l'énergie.
 */
public class ItemQuill extends Item {
	private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "quill_attack_damage");

	private final AWToolMaterial material;

	public ItemQuill(AWToolMaterial material) {
		super(createProperties(material));
		this.material = material;
	}

	private static Item.Properties createProperties(AWToolMaterial material) {
		double attackDamage = 1.0D + material.getAttackDamageBonus();
		return new Item.Properties()
				.stacksTo(1)
				.durability(material.getUses())
				.attributes(ItemAttributeModifiers.builder()
						.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
						.build());
	}

	public AWToolMaterial getMaterial() {
		return material;
	}

	@Override
	public int getEnchantmentValue() {
		return material.getEnchantmentValue();
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return material.getRepairIngredient().test(repair);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
		return true;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
		if (state.getDestroySpeed(level, pos) != 0.0F) {
			stack.hurtAndBreak(2, miner, EquipmentSlot.MAINHAND);
		}
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("guistrings.core.quill.work_mode"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide) {
			return InteractionResultHolder.success(stack);
		}
		BlockPos pos = BlockTools.getBlockClickedOn(player, level, false);
		if (pos != null) {
			WorldTools.getTile(level, pos, IWorkSite.class).filter(t -> t.getWorkType() == IWorkSite.WorkType.RESEARCH).ifPresent(t -> addResearchEnergy(player, stack, t));
		}
		return InteractionResultHolder.success(stack);
	}

	private void addResearchEnergy(Player player, ItemStack stack, IWorkSite researchStation) {
		if (researchStation.hasWork()) {
			researchStation.addEnergyFromPlayer(player);
			stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
		}
	}
}
