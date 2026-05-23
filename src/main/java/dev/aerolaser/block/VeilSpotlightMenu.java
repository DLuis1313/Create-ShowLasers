package dev.aerolaser.block;

import dev.aerolaser.blockentity.VeilSpotlightBlockEntity;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * Slots ContainerData:
 * [0]R [1]G [2]B [3]brightness*10 [4]distance*10 [5]angle*10 [6]sizeX*10 [7]sizeY*10
 */
public class VeilSpotlightMenu extends AbstractContainerMenu {

    private final VeilSpotlightBlockEntity be;
    private final ContainerData data;

    public VeilSpotlightMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (VeilSpotlightBlockEntity) inv.player.level()
                .getBlockEntity(buf.readBlockPos()));
    }

    public VeilSpotlightMenu(int id, Inventory inv, VeilSpotlightBlockEntity be) {
        super(AeroLaserMenuTypes.VEIL_SPOTLIGHT_MENU.get(), id);
        this.be = be;
        this.data = new SimpleContainerData(8) {
            @Override public int get(int i) {
                return switch (i) {
                    case 0 -> be.getColorR();
                    case 1 -> be.getColorG();
                    case 2 -> be.getColorB();
                    case 3 -> (int)(be.getBrightness() * 10);
                    case 4 -> (int)(be.getDistance()   * 10);
                    case 5 -> (int)(be.getAngle()      * 10);
                    case 6 -> (int)(be.getSizeX()      * 10);
                    case 7 -> (int)(be.getSizeY()      * 10);
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {
                switch (i) {
                    case 0 -> be.setColorR(v);
                    case 1 -> be.setColorG(v);
                    case 2 -> be.setColorB(v);
                    case 3 -> be.setBrightness(v / 10f);
                    case 4 -> be.setDistance(v / 10f);
                    case 5 -> be.setAngle(v / 10f);
                    case 6 -> be.setSizeX(v / 10f);
                    case 7 -> be.setSizeY(v / 10f);
                }
            }
        };
        addDataSlots(this.data);
    }

    public int   getColorR()     { return data.get(0); }
    public int   getColorG()     { return data.get(1); }
    public int   getColorB()     { return data.get(2); }
    public float getBrightness() { return data.get(3) / 10f; }
    public float getDistance()   { return data.get(4) / 10f; }
    public float getAngle()      { return data.get(5) / 10f; }
    public float getSizeX()      { return data.get(6) / 10f; }
    public float getSizeY()      { return data.get(7) / 10f; }

    public void setColorR(int v)       { data.set(0, v); }
    public void setColorG(int v)       { data.set(1, v); }
    public void setColorB(int v)       { data.set(2, v); }
    public void setBrightness(float v) { data.set(3, (int)(v * 10)); }
    public void setDistance(float v)   { data.set(4, (int)(v * 10)); }
    public void setAngle(float v)      { data.set(5, (int)(v * 10)); }
    public void setSizeX(float v)      { data.set(6, (int)(v * 10)); }
    public void setSizeY(float v)      { data.set(7, (int)(v * 10)); }

    public VeilSpotlightBlockEntity getBlockEntity() { return be; }

    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) {
        return be.getLevel() != null && AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), player,
                dev.aerolaser.registry.AeroLaserBlocks.VEIL_SPOTLIGHT.get());
    }
}
