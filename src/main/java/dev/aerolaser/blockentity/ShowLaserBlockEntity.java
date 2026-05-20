package dev.aerolaser.blockentity;

import dev.aerolaser.block.ShowLaserMenu;
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
 * Stores and ticks all Show Laser configuration:
 *  - zoom       : 1–20  (1 = thin beam, 20 = wide cone)
 *  - red/green/blue : 0–255 RGB colour of the laser
 *  - mode       : 0=STATIC  1=SWEEP  2=SPIN  3=BOUNCE  4=PULSE
 *  - sweepSpeed : 1–20 (ticks per degree step)
 *  - range      : 1–64 (blocks)
 *  - powered    : follows redstone; only shoots when true (or always if redstone-independent)
 */
public class ShowLaserBlockEntity extends BlockEntity implements MenuProvider {

    // ── Config fields ─────────────────────────────────────────────────────────
    private int zoom        = 1;
    private int colorR      = 255;
    private int colorG      = 0;
    private int colorB      = 0;
    private int mode        = 0;   // LaserMode ordinal
    private int sweepSpeed  = 5;
    private int range       = 32;

    // ── Runtime state ─────────────────────────────────────────────────────────
    private boolean active  = false;
    private float   sweepAngle = 0f;
    private int     sweepDir   = 1;
    private int     pulseTick  = 0;

    // ── Mode constants ────────────────────────────────────────────────────────
    public static final int MODE_STATIC  = 0;
    public static final int MODE_SWEEP   = 1;
    public static final int MODE_SPIN    = 2;
    public static final int MODE_BOUNCE  = 3;
    public static final int MODE_PULSE   = 4;
    public static final int MODE_COUNT   = 5;

    public ShowLaserBlockEntity(BlockPos pos, BlockState state) {
        super(AeroLaserBlockEntities.SHOW_LASER.get(), pos, state);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos,
                            BlockState state, ShowLaserBlockEntity be) {
        if (level.isClientSide) return;
        if (!be.active) return;

        switch (be.mode) {
            case MODE_SWEEP -> be.tickSweep();
            case MODE_SPIN  -> be.tickSpin();
            case MODE_BOUNCE -> be.tickBounce();
            case MODE_PULSE -> be.tickPulse();
            default -> { /* static – nothing to do */ }
        }
    }

    private void tickSweep() {
        sweepAngle += sweepDir * (1f / Math.max(1, sweepSpeed));
        if (sweepAngle > 45f)  { sweepAngle = 45f;  sweepDir = -1; }
        if (sweepAngle < -45f) { sweepAngle = -45f; sweepDir =  1; }
        setChanged();
        syncToClients();
    }

    private void tickSpin() {
        sweepAngle = (sweepAngle + (1f / Math.max(1, sweepSpeed))) % 360f;
        setChanged();
        syncToClients();
    }

    private void tickBounce() {
        sweepAngle += sweepDir * (2f / Math.max(1, sweepSpeed));
        if (sweepAngle > 90f)  { sweepAngle = 90f;  sweepDir = -1; }
        if (sweepAngle < 0f)   { sweepAngle = 0f;   sweepDir =  1; }
        setChanged();
        syncToClients();
    }

    private void tickPulse() {
        pulseTick++;
        if (pulseTick > sweepSpeed * 4) pulseTick = 0;
        setChanged();
        syncToClients();
    }

    /** Called by the block when redstone signal changes. */
    public void onPowerChange(boolean powered) {
        this.active = powered;
        setChanged();
        syncToClients();
    }

    /** Effective zoom considering pulse mode (shrinks/grows). */
    public int getEffectiveZoom() {
        if (mode == MODE_PULSE && active) {
            double phase = (Math.sin(pulseTick * Math.PI * 2.0 / (sweepSpeed * 4)) + 1) / 2;
            return Math.max(1, (int)(zoom * phase));
        }
        return zoom;
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Zoom",       zoom);
        tag.putInt("ColorR",     colorR);
        tag.putInt("ColorG",     colorG);
        tag.putInt("ColorB",     colorB);
        tag.putInt("Mode",       mode);
        tag.putInt("SweepSpeed", sweepSpeed);
        tag.putInt("Range",      range);
        tag.putBoolean("Active", active);
        tag.putFloat("SweepAngle", sweepAngle);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        zoom       = clamp(tag.getInt("Zoom"),       1, 20);
        colorR     = clamp(tag.getInt("ColorR"),     0, 255);
        colorG     = clamp(tag.getInt("ColorG"),     0, 255);
        colorB     = clamp(tag.getInt("ColorB"),     0, 255);
        mode       = clamp(tag.getInt("Mode"),       0, MODE_COUNT - 1);
        sweepSpeed = clamp(tag.getInt("SweepSpeed"), 1, 20);
        range      = clamp(tag.getInt("Range"),      1, 64);
        active     = tag.getBoolean("Active");
        sweepAngle = tag.getFloat("SweepAngle");
    }

    // ── Sync packet ───────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ── MenuProvider ─────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.aerolaser.show_laser");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player player) {
        return new ShowLaserMenu(containerId, inv, this);
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getZoom()        { return zoom; }
    public int getColorR()      { return colorR; }
    public int getColorG()      { return colorG; }
    public int getColorB()      { return colorB; }
    public int getMode()        { return mode; }
    public int getSweepSpeed()  { return sweepSpeed; }
    public int getRange()       { return range; }
    public boolean isActive()   { return active; }
    public float getSweepAngle(){ return sweepAngle; }

    public void setZoom(int v)       { zoom       = clamp(v,1,20); setChanged(); }
    public void setColorR(int v)     { colorR     = clamp(v,0,255); setChanged(); }
    public void setColorG(int v)     { colorG     = clamp(v,0,255); setChanged(); }
    public void setColorB(int v)     { colorB     = clamp(v,0,255); setChanged(); }
    public void setMode(int v)       { mode       = clamp(v,0,MODE_COUNT-1); setChanged(); }
    public void setSweepSpeed(int v) { sweepSpeed = clamp(v,1,20); setChanged(); }
    public void setRange(int v)      { range      = clamp(v,1,64); setChanged(); }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
