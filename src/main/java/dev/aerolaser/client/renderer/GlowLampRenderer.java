package dev.aerolaser.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.blockentity.GlowLampBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renderiza o efeito de glow/bloom do GlowLamp.
 *
 * Técnica: billboard com 3 camadas de quads usando a textura
 * glow.png (gradiente radial branco→transparente).
 * Completamente sem shaders — compatível com celular/MobileGlues.
 *
 * Performance:
 *  - Culling a 24 blocos de distância
 *  - Apenas 12 vértices por bloco (3 quads × 4 vértices)
 *  - Usa o buffer translucent padrão do Minecraft
 */
public class GlowLampRenderer implements BlockEntityRenderer<GlowLampBlockEntity> {

    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "textures/block/glow.png");

    // Distância máxima de render ao quadrado (24 blocos)
    private static final double MAX_DIST_SQ = 24.0 * 24.0;

    // Luz máxima (fullbright) para o glow aparecer mesmo no escuro
    private static final int FULL_BRIGHT = 0xF000F0;

    public GlowLampRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(GlowLampBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {

        if (!be.isActive()) return;

        // Culling por distância — evita render desnecessário longe
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 blockCenter = Vec3.atCenterOf(be.getBlockPos());
        if (cam.distanceToSqr(blockCenter) > MAX_DIST_SQ) return;

        float r = be.getColorR() / 255f;
        float g = be.getColorG() / 255f;
        float b = be.getColorB() / 255f;
        float s = be.getSize();

        try {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            // Faz o quad sempre olhar para a câmera
            poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());

            VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentCull(GLOW_TEX));
            Matrix4f mat = poseStack.last().pose();

            // 3 camadas: centro brilhante → halo externo suave
            // Camada 1 — núcleo brilhante
            quad(vc, mat, s * 0.35f, r, g, b, 0.92f);
            // Camada 2 — glow médio
            quad(vc, mat, s * 0.80f, r, g, b, 0.50f);
            // Camada 3 — halo externo
            quad(vc, mat, s * 1.60f, r, g, b, 0.18f);

            poseStack.popPose();
        } catch (Exception ignored) {}
    }

    /**
     * Desenha um quad plano centralizado na origem, tamanho s×s,
     * com a textura de glow e cor RGB+alpha.
     */
    private void quad(VertexConsumer vc, Matrix4f mat,
                      float s, float r, float g, float b, float a) {
        vc.addVertex(mat, -s, -s, 0).setColor(r,g,b,a).setUv(0,1)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat,  s, -s, 0).setColor(r,g,b,a).setUv(1,1)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat,  s,  s, 0).setColor(r,g,b,a).setUv(1,0)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat, -s,  s, 0).setColor(r,g,b,a).setUv(0,0)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
    }

    @Override
    public boolean shouldRenderOffScreen(GlowLampBlockEntity be) { return false; }

    @Override
    public int getViewDistance() { return 24; }
}
