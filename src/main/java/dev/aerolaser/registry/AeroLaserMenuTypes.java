package dev.aerolaser.registry;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.ShowLaserMenu;
import dev.aerolaser.block.VeilSpotlightMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AeroLaserMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, AeroLaserMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ShowLaserMenu>> SHOW_LASER_MENU =
            MENUS.register("show_laser_menu",
                    () -> IMenuTypeExtension.create(ShowLaserMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<VeilSpotlightMenu>> VEIL_SPOTLIGHT_MENU =
            MENUS.register("veil_spotlight_menu",
                    () -> IMenuTypeExtension.create(VeilSpotlightMenu::new));
}
