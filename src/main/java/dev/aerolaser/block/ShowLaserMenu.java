package dev.aerolaser.block;

import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Container that syncs ShowLaserBlockEntity data to/from the client GUI.
 * We use a ContainerData (int array) to sync the 7 config values:
 *   [0] zoom  [1] R  [2] G  [3] B  [4] mode  [5] sweepSpeed  [6] range
 */
public class ShowLaserMenu extends AbstractContainerMenu {

    public static final int SLOTS = 7;

    private final ShowLaserBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // Client-side constructor (called by IMenuTypeExtension factory)
    public ShowLaserMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, (ShowLaserBlockEntity) inv.player.level()
                .getBlockEntity(buf.readBlockPos()));
    }

    // Server-side constructor
    public ShowLaserMenu(int containerId, Inventory inv, ShowLaserBlockEntity be) {
        super(AeroLaserMenuTypes.SHOW_LASER_MENU.get(), containerId);
        this.blockEntity = be;
        this.level = inv.player.level();

        this.data = new SimpleContainerData(SLOTS) {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getZoom();
                    case 1 -> be.getColorR();
                    case 2 -> be.getColorG();
                    case 3 -> be.getColorB();
                    case 4 -> be.getMode();
                    case 5 -> be.getSweepSpeed();
                    case 6 -> be.getRange();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> be.setZoom(value);
                    case 1 -> be.setColorR(value);
                    case 2 -> be.setColorG(value);
                    case 3 -> be.setColorB(value);
                    case 4 -> be.setMode(value);
                    case 5 -> be.setSweepSpeed(value);
                    case 6 -> be.setRange(value);
                }
            }
        };
        addDataSlots(this.data);
    }

    // ── Data accessors (client reads these) ──────────────────────────────────

    public int getZoom()       { return data.get(0); }
    public int getColorR()     { return data.get(1); }
    public int getColorG()     { return data.get(2); }
    public int getColorB()     { return data.get(3); }
    public int getMode()       { return data.get(4); }
    public int getSweepSpeed() { return data.get(5); }
    public int getRange()      { return data.get(6); }

    public void setZoom(int v)       { data.set(0, v); }
    public void setColorR(int v)     { data.set(1, v); }
    public void setColorG(int v)     { data.set(2, v); }
    public void setColorB(int v)     { data.set(3, v); }
    public void setMode(int v)       { data.set(4, v); }
    public void setSweepSpeed(int v) { data.set(5, v); }
    public void setRange(int v)      { data.set(6, v); }

    public ShowLaserBlockEntity getBlockEntity() { return blockEntity; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.getLevel() != null &&
               AbstractContainerMenu.stillValid(
                   net.minecraft.world.inventory.ContainerLevelAccess.create(
                       blockEntity.getLevel(), blockEntity.getBlockPos()),
                   player,
                   dev.aerolaser.registry.AeroLaserBlocks.SHOW_LASER.get());
    }
}
