package dev.aerolaser.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
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
 * Renderer de glow para o GlowLamp — sem shaders, otimizado para mobile.
 *
 * Técnica "billboard em camadas" inspirada em mods como Actions and Stuff:
 *  - 3 quads sobrepostos com a textura gaussiana (glow.png)
 *  - Cada camada ligeiramente deslocada em Z para evitar flickering
 *  - Fullbright (sem influência da iluminação do mundo)
 *  - Efeito de piscar via game time (sem tick no BE)
 *
 * Correção de sublevel: o packet agora busca em todos os levels do servidor.
 */
public class GlowLampRenderer implements BlockEntityRenderer<GlowLampBlockEntity> {

    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "textures/block/glow.png");

    // Distância máxima de render (24 blocos)
    private static final double MAX_DIST_SQ = 24.0 * 24.0;

    // Full-bright: não sofre influência da luz ambiente
    private static final int FULL_BRIGHT = 0xF000F0;

    public GlowLampRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(GlowLampBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!be.isActive()) return;

        // Culling por distância
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 center = Vec3.atCenterOf(be.getBlockPos());
        if (cam.distanceToSqr(center) > MAX_DIST_SQ) return;

        // ── Efeito de piscar ─────────────────────────────────────────────────
        if (be.isBlinkEnabled()) {
            // Usa game time para piscar — sem tick no BE, muito leve
            long gameTime = mc.level.getGameTime();
            // blinkSpeed 1=lento(40 ticks/ciclo) → 20=rápido(2 ticks/ciclo)
            int period = Math.max(2, 42 - be.getBlinkSpeed() * 2);
            if ((gameTime % period) < (period / 2)) return; // "off" nesta metade do ciclo
        }

        float r = be.getColorR() / 255f;
        float g = be.getColorG() / 255f;
        float b = be.getColorB() / 255f;
        float s = be.getSize();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Faz o billboard sempre olhar para a câmera
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentCull(GLOW_TEX));
        Matrix4f mat = poseStack.last().pose();

        // ── 3 camadas com Z offset para evitar flickering ───────────────────
        // Cada camada avança 0.001 em Z (em direção à câmera após a rotação)
        // para que o depth buffer não cause Z-fighting entre elas.

        // Camada 3 — halo externo, sutil
        drawQuad(mat, vc, s * 1.7f, r, g, b, 0.16f,  0.000f);
        // Camada 2 — glow médio
        drawQuad(mat, vc, s * 0.85f, r, g, b, 0.52f, 0.001f);
        // Camada 1 — núcleo brilhante
        drawQuad(mat, vc, s * 0.35f, r, g, b, 0.95f, 0.002f);

        poseStack.popPose();
    }

    /**
     * Desenha um quad camera-facing de tamanho s×s.
     * zOffset empurra o quad ligeiramente para frente (em espaço câmera)
     * para evitar Z-fighting entre as camadas.
     */
    private void drawQuad(Matrix4f mat, VertexConsumer vc,
                          float s, float r, float g, float b, float a, float zOff) {
        // UV: (0,0)=topo-esquerda, (1,1)=baixo-direita
        vc.addVertex(mat, -s, -s, zOff).setColor(r,g,b,a).setUv(0,0)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat,  s, -s, zOff).setColor(r,g,b,a).setUv(1,0)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat,  s,  s, zOff).setColor(r,g,b,a).setUv(1,1)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat, -s,  s, zOff).setColor(r,g,b,a).setUv(0,1)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
    }

    @Override public boolean shouldRenderOffScreen(GlowLampBlockEntity be) { return false; }
    @Override public int getViewDistance() { return 24; }
}
