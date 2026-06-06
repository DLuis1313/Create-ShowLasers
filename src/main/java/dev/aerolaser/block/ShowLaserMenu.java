package dev.aerolaser.block;

import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

// Slots: [0]zoom [1]R [2]G [3]B [4]mode [5]speed [6]range
public class ShowLaserMenu extends AbstractContainerMenu {

    private final ShowLaserBlockEntity be;
    private final ContainerData data;

    public ShowLaserMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (ShowLaserBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ShowLaserMenu(int id, Inventory inv, ShowLaserBlockEntity be) {
        super(AeroLaserMenuTypes.SHOW_LASER_MENU.get(), id);
        this.be = be;
        this.data = new SimpleContainerData(7) {
            @Override public int get(int i) {
                return switch (i) {
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
            @Override public void set(int i, int v) {
                switch (i) {
                    case 0 -> be.setZoom(v);
                    case 1 -> be.setColorR(v);
                    case 2 -> be.setColorG(v);
                    case 3 -> be.setColorB(v);
                    case 4 -> be.setMode(v);
                    case 5 -> be.setSweepSpeed(v);
                    case 6 -> be.setRange(v);
                }
            }
        };
        addDataSlots(this.data);
    }

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

    public ShowLaserBlockEntity getBlockEntity() { return be; }

    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }

    @Override public boolean stillValid(Player player) {
        return be.getLevel() != null && AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()),
                player, dev.aerolaser.registry.AeroLaserBlocks.SHOW_LASER.get());
    }
}
