package net.shadowmage.ancientwarfarerecompiled.core.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.research.PlayerResearch;

/**
 * Attachments du module Core (données rattachées aux joueurs).
 */
public final class AWCoreAttachments {
	private AWCoreAttachments() {}

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AncientWarfareRecompiled.MODID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerResearch>> PLAYER_RESEARCH = ATTACHMENTS.register("player_research",
			() -> AttachmentType.builder(PlayerResearch::new).serialize(PlayerResearch.CODEC).copyOnDeath().build());

	public static void register(IEventBus modEventBus) {
		ATTACHMENTS.register(modEventBus);
	}
}
