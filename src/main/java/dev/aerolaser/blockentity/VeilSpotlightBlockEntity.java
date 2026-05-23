package dev.aerolaser.blockentity;

import dev.aerolaser.block.VeilSpotlightMenu;
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

/**
 * Bloco Spotlight do Veil.
 * Usa AreaLightData para criar uma flood/spotlight volumétrica colorida.
 *
 * Configurações:
 *  - colorR/G/B : cor da luz (0–255)
 *  - brightness : intensidade (1–20)
 *  - distance   : alcance em blocos (1–64)
 *  - angle      : ângulo do cone em graus (5–90)
 *  - sizeX/Y    : tamanho da superfície emissora (0.1–4.0)
 *  - active     : ligado/desligado via redstone
 */
public class VeilSpotlightBlockEntity extends BlockEntity implements MenuProvider {

    private int   colorR     = 255;
    private int   colorG     = 255;
    private int   colorB     = 255;
    private float brightness = 5f;
    private float distance   = 16f;
    private float angle      = 45f;  // graus
    private float sizeX      = 0.5f;
    private float sizeY      = 0.5f;
    private boolean active   = false;

    public VeilSpotlightBlockEntity(BlockPos pos, BlockState state) {
        super(AeroLaserBlockEntities.VEIL_SPOTLIGHT.get(), pos, state);
    }

    public void onPowerChange(boolean powered) {
        this.active = powered;
        setChanged();
        syncToClients();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("ColorR",     colorR);
        tag.putInt("ColorG",     colorG);
        tag.putInt("ColorB",     colorB);
        tag.putFloat("Brightness", brightness);
        tag.putFloat("Distance",   distance);
        tag.putFloat("Angle",      angle);
        tag.putFloat("SizeX",      sizeX);
        tag.putFloat("SizeY",      sizeY);
        tag.putBoolean("Active",   active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        colorR     = clampI(tag.getInt("ColorR"),     0, 255);
        colorG     = clampI(tag.getInt("ColorG"),     0, 255);
        colorB     = clampI(tag.getInt("ColorB"),     0, 255);
        brightness = clampF(tag.getFloat("Brightness"), 0.1f, 20f);
        distance   = clampF(tag.getFloat("Distance"),   1f,   64f);
        angle      = clampF(tag.getFloat("Angle"),      5f,   90f);
        sizeX      = clampF(tag.getFloat("SizeX"),      0.1f, 4f);
        sizeY      = clampF(tag.getFloat("SizeY"),      0.1f, 4f);
        active     = tag.getBoolean("Active");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider reg) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, reg);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.aerolaser.veil_spotlight");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new VeilSpotlightMenu(id, inv, this);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int   getColorR()     { return colorR; }
    public int   getColorG()     { return colorG; }
    public int   getColorB()     { return colorB; }
    public float getBrightness() { return brightness; }
    public float getDistance()   { return distance; }
    public float getAngle()      { return angle; }
    public float getSizeX()      { return sizeX; }
    public float getSizeY()      { return sizeY; }
    public boolean isActive()    { return active; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setColorR(int v)      { colorR     = clampI(v, 0, 255);    setChanged(); }
    public void setColorG(int v)      { colorG     = clampI(v, 0, 255);    setChanged(); }
    public void setColorB(int v)      { colorB     = clampI(v, 0, 255);    setChanged(); }
    public void setBrightness(float v){ brightness = clampF(v, 0.1f, 20f); setChanged(); }
    public void setDistance(float v)  { distance   = clampF(v, 1f, 64f);   setChanged(); }
    public void setAngle(float v)     { angle      = clampF(v, 5f, 90f);   setChanged(); }
    public void setSizeX(float v)     { sizeX      = clampF(v, 0.1f, 4f);  setChanged(); }
    public void setSizeY(float v)     { sizeY      = clampF(v, 0.1f, 4f);  setChanged(); }

    private static int   clampI(int v, int min, int max)       { return Math.max(min, Math.min(max, v)); }
    private static float clampF(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
