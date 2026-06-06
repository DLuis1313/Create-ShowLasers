package dev.aerolaser.network;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LaserConfigPacket(
        BlockPos pos, int zoom, int colorR, int colorG, int colorB,
        int mode, int sweepSpeed, int range
) implements CustomPacketPayload {

    public static final Type<LaserConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "laser_config"));

    public static final StreamCodec<FriendlyByteBuf, LaserConfigPacket> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeBlockPos(p.pos());
                        buf.writeVarInt(p.zoom());   buf.writeVarInt(p.colorR());
                        buf.writeVarInt(p.colorG()); buf.writeVarInt(p.colorB());
                        buf.writeVarInt(p.mode());   buf.writeVarInt(p.sweepSpeed());
                        buf.writeVarInt(p.range());
                    },
                    buf -> new LaserConfigPacket(
                            buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(),   buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(),   buf.readVarInt()
                    )
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(LaserConfigPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level().getBlockEntity(pkt.pos()) instanceof ShowLaserBlockEntity be) {
                double dx = player.getX()-(pkt.pos().getX()+0.5);
                double dy = player.getY()-(pkt.pos().getY()+0.5);
                double dz = player.getZ()-(pkt.pos().getZ()+0.5);
                if (dx*dx+dy*dy+dz*dz < 64) {
                    be.setZoom(pkt.zoom());       be.setColorR(pkt.colorR());
                    be.setColorG(pkt.colorG());   be.setColorB(pkt.colorB());
                    be.setMode(pkt.mode());       be.setSweepSpeed(pkt.sweepSpeed());
                    be.setRange(pkt.range());
                }
            }
        });
    }
}
