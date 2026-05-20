package dev.aerolaser.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/**
 * Renders the laser beam as a translucent coloured line/quad extending
 * from the block face toward the facing direction, with zoom support.
 *
 * Zoom  1 = single-pixel wide ray
 * Zoom 20 = wide cone (~30° spread)
 */
public class ShowLaserRenderer implements BlockEntityRenderer<ShowLaserBlockEntity> {

    public ShowLaserRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ShowLaserBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {

        if (!be.isActive()) return;

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(dev.aerolaser.block.ShowLaserBlock.FACING);

        float r = be.getColorR() / 255f;
        float g = be.getColorG() / 255f;
        float b = be.getColorB() / 255f;
        float alpha = 0.85f;

        int zoom  = be.getEffectiveZoom();
        float len = be.getRange();
        // Beam half-width: zoom 1 = 0.01, zoom 20 = 0.30
        float hw = zoom * 0.015f;

        float sweepAngle = be.getSweepAngle();

        poseStack.pushPose();
        // Move origin to center of block face
        poseStack.translate(0.5, 0.5, 0.5);

        // Rotate stack to face the beam direction
        applyFacingRotation(poseStack, facing);

        // Sweep rotation around Y axis (for SWEEP / SPIN modes)
        if (be.getMode() == ShowLaserBlockEntity.MODE_SWEEP ||
            be.getMode() == ShowLaserBlockEntity.MODE_SPIN) {
            poseStack.mulPose(new org.joml.Quaternionf().rotateY((float) Math.toRadians(sweepAngle)));
        }
        // Bounce rotates around X
        if (be.getMode() == ShowLaserBlockEntity.MODE_BOUNCE) {
            poseStack.mulPose(new org.joml.Quaternionf().rotateX((float) Math.toRadians(sweepAngle)));
        }

        VertexConsumer vc = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f mat = poseStack.last().pose();

        // Draw a simple quad beam along +Z (we rotated to facing already)
        drawBeam(mat, vc, hw, len, r, g, b, alpha);

        poseStack.popPose();
    }

    /** Draws a flat rectangular beam of half-width hw and length len along +Z. */
    private void drawBeam(Matrix4f mat, VertexConsumer vc,
                          float hw, float len, float r, float g, float b, float a) {
        int light = 0xF000F0; // full bright
        // Bottom face
        vc.addVertex(mat, -hw, -hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat,  hw, -hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat,  hw, -hw, len).setColor(r,g,b,0).setLight(light);
        vc.addVertex(mat, -hw, -hw, len).setColor(r,g,b,0).setLight(light);
        // Top face
        vc.addVertex(mat, -hw,  hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat,  hw,  hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat,  hw,  hw, len).setColor(r,g,b,0).setLight(light);
        vc.addVertex(mat, -hw,  hw, len).setColor(r,g,b,0).setLight(light);
        // Left face
        vc.addVertex(mat, -hw, -hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat, -hw,  hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat, -hw,  hw, len).setColor(r,g,b,0).setLight(light);
        vc.addVertex(mat, -hw, -hw, len).setColor(r,g,b,0).setLight(light);
        // Right face
        vc.addVertex(mat,  hw, -hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat,  hw,  hw, 0).setColor(r,g,b,a).setLight(light);
        vc.addVertex(mat,  hw,  hw, len).setColor(r,g,b,0).setLight(light);
        vc.addVertex(mat,  hw, -hw, len).setColor(r,g,b,0).setLight(light);
    }

    private void applyFacingRotation(PoseStack ps, Direction facing) {
        switch (facing) {
            case NORTH -> ps.mulPose(new org.joml.Quaternionf().rotateY((float)Math.PI));
            case SOUTH -> {} // default
            case EAST  -> ps.mulPose(new org.joml.Quaternionf().rotateY((float)(-Math.PI/2)));
            case WEST  -> ps.mulPose(new org.joml.Quaternionf().rotateY((float)(Math.PI/2)));
            case UP    -> ps.mulPose(new org.joml.Quaternionf().rotateX((float)(Math.PI/2)));
            case DOWN  -> ps.mulPose(new org.joml.Quaternionf().rotateX((float)(-Math.PI/2)));
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ShowLaserBlockEntity be) {
        return true; // always render even when block is off-screen (long beam)
    }
}
