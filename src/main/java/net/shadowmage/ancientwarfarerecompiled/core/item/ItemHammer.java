package net.shadowmage.ancientwarfarerecompiled.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IHammerRotatable;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfarerecompiled.core.util.WorldTools;

import java.util.List;
import java.util.Optional;

/**
 * Marteau : mode travail (énergie aux chantiers) ou mode rotation (tourne les blocs compatibles).
 * Le mode se change avec la touche alternative 1.
 */
public class ItemHammer extends Item implements IItemKeyInterface {
	public static final String WORK_MODE_TAG = "workMode";
	private static final double REACH = 5.0D;
	private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "hammer_attack_damage");
	private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "hammer_attack_speed");

	private final AWToolMaterial material;

	public ItemHammer(AWToolMaterial material) {
		super(createProperties(material));
		this.material = material;
	}

	private static Item.Properties createProperties(AWToolMaterial material) {
		// valeurs de 1.12 : dégâts de base 4 + bonus du matériau, vitesse d'attaque -3.1
		return new Item.Properties()
				.stacksTo(1)
				.durability(material.getUses())
				.attributes(ItemAttributeModifiers.builder()
						.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_ID, 4.0D + material.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
						.add(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_ID, -3.1D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
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
		tooltip.add(Component.translatable("guistrings.core.hammer.use_primary_item_key", Component.keybind("key.ancientwarfarerecompiled.alt_item_use_1")));
		if (isWorkMode(stack)) {
			tooltip.add(Component.translatable("guistrings.core.hammer.work_mode"));
		} else {
			tooltip.add(Component.translatable("guistrings.core.hammer.rotate_mode"));
		}
	}

	@Override
	public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
		return altFunction == ItemAltFunction.ALT_FUNCTION_1;
	}

	@Override
	public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
		if (altFunction != ItemAltFunction.ALT_FUNCTION_1) {
			return;
		}
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(WORK_MODE_TAG, !tag.getBoolean(WORK_MODE_TAG)));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.3F, 0.6F);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			return InteractionResultHolder.success(stack);
		}
		BlockHitResult hit = raycast(player);
		if (hit == null) {
			return InteractionResultHolder.pass(stack);
		}
		BlockPos pos = hit.getBlockPos();
		if (isWorkMode(stack)) {
			Optional<IWorkSite> site = WorldTools.getTile(level, pos, IWorkSite.class).filter(IWorkSite::hasWork);
			if (site.isPresent()) {
				site.get().addEnergyFromPlayer(player);
				playSound(level, pos, SoundEvents.PISTON_CONTRACT);
			} else if (!level.isEmptyBlock(pos)) {
				playBlockSound(level, pos, level.getBlockState(pos));
			}
		} else if (!level.isEmptyBlock(pos)) {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof IHammerRotatable rotatable && rotatable.rotateBlock(level, pos, hit.getDirection())) {
				playSound(level, pos, SoundEvents.PISTON_EXTEND);
			} else {
				playBlockSound(level, pos, state);
			}
		}
		return InteractionResultHolder.success(stack);
	}

	private static boolean isWorkMode(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(WORK_MODE_TAG);
	}

	private static BlockHitResult raycast(Player player) {
		Vec3 start = player.getEyePosition(1.0F);
		Vec3 end = start.add(player.getViewVector(1.0F).scale(REACH));
		HitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		return hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit : null;
	}

	private static void playSound(Level level, BlockPos pos, net.minecraft.sounds.SoundEvent sound) {
		level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.2F, level.random.nextFloat() * 0.15F + 0.6F);
	}

	private static void playBlockSound(Level level, BlockPos pos, BlockState state) {
		net.minecraft.world.level.block.SoundType sound = state.getSoundType();
		level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, sound.getVolume() * 0.5F, sound.getPitch() * 0.8F);
	}
}
