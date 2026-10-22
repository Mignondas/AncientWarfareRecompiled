package net.shadowmage.ancientwarfarerecompiled.core.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.shadowmage.ancientwarfarerecompiled.core.menu.ResearchBookMenu;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Livre de recherche : se lie au premier joueur qui fait un clic droit dessus.
 * Seul un livre lié peut être placé dans la station de recherche.
 */
public class ItemResearchBook extends Item {
	public static final String RESEARCHER_TAG = "researcherName";

	public ItemResearchBook() {
		super(new Item.Properties().stacksTo(1));
	}

	@Nullable
	public static String getResearcherName(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof ItemResearchBook)) {
			return null;
		}
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return null;
		}
		CompoundTag tag = data.copyTag();
		return tag.contains(RESEARCHER_TAG) ? tag.getString(RESEARCHER_TAG) : null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		String owner = getResearcherName(stack);
		if (owner == null) {
			if (!level.isClientSide) {
				String name = player.getScoreboardName();
				CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(RESEARCHER_TAG, name));
				player.displayClientMessage(Component.translatable("guistrings.research.book_bound"), true);
			}
		} else if (!owner.equals(player.getScoreboardName())) {
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("guistrings.research.book_not_yours"), true);
			}
		} else if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new ResearchBookMenu(id, inv),
					Component.translatable("guistrings.research.book_title")));
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		String name = getResearcherName(stack);
		if (name == null) {
			tooltip.add(Component.translatable("guistrings.research.researcher_name").append(": ").append(Component.translatable("guistrings.research.no_researcher")));
			tooltip.add(Component.translatable("guistrings.research.right_click_to_bind"));
		} else {
			tooltip.add(Component.translatable("guistrings.research.researcher_name").append(": " + name));
		}
	}
}
