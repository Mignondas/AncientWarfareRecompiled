package net.shadowmage.ancientwarfarerecompiled.core.input.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.shadowmage.ancientwarfarerecompiled.core.input.IItemKeyInterface;

/**
 * Les touches alternatives ne sont actives que lorsqu'un objet compatible est tenu en main.
 */
@OnlyIn(Dist.CLIENT)
public final class ItemKeyConflictContext implements IKeyConflictContext {
	public static final ItemKeyConflictContext INSTANCE = new ItemKeyConflictContext();

	private ItemKeyConflictContext() {}

	@Override
	public boolean isActive() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null || mc.player == null || mc.level == null) {
			return false;
		}
		return mc.player.getMainHandItem().getItem() instanceof IItemKeyInterface || mc.player.getOffhandItem().getItem() instanceof IItemKeyInterface;
	}

	@Override
	public boolean conflicts(IKeyConflictContext other) {
		return this == other;
	}
}
