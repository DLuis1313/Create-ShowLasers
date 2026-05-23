package dev.aerolaser.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import foundry.veil.api.quasar.particle.ParticleSystemManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class VeilLaserRenderer extends ShowLaserRenderer {

    private static final ResourceLocation SPARK_EMITTER =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "laser_spark");
    private static final ResourceLocation GLOW_EMITTER =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "laser_glow");

    public VeilLaserRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ShowLaserBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        if (!be.isActive()) return;

        // Renderiza o feixe base
        super.render(be, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        // Partículas Quasar no ponto de impacto
        try {
            spawnImpactParticles(be);
        } catch (Exception ignored) {}
    }

    private void spawnImpactParticles(ShowLaserBlockEntity be) {
        if (be.getLevel() == null) return;
        // Só emite a cada 10 ticks para performance
        if (be.getLevel().getGameTime() % 10 != 0) return;

        ParticleSystemManager manager = VeilRenderSystem.renderer().getParticleManager();
        BlockPos pos = be.getBlockPos();

        var facing = be.getBlockState()
                .getValue(dev.aerolaser.block.ShowLaserBlock.FACING);
        double range = be.getRange();
        Vec3 impactPos = new Vec3(
                pos.getX() + 0.5 + facing.getStepX() * range,
                pos.getY() + 0.5 + facing.getStepY() * range,
                pos.getZ() + 0.5 + facing.getStepZ() * range
        );

        ParticleEmitter sparks = manager.createEmitter(SPARK_EMITTER);
        if (sparks != null) {
            sparks.setPosition(impactPos);
            manager.addParticleSystem(sparks);
        }

        ParticleEmitter glow = manager.createEmitter(GLOW_EMITTER);
        if (glow != null) {
            glow.setPosition(impactPos);
            manager.addParticleSystem(glow);
        }
    }
}
