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

public class ShowLaserBlockEntity extends BlockEntity implements MenuProvider {

    private int zoom=1, colorR=255, colorG=0, colorB=0, mode=0, sweepSpeed=5, range=32;
    private boolean active=false;
    private float sweepAngle=0f;
    private int sweepDir=1, pulseTick=0;

    public static final int MODE_STATIC=0, MODE_SWEEP=1, MODE_SPIN=2, MODE_BOUNCE=3, MODE_PULSE=4, MODE_COUNT=5;

    public ShowLaserBlockEntity(BlockPos pos, BlockState state) {
        super(AeroLaserBlockEntities.SHOW_LASER.get(), pos, state);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, ShowLaserBlockEntity be) {
        if (level.isClientSide || !be.active) return;
        switch (be.mode) {
            case MODE_SWEEP  -> be.tickSweep();
            case MODE_SPIN   -> be.tickSpin();
            case MODE_BOUNCE -> be.tickBounce();
            case MODE_PULSE  -> be.tickPulse();
        }
    }

    private float speedToDegrees() { return 0.05f + (sweepSpeed-1) * (2f-0.05f) / 19f; }
    private void tickSweep()  { sweepAngle += sweepDir*speedToDegrees(); if(sweepAngle>45f){sweepAngle=45f;sweepDir=-1;} if(sweepAngle<-45f){sweepAngle=-45f;sweepDir=1;} setChanged();syncToClients(); }
    private void tickSpin()   { sweepAngle=(sweepAngle+speedToDegrees())%360f; setChanged();syncToClients(); }
    private void tickBounce() { sweepAngle+=sweepDir*speedToDegrees(); if(sweepAngle>90f){sweepAngle=90f;sweepDir=-1;} if(sweepAngle<0f){sweepAngle=0f;sweepDir=1;} setChanged();syncToClients(); }
    private void tickPulse()  { int c=Math.max(1,80-sweepSpeed*3); if(++pulseTick>=c)pulseTick=0; setChanged();syncToClients(); }

    public void onPowerChange(boolean powered) { this.active=powered; setChanged();syncToClients(); }

    public int getEffectiveZoom() {
        if(mode==MODE_PULSE&&active){ int c=Math.max(1,80-sweepSpeed*3); double p=(Math.sin(pulseTick*Math.PI*2.0/c)+1)/2; return Math.max(1,(int)(zoom*p)); }
        return zoom;
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag,reg);
        tag.putInt("Zoom",zoom); tag.putInt("ColorR",colorR); tag.putInt("ColorG",colorG); tag.putInt("ColorB",colorB);
        tag.putInt("Mode",mode); tag.putInt("SweepSpeed",sweepSpeed); tag.putInt("Range",range);
        tag.putBoolean("Active",active); tag.putFloat("SweepAngle",sweepAngle);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag,reg);
        zoom=clamp(tag.getInt("Zoom"),1,20); colorR=clamp(tag.getInt("ColorR"),0,255); colorG=clamp(tag.getInt("ColorG"),0,255); colorB=clamp(tag.getInt("ColorB"),0,255);
        mode=clamp(tag.getInt("Mode"),0,MODE_COUNT-1); sweepSpeed=clamp(tag.getInt("SweepSpeed"),1,20); range=clamp(tag.getInt("Range"),1,64);
        active=tag.getBoolean("Active"); sweepAngle=tag.getFloat("SweepAngle");
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider reg) { CompoundTag t=new CompoundTag(); saveAdditional(t,reg); return t; }
    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    private void syncToClients() { if(level!=null&&!level.isClientSide) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }

    @Override public Component getDisplayName() { return Component.translatable("container.aerolaser.show_laser"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new ShowLaserMenu(id,inv,this); }

    public int getZoom(){return zoom;} public int getColorR(){return colorR;} public int getColorG(){return colorG;} public int getColorB(){return colorB;}
    public int getMode(){return mode;} public int getSweepSpeed(){return sweepSpeed;} public int getRange(){return range;}
    public boolean isActive(){return active;} public float getSweepAngle(){return sweepAngle;}
    public void setZoom(int v){zoom=clamp(v,1,20);setChanged();} public void setColorR(int v){colorR=clamp(v,0,255);setChanged();}
    public void setColorG(int v){colorG=clamp(v,0,255);setChanged();} public void setColorB(int v){colorB=clamp(v,0,255);setChanged();}
    public void setMode(int v){mode=clamp(v,0,MODE_COUNT-1);setChanged();} public void setSweepSpeed(int v){sweepSpeed=clamp(v,1,20);setChanged();}
    public void setRange(int v){range=clamp(v,1,64);setChanged();}
    private static int clamp(int v,int min,int max){return Math.max(min,Math.min(max,v));}
}
