package dev.aerolaser.registry;

import dev.aerolaser.AeroLaserMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = AeroLaserMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class AeroLaserCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AeroLaserMod.MOD_ID);

    // Aba própria do AeroLaser
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AEROLASER_TAB =
            CREATIVE_TABS.register("aerolaser_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.aerolaser.main"))
                            .icon(() -> new ItemStack(AeroLaserItems.SHOW_LASER.get()))
                            .displayItems((params, output) -> {
                                output.accept(AeroLaserItems.SHOW_LASER.get());
                            })
                            .build()
            );

    // Também adiciona na aba "Simulated" do Create Aeronautics, se presente
    @SubscribeEvent
    public static void addToSimulatedTab(BuildCreativeModeTabContentsEvent event) {
        // Tenta adicionar na aba do Create Aeronautics (simulated)
        ResourceKey<CreativeModeTab> simulatedTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath("simulated", "simulated")
        );
        if (event.getTabKey() == simulatedTab) {
            event.accept(AeroLaserItems.SHOW_LASER.get());
        }
    }
}
