package dev.aerolaser.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.emitters.ParticleEmitter;
import foundry.veil.api.quasar.emitters.ParticleSystemManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import dev.aerolaser.AeroLaserMod;

/**
 * Renderer que usa o Veil para:
 *  - Partículas Quasar no ponto de impacto do laser (faíscas coloridas)
 *  - Shader customizado com bloom para o feixe
 *
 * Só é carregado se o Veil estiver presente (verificado em ShowLaserRenderer).
 */
public class VeilLaserRenderer implements BlockEntityRenderer<ShowLaserBlockEntity> {

    // IDs dos emissores de partícula Quasar
    private static final ResourceLocation SPARK_EMITTER =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "laser_spark");
    private static final ResourceLocation GLOW_EMITTER =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "laser_glow");

    // Renderer de fallback (linhas simples) para quando shaders não suportados
    private final ShowLaserRenderer fallback;

    public VeilLaserRenderer(BlockEntityRendererProvider.Context ctx) {
        this.fallback = new ShowLaserRenderer(ctx);
    }

    @Override
    public void render(ShowLaserBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!be.isActive()) return;

        // Renderiza o feixe base (linhas coloridas com zoom)
        fallback.render(be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        // Adiciona partículas Quasar no ponto de impacto
        try {
            spawnImpactParticles(be);
        } catch (Exception ignored) {
            // Veil pode não estar disponível ou ocorrer erro — ignora silenciosamente
        }
    }

    private void spawnImpactParticles(ShowLaserBlockEntity be) {
        ParticleSystemManager manager = VeilRenderSystem.renderer().getParticleManager();
        BlockPos pos = be.getBlockPos();

        // Calcula ponto de impacto aproximado baseado na facing e range
        var facing = be.getBlockState().getValue(dev.aerolaser.block.ShowLaserBlock.FACING);
        double range = be.getRange();
        Vec3 impactPos = new Vec3(
                pos.getX() + 0.5 + facing.getStepX() * range,
                pos.getY() + 0.5 + facing.getStepY() * range,
                pos.getZ() + 0.5 + facing.getStepZ() * range
        );

        // Emite faíscas no ponto de impacto (só 1 vez por segundo para performance)
        if (be.getLevel() != null && be.getLevel().getGameTime() % 20 == 0) {
            ParticleEmitter sparks = manager.createEmitter(SPARK_EMITTER);
            if (sparks != null) {
                sparks.setPosition(impactPos);
                manager.addParticleSystem(sparks);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ShowLaserBlockEntity be) { return true; }

    @Override
    public int getViewDistance() { return 128; }
}
