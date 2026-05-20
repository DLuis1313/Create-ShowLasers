package dev.aerolaser.network;

import dev.aerolaser.AeroLaserMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AeroLaserMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class AeroLaserNetwork {

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(AeroLaserMod.MOD_ID);

        reg.playToServer(
                LaserConfigPacket.TYPE,
                LaserConfigPacket.CODEC,
                LaserConfigPacket::handle
        );
    }
}
