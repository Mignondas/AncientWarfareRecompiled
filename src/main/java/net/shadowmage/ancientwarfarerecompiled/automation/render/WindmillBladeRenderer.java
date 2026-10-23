package net.shadowmage.ancientwarfarerecompiled.automation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileWindmillBlade;

/**
 * Dessine le rotor de la pale d'éolienne, tourné autour de son moyeu selon la vitesse de la tuile.
 */
public class WindmillBladeRenderer implements BlockEntityRenderer<TileWindmillBlade> {
	public static final ModelResourceLocation ROTOR = ModelResourceLocation.standalone(
			ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "block/windmill_blade_rotor"));

	/** Moyeu en coordonnées de bloc (0..1), axe Z. */
	private static final float HUB_X = 0.5f;
	private static final float HUB_Y = 1.0f;
	private static final float HUB_Z = 0.5f;

	public WindmillBladeRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(TileWindmillBlade blade, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
		if (blade.getLevel() == null) {
			return;
		}
		float angle = ((blade.getLevel().getGameTime() + partialTick) * blade.getSpeed()) % 360f;
		BakedModel rotor = Minecraft.getInstance().getModelManager().getModel(ROTOR);
		poseStack.pushPose();
		poseStack.translate(HUB_X, HUB_Y, HUB_Z);
		poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
		poseStack.translate(-HUB_X, -HUB_Y, -HUB_Z);
		Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
				buffer.getBuffer(RenderType.cutout()), null, rotor, 1.0f, 1.0f, 1.0f, light, overlay);
		poseStack.popPose();
	}

	/** Le rotor dépasse le bloc : on élargit la zone de rendu pour qu'il ne disparaisse pas. */
	@Override
	public AABB getRenderBoundingBox(TileWindmillBlade blade) {
		return AABB.encapsulatingFullBlocks(blade.getBlockPos().offset(-3, -3, -3), blade.getBlockPos().offset(4, 4, 4));
	}
}
