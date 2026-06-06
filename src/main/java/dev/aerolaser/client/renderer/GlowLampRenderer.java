package dev.aerolaser.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.GlowLampBlock;
import dev.aerolaser.blockentity.GlowLampBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renderer de glow emissivo — sem shaders, otimizado para mobile.
 *
 * Melhorias v3:
 *  - Textura 128x128 gaussiana retangular (formato farol/trator)
 *  - RenderType com additive blending real (GL_SRC_ALPHA, GL_ONE)
 *    → efeito emissivo genuíno, sem dependência de shaders
 *  - Billboard offsetado para sair da face correta (baseado em FACING)
 *  - Piscar via game time, sem tick no BE
 *  - Sublevel fix: o packet agora usa containerId
 */
public class GlowLampRenderer implements BlockEntityRenderer<GlowLampBlockEntity> {

    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "textures/block/glow.png");

    // RenderType com additive blending: src=SRC_ALPHA, dst=ONE
    // Isso faz a textura SOMAR com o fundo → efeito emissivo real
    private static final RenderType GLOW_ADDITIVE = RenderType.create(
            "aerolaser_glow_additive",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(GLOW_TEX, false, false))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "aerolaser_additive",
                            () -> {
                                RenderSystem.enableBlend();
                                // Additive: destino = ONE (a cor se soma ao fundo)
                                RenderSystem.blendFunc(
                                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE
                                );
                            },
                            () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }
                    ))
                    .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderType.COLOR_WRITE) // não escreve no depth buffer
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(false)
    );

    private static final double MAX_DIST_SQ = 32.0 * 32.0;
    private static final int FULL_BRIGHT = 0xF000F0;

    public GlowLampRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(GlowLampBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!be.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Culling por distância
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 center = Vec3.atCenterOf(be.getBlockPos());
        if (cam.distanceToSqr(center) > MAX_DIST_SQ) return;

        // Piscar via game time — sem tick no BE
        if (be.isBlinkEnabled()) {
            long t = mc.level.getGameTime();
            int period = Math.max(2, 42 - be.getBlinkSpeed() * 2);
            if ((t % period) < (period / 2)) return;
        }

        float r = be.getColorR() / 255f;
        float g = be.getColorG() / 255f;
        float b = be.getColorB() / 255f;
        float s = be.getSize();

        // Direção da face da lâmpada
        Direction facing = be.getBlockState().getValue(GlowLampBlock.FACING);

        poseStack.pushPose();

        // Posiciona o billboard levemente fora da face da lâmpada
        // para o glow aparecer na frente do bloco, não dentro dele
        double ox = facing.getStepX() * 0.18;
        double oy = facing.getStepY() * 0.18;
        double oz = facing.getStepZ() * 0.18;
        poseStack.translate(0.5 + ox, 0.5 + oy, 0.5 + oz);

        // Billboard: sempre olha para a câmera
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());

        VertexConsumer vc = bufferSource.getBuffer(GLOW_ADDITIVE);
        Matrix4f mat = poseStack.last().pose();

        // 3 camadas com Z-offset para evitar Z-fighting
        // Com additive blending, as camadas se somam naturalmente
        // criando um núcleo muito brilhante e halo suave
        drawQuad(mat, vc, s * 1.8f, r, g, b, 0.20f, 0.000f); // halo externo
        drawQuad(mat, vc, s * 0.9f, r, g, b, 0.55f, 0.001f); // glow médio
        drawQuad(mat, vc, s * 0.38f,r, g, b, 1.00f, 0.002f); // núcleo máximo

        poseStack.popPose();
    }

    private void drawQuad(Matrix4f mat, VertexConsumer vc,
                          float s, float r, float g, float b, float a, float z) {
        vc.addVertex(mat,-s,-s,z).setColor(r,g,b,a).setUv(0,0)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat, s,-s,z).setColor(r,g,b,a).setUv(1,0)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat, s, s,z).setColor(r,g,b,a).setUv(1,1)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
        vc.addVertex(mat,-s, s,z).setColor(r,g,b,a).setUv(0,1)
          .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0,0,1);
    }

    @Override public boolean shouldRenderOffScreen(GlowLampBlockEntity be) { return true; }
    @Override public int getViewDistance() { return 32; }
}
