package net.shadowmage.ancientwarfarerecompiled.automation.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileMailbox;

/** Client -> serveur : réglages d'une boîte aux lettres (nom, destination, privée). */
public record PacketMailboxSettings(BlockPos pos, String name, String target, boolean priv) implements CustomPacketPayload {
	public static final Type<PacketMailboxSettings> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "mailbox_settings"));
	public static final StreamCodec<ByteBuf, PacketMailboxSettings> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, PacketMailboxSettings::pos,
			ByteBufCodecs.STRING_UTF8, PacketMailboxSettings::name,
			ByteBufCodecs.STRING_UTF8, PacketMailboxSettings::target,
			ByteBufCodecs.BOOL, PacketMailboxSettings::priv,
			PacketMailboxSettings::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Validation : le joueur doit être proche de la boîte, et le bloc doit être une boîte aux lettres. */
	public static void execute(Player player, PacketMailboxSettings packet) {
		if (!(player instanceof ServerPlayer serverPlayer) || !(serverPlayer.level() instanceof ServerLevel level)) {
			return;
		}
		if (player.distanceToSqr(packet.pos().getX() + 0.5, packet.pos().getY() + 0.5, packet.pos().getZ() + 0.5) > 64.0) {
			return;
		}
		if (level.getBlockEntity(packet.pos()) instanceof TileMailbox mailbox) {
			mailbox.applySettings(level, packet.name(), packet.target(), packet.priv());
		}
	}
}
