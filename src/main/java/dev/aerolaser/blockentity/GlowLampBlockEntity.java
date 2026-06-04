package dev.aerolaser.blockentity;

import dev.aerolaser.block.GlowLampMenu;
import dev.aerolaser.registry.AeroLaserBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GlowLampBlockEntity extends BlockEntity implements MenuProvider {

    private int   colorR  = 255;
    private int   colorG  = 200;
    private int   colorB  = 100;
    // tamanho do glow: 5–30 (representa 0.5–3.0 blocos)
    private int   size    = 15; // = 1.5 blocos
    private boolean active = false;

    public GlowLampBlockEntity(BlockPos pos, BlockState state) {
        super(AeroLaserBlockEntities.GLOW_LAMP.get(), pos, state);
    }

    public void onPowerChange(boolean powered) {
        this.active = powered;
        setChanged();
        syncToClients();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("ColorR", colorR);
        tag.putInt("ColorG", colorG);
        tag.putInt("ColorB", colorB);
        tag.putInt("Size",   size);
        tag.putBoolean("Active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        colorR = clamp(tag.getInt("ColorR"), 0, 255);
        colorG = clamp(tag.getInt("ColorG"), 0, 255);
        colorB = clamp(tag.getInt("ColorB"), 0, 255);
        size   = clamp(tag.getInt("Size"),   5,  30);
        active = tag.getBoolean("Active");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider reg) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, reg);
        return tag;
    }

    @Nullable @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.aerolaser.glow_lamp");
    }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new GlowLampMenu(id, inv, this);
    }

    public int     getColorR() { return colorR; }
    public int     getColorG() { return colorG; }
    public int     getColorB() { return colorB; }
    public float   getSize()   { return size / 10f; }
    public boolean isActive()  { return active; }

    public void setColorR(int v) { colorR = clamp(v, 0, 255); setChanged(); }
    public void setColorG(int v) { colorG = clamp(v, 0, 255); setChanged(); }
    public void setColorB(int v) { colorB = clamp(v, 0, 255); setChanged(); }
    public void setSize(int v)   { size   = clamp(v, 5,  30); setChanged(); }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}
