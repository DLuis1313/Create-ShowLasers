package dev.aerolaser.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.aerolaser.blockentity.VeilSpotlightBlockEntity;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

/**
 * Renderer do Veil Spotlight.
 *
 * Usa AreaLightData do Veil 4.x para criar spotlights/flood lights volumétricas.
 * Cada bloco gerencia uma instância de AreaLightData — adicionada quando ativa,
 * removida quando desativada ou o bloco é destruído.
 *
 * AreaLightData:
 *  - position   : posição no mundo
 *  - orientation: rotação para apontar na direção do FACING
 *  - size        : tamanho da superfície emissora (largura × altura)
 *  - angle       : ângulo do cone em radianos
 *  - distance    : alcance máximo em blocos
 *  - color       : RGB normalizado (0.0–1.0)
 *  - brightness  : multiplicador de intensidade
 */
public class VeilSpotlightRenderer implements BlockEntityRenderer<VeilSpotlightBlockEntity> {

    // Mapa de posição → luz ativa para gerenciar o ciclo de vida
    private static final Map<BlockPos, AreaLightData> ACTIVE_LIGHTS = new HashMap<>();

    public VeilSpotlightRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(VeilSpotlightBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        BlockPos pos = be.getBlockPos();

        if (!be.isActive()) {
            // Remove a luz se existir
            removeLight(pos);
            return;
        }

        try {
            var lightRenderer = VeilRenderSystem.renderer().getLightRenderer();
            AreaLightData light = ACTIVE_LIGHTS.get(pos);

            if (light == null) {
                // Cria nova luz e registra
                light = new AreaLightData();
                lightRenderer.addLight(light);
                ACTIVE_LIGHTS.put(pos, light);
            }

            // Atualiza posição (centro do bloco)
            light.setPosition(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5
            );

            // Orientação baseada no FACING do bloco
            Direction facing = be.getBlockState()
                    .getValue(dev.aerolaser.block.VeilSpotlightBlock.FACING);
            light.getOrientation().set(directionToQuaternion(facing));

            // Configurações do bloco
            light.setSize(be.getSizeX(), be.getSizeY());
            light.setAngle((float) Math.toRadians(be.getAngle()));
            light.setDistance(be.getDistance());
            light.setColor(
                    be.getColorR() / 255f,
                    be.getColorG() / 255f,
                    be.getColorB() / 255f
            );
            light.setBrightness(be.getBrightness());
            light.setOcclusionEnabled(true);

        } catch (Exception e) {
            // Veil pode não estar disponível ou API diferente — ignora
            removeLight(pos);
        }
    }

    private static void removeLight(BlockPos pos) {
        AreaLightData light = ACTIVE_LIGHTS.remove(pos);
        if (light != null) {
            try {
                VeilRenderSystem.renderer().getLightRenderer().removeLight(light);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Converte Direction para Quaternionf para a AreaLightData.
     * O Veil usa +Y como direção padrão da luz, então rotacionamos a partir daí.
     */
    private static Quaternionf directionToQuaternion(Direction dir) {
        return switch (dir) {
            case DOWN  -> new Quaternionf(); // padrão — aponta para baixo
            case UP    -> new Quaternionf().rotateX((float) Math.PI);
            case NORTH -> new Quaternionf().rotateX((float) (Math.PI / 2));
            case SOUTH -> new Quaternionf().rotateX((float) (-Math.PI / 2));
            case EAST  -> new Quaternionf().rotateZ((float) (Math.PI / 2));
            case WEST  -> new Quaternionf().rotateZ((float) (-Math.PI / 2));
        };
    }

    @Override
    public boolean shouldRenderOffScreen(VeilSpotlightBlockEntity be) { return true; }

    @Override
    public int getViewDistance() { return 256; }
}
