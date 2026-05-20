package dev.aerolaser.client;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.client.gui.ShowLaserScreen;
import dev.aerolaser.client.renderer.ShowLaserRenderer;
import dev.aerolaser.registry.AeroLaserBlockEntities;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import dev.aerolaser.network.LaserConfigPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = AeroLaserMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                AeroLaserBlockEntities.SHOW_LASER.get(),
                ShowLaserRenderer::new
        );
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(AeroLaserMenuTypes.SHOW_LASER_MENU.get(), ShowLaserScreen::new);
    }
}
