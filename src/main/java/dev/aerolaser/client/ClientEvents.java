package dev.aerolaser.client;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.blockentity.GlowLampBlockEntity;
import dev.aerolaser.client.gui.GlowLampScreen;
import dev.aerolaser.client.gui.ShowLaserScreen;
import dev.aerolaser.client.renderer.GlowLampRenderer;
import dev.aerolaser.client.renderer.ShowLaserRenderer;
import dev.aerolaser.registry.AeroLaserBlockEntities;
import dev.aerolaser.registry.AeroLaserBlocks;
import dev.aerolaser.registry.AeroLaserItems;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AeroLaserMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AeroLaserBlockEntities.SHOW_LASER.get(), ShowLaserRenderer::new);
        event.registerBlockEntityRenderer(AeroLaserBlockEntities.GLOW_LAMP.get(), GlowLampRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(AeroLaserMenuTypes.SHOW_LASER_MENU.get(), ShowLaserScreen::new);
        event.register(AeroLaserMenuTypes.GLOW_LAMP_MENU.get(), GlowLampScreen::new);
    }

    /**
     * BlockColor: tinta a face marcada com tintindex:0 no modelo
     * com a cor RGB configurada no BlockEntity.
     * Isso faz a parte branca da textura mudar de cor conforme o menu.
     */
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 0 && level != null && pos != null
                            && level.getBlockEntity(pos) instanceof GlowLampBlockEntity be) {
                        return (be.getColorR() << 16) | (be.getColorG() << 8) | be.getColorB();
                    }
                    return 0xFFFFFF;
                },
                AeroLaserBlocks.GLOW_LAMP.get()
        );
    }

    /**
     * ItemColor: tinta o item no inventário com uma cor padrão dourada.
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? 0xFFDD44 : 0xFFFFFF,
                AeroLaserItems.GLOW_LAMP.get()
        );
    }
}
