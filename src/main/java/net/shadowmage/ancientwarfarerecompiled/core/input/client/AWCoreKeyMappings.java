package net.shadowmage.ancientwarfarerecompiled.core.input.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Touches alternatives des objets du mod (dans l'ordre ALT_FUNCTION_1 à ALT_FUNCTION_5).
 */
@EventBusSubscriber(modid = AncientWarfareRecompiled.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AWCoreKeyMappings {
	public static final String CATEGORY = "key.category.ancientwarfarerecompiled";

	public static final KeyMapping ALT_ITEM_USE_1 = create("key.ancientwarfarerecompiled.alt_item_use_1", GLFW.GLFW_KEY_Z);
	public static final KeyMapping ALT_ITEM_USE_2 = create("key.ancientwarfarerecompiled.alt_item_use_2", GLFW.GLFW_KEY_X);
	public static final KeyMapping ALT_ITEM_USE_3 = create("key.ancientwarfarerecompiled.alt_item_use_3", GLFW.GLFW_KEY_C);
	public static final KeyMapping ALT_ITEM_USE_4 = create("key.ancientwarfarerecompiled.alt_item_use_4", GLFW.GLFW_KEY_V);
	public static final KeyMapping ALT_ITEM_USE_5 = create("key.ancientwarfarerecompiled.alt_item_use_5", GLFW.GLFW_KEY_B);

	private static final List<KeyMapping> ALL = List.of(ALT_ITEM_USE_1, ALT_ITEM_USE_2, ALT_ITEM_USE_3, ALT_ITEM_USE_4, ALT_ITEM_USE_5);

	private AWCoreKeyMappings() {}

	private static KeyMapping create(String name, int key) {
		return new KeyMapping(name, ItemKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM, key, CATEGORY);
	}

	public static List<KeyMapping> all() {
		return ALL;
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		ALL.forEach(event::register);
	}
}
