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
            float len = Math.min(be.getRange(), 32); // limita para segurança
            float hw = Math.max(0.01f, be.getEffectiveZoom() * 0.012f);
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

            // Usa LINES ao invés de translucent para maior compatibilidade com Sodium/mobile
            VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
            Matrix4f mat = poseStack.last().pose();

            // Desenha o feixe como linhas simples (compatível com mobile/Sodium)
            int fullBright = 0xF000F0;
            // Linha central
            vc.addVertex(mat, 0, 0, 0).setColor(r, g, b, 1f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat, 0, 0, len).setColor(r, g, b, 0f).setNormal(poseStack.last(), 0, 0, 1);
            // Linhas de espessura (simulam o feixe)
            vc.addVertex(mat,  hw,  hw, 0).setColor(r, g, b, 0.7f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat,  hw,  hw, len).setColor(r, g, b, 0f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat, -hw,  hw, 0).setColor(r, g, b, 0.7f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat, -hw,  hw, len).setColor(r, g, b, 0f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat,  hw, -hw, 0).setColor(r, g, b, 0.7f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat,  hw, -hw, len).setColor(r, g, b, 0f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat, -hw, -hw, 0).setColor(r, g, b, 0.7f).setNormal(poseStack.last(), 0, 0, 1);
            vc.addVertex(mat, -hw, -hw, len).setColor(r, g, b, 0f).setNormal(poseStack.last(), 0, 0, 1);

            poseStack.popPose();
        } catch (Exception e) {
            // Silenciosamente ignora erros de renderização para evitar crashes
        }
    }

    private void applyFacingRotation(PoseStack ps, Direction facing) {
        switch (facing) {
            case NORTH -> ps.mulPose(new Quaternionf().rotateY((float) Math.PI));
            case SOUTH -> {}
            case EAST  -> ps.mulPose(new Quaternionf().rotateY((float) (-Math.PI / 2)));
            case WEST  -> ps.mulPose(new Quaternionf().rotateY((float) (Math.PI / 2)));
            case UP    -> ps.mulPose(new Quaternionf().rotateX((float) (Math.PI / 2)));
            case DOWN  -> ps.mulPose(new Quaternionf().rotateX((float) (-Math.PI / 2)));
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ShowLaserBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
