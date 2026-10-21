package net.shadowmage.ancientwarfarerecompiled.core.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.UUID;

/**
 * Joueur factice utilisé par les automates et les véhicules pour agir sur le monde
 * (casser, placer, cliquer) au nom du mod.
 */
public class AWFakePlayer extends FakePlayer {
	private static final String PLAYER_NAME = "AncientWarfareFakePlayer";
	private static AWFakePlayer instance;

	private AWFakePlayer(ServerLevel level) {
		super(level, new GameProfile(UUID.nameUUIDFromBytes(PLAYER_NAME.getBytes()), PLAYER_NAME));
	}

	/**
	 * Renvoie le joueur factice du monde serveur, ou null si le monde n'est pas un monde serveur.
	 */
	public static AWFakePlayer get(Level level) {
		if (instance == null && level instanceof ServerLevel serverLevel) {
			instance = new AWFakePlayer(serverLevel);
		}
		return instance;
	}

	@Override
	public void onInsideBlock(BlockState state) {
		// aucune action : le joueur factice ne réagit pas aux blocs qu'il traverse
	}

	/**
	 * À appeler quand le monde est déchargé, pour libérer l'instance.
	 */
	public static void onWorldUnload() {
		instance = null;
	}
}
