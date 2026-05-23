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

/**
 * Slots: [0]zoom [1]R [2]G [3]B [4]mode [5]speed [6]range [7]useVeil(0/1)
 */
public class ShowLaserMenu extends AbstractContainerMenu {

    public static final int SLOTS = 8;
    private final ShowLaserBlockEntity blockEntity;
    private final ContainerData data;

    public ShowLaserMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (ShowLaserBlockEntity) inv.player.level()
                .getBlockEntity(buf.readBlockPos()));
    }

    public ShowLaserMenu(int id, Inventory inv, ShowLaserBlockEntity be) {
        super(AeroLaserMenuTypes.SHOW_LASER_MENU.get(), id);
        this.blockEntity = be;

        this.data = new SimpleContainerData(SLOTS) {
            @Override public int get(int i) {
                return switch (i) {
                    case 0 -> be.getZoom();
                    case 1 -> be.getColorR();
                    case 2 -> be.getColorG();
                    case 3 -> be.getColorB();
                    case 4 -> be.getMode();
                    case 5 -> be.getSweepSpeed();
                    case 6 -> be.getRange();
                    case 7 -> be.isUseVeil() ? 1 : 0;
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {
                switch (i) {
                    case 0 -> be.setZoom(v);
                    case 1 -> be.setColorR(v);
                    case 2 -> be.setColorG(v);
                    case 3 -> be.setColorB(v);
                    case 4 -> be.setMode(v);
                    case 5 -> be.setSweepSpeed(v);
                    case 6 -> be.setRange(v);
                    case 7 -> be.setUseVeil(v == 1);
                }
            }
        };
        addDataSlots(this.data);
    }

    public int  getZoom()       { return data.get(0); }
    public int  getColorR()     { return data.get(1); }
    public int  getColorG()     { return data.get(2); }
    public int  getColorB()     { return data.get(3); }
    public int  getMode()       { return data.get(4); }
    public int  getSweepSpeed() { return data.get(5); }
    public int  getRange()      { return data.get(6); }
    public boolean isUseVeil()  { return data.get(7) == 1; }

    public void setZoom(int v)       { data.set(0, v); }
    public void setColorR(int v)     { data.set(1, v); }
    public void setColorG(int v)     { data.set(2, v); }
    public void setColorB(int v)     { data.set(3, v); }
    public void setMode(int v)       { data.set(4, v); }
    public void setSweepSpeed(int v) { data.set(5, v); }
    public void setRange(int v)      { data.set(6, v); }
    public void toggleVeil()         { data.set(7, isUseVeil() ? 0 : 1); }

    public ShowLaserBlockEntity getBlockEntity() { return blockEntity; }

    @Override
    public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }

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
