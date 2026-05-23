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
import org.joml.Quaternionf;

/**
 * Renderer padrão (sem Veil) — usa RenderType.lines().
 * Também usado como base pelo VeilLaserRenderer.
 */
public class ShowLaserRenderer implements BlockEntityRenderer<ShowLaserBlockEntity> {

    public ShowLaserRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ShowLaserBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!be.isActive()) return;

        try {
            BlockState state = be.getBlockState();
            Direction facing = state.getValue(dev.aerolaser.block.ShowLaserBlock.FACING);

            float r = be.getColorR() / 255f;
            float g = be.getColorG() / 255f;
            float b = be.getColorB() / 255f;
            float len = Math.min(be.getRange(), 64);
            int effectiveZoom = be.getEffectiveZoom();
            float spread = effectiveZoom * 0.018f;
            float sweepAngle = be.getSweepAngle();

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            applyFacingRotation(poseStack, facing);

            int mode = be.getMode();
            if (mode == ShowLaserBlockEntity.MODE_SWEEP ||
                mode == ShowLaserBlockEntity.MODE_SPIN) {
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(sweepAngle)));
            } else if (mode == ShowLaserBlockEntity.MODE_BOUNCE) {
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(sweepAngle)));
            }

            VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
            Matrix4f mat = poseStack.last().pose();
            float nx = 0f, ny = 0f, nz = 1f;

            addLine(vc, mat, 0,       0,      0, len, r, g, b, 1.0f, nx, ny, nz);
            if (effectiveZoom >= 2) {
                addLine(vc, mat,  spread, 0,      0, len, r, g, b, 0.6f, nx, ny, nz);
                addLine(vc, mat, -spread, 0,      0, len, r, g, b, 0.6f, nx, ny, nz);
                addLine(vc, mat,  0,      spread, 0, len, r, g, b, 0.6f, nx, ny, nz);
                addLine(vc, mat,  0,     -spread, 0, len, r, g, b, 0.6f, nx, ny, nz);
            }
            if (effectiveZoom >= 5) {
                float d = spread * 1.5f;
                addLine(vc, mat,  d,  d, 0, len, r, g, b, 0.3f, nx, ny, nz);
                addLine(vc, mat, -d,  d, 0, len, r, g, b, 0.3f, nx, ny, nz);
                addLine(vc, mat,  d, -d, 0, len, r, g, b, 0.3f, nx, ny, nz);
                addLine(vc, mat, -d, -d, 0, len, r, g, b, 0.3f, nx, ny, nz);
            }
            if (effectiveZoom >= 10) {
                float d = spread * 2.5f;
                addLine(vc, mat,  d,  0, 0, len, r, g, b, 0.15f, nx, ny, nz);
                addLine(vc, mat, -d,  0, 0, len, r, g, b, 0.15f, nx, ny, nz);
                addLine(vc, mat,  0,  d, 0, len, r, g, b, 0.15f, nx, ny, nz);
                addLine(vc, mat,  0, -d, 0, len, r, g, b, 0.15f, nx, ny, nz);
            }

            poseStack.popPose();
        } catch (Exception ignored) {}
    }

    protected void addLine(VertexConsumer vc, Matrix4f mat,
                         float ox, float oy, float startZ, float endZ,
                         float r, float g, float b, float alpha,
                         float nx, float ny, float nz) {
        vc.addVertex(mat, ox, oy, startZ).setColor(r, g, b, alpha).setNormal(nx, ny, nz);
        vc.addVertex(mat, ox, oy, endZ  ).setColor(r, g, b, 0f   ).setNormal(nx, ny, nz);
    }

    protected void applyFacingRotation(PoseStack ps, Direction facing) {
        switch (facing) {
            case NORTH -> ps.mulPose(new Quaternionf().rotateY((float)  Math.PI));
            case SOUTH -> {}
            case EAST  -> ps.mulPose(new Quaternionf().rotateY((float) (-Math.PI / 2)));
            case WEST  -> ps.mulPose(new Quaternionf().rotateY((float) ( Math.PI / 2)));
            case UP    -> ps.mulPose(new Quaternionf().rotateX((float) ( Math.PI / 2)));
            case DOWN  -> ps.mulPose(new Quaternionf().rotateX((float) (-Math.PI / 2)));
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ShowLaserBlockEntity be) { return true; }

    @Override
    public int getViewDistance() { return 128; }
}
