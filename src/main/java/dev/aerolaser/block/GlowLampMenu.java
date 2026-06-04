package dev.aerolaser.block;

import dev.aerolaser.blockentity.GlowLampBlockEntity;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

// Slots: [0]R [1]G [2]B [3]size
public class GlowLampMenu extends AbstractContainerMenu {

    private final GlowLampBlockEntity be;
    private final ContainerData data;

    public GlowLampMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (GlowLampBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public GlowLampMenu(int id, Inventory inv, GlowLampBlockEntity be) {
        super(AeroLaserMenuTypes.GLOW_LAMP_MENU.get(), id);
        this.be = be;
        this.data = new SimpleContainerData(4) {
            @Override public int get(int i) {
                return switch (i) {
                    case 0 -> be.getColorR();
                    case 1 -> be.getColorG();
                    case 2 -> be.getColorB();
                    case 3 -> (int)(be.getSize() * 10);
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {
                switch (i) {
                    case 0 -> be.setColorR(v);
                    case 1 -> be.setColorG(v);
                    case 2 -> be.setColorB(v);
                    case 3 -> be.setSize(v);
                }
            }
        };
        addDataSlots(this.data);
    }

    public int   getColorR() { return data.get(0); }
    public int   getColorG() { return data.get(1); }
    public int   getColorB() { return data.get(2); }
    public int   getSizeInt(){ return data.get(3); }
    public float getSize()   { return data.get(3) / 10f; }

    public void setColorR(int v) { data.set(0, v); }
    public void setColorG(int v) { data.set(1, v); }
    public void setColorB(int v) { data.set(2, v); }
    public void setSize(int v)   { data.set(3, v); }

    public GlowLampBlockEntity getBlockEntity() { return be; }

    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) {
        return be.getLevel() != null && AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), player,
                dev.aerolaser.registry.AeroLaserBlocks.GLOW_LAMP.get());
    }
}
