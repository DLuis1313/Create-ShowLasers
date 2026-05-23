package dev.aerolaser.client;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.client.gui.ShowLaserScreen;
import dev.aerolaser.client.gui.VeilSpotlightScreen;
import dev.aerolaser.client.renderer.ShowLaserRenderer;
import dev.aerolaser.client.renderer.VeilLaserRenderer;
import dev.aerolaser.client.renderer.VeilSpotlightRenderer;
import dev.aerolaser.registry.AeroLaserBlockEntities;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AeroLaserMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        boolean veilPresent = ModList.get().isLoaded("veil");

        // Show Laser — usa Veil se disponível
        if (veilPresent) {
            event.registerBlockEntityRenderer(AeroLaserBlockEntities.SHOW_LASER.get(), VeilLaserRenderer::new);
        } else {
            event.registerBlockEntityRenderer(AeroLaserBlockEntities.SHOW_LASER.get(), ShowLaserRenderer::new);
        }

        // Veil Spotlight — só registra renderer se Veil estiver instalado
        if (veilPresent) {
            event.registerBlockEntityRenderer(AeroLaserBlockEntities.VEIL_SPOTLIGHT.get(), VeilSpotlightRenderer::new);
        }
        // Se Veil não estiver instalado, o bloco não renderiza luz mas ainda funciona como bloco decorativo
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(AeroLaserMenuTypes.SHOW_LASER_MENU.get(),     ShowLaserScreen::new);
        event.register(AeroLaserMenuTypes.VEIL_SPOTLIGHT_MENU.get(), VeilSpotlightScreen::new);
    }
}
