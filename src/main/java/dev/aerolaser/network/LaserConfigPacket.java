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

/**
 * Sent from the client GUI to the server whenever the player
 * changes a laser setting (zoom, colour, mode, etc.).
 */
public record LaserConfigPacket(
        BlockPos pos,
        int zoom,
        int colorR,
        int colorG,
        int colorB,
        int mode,
        int sweepSpeed,
        int range
) implements CustomPacketPayload {

    public static final Type<LaserConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "laser_config"));

    public static final StreamCodec<FriendlyByteBuf, LaserConfigPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeBlockPos(pkt.pos);
                        buf.writeVarInt(pkt.zoom);
                        buf.writeVarInt(pkt.colorR);
                        buf.writeVarInt(pkt.colorG);
                        buf.writeVarInt(pkt.colorB);
                        buf.writeVarInt(pkt.mode);
                        buf.writeVarInt(pkt.sweepSpeed);
                        buf.writeVarInt(pkt.range);
                    },
                    buf -> new LaserConfigPacket(
                            buf.readBlockPos(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Server-side handler */
    public static void handle(LaserConfigPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level().getBlockEntity(pkt.pos) instanceof ShowLaserBlockEntity be) {
                if (be.stillValid(player)) {
                    be.setZoom(pkt.zoom);
                    be.setColorR(pkt.colorR);
                    be.setColorG(pkt.colorG);
                    be.setColorB(pkt.colorB);
                    be.setMode(pkt.mode);
                    be.setSweepSpeed(pkt.sweepSpeed);
                    be.setRange(pkt.range);
                }
            }
        });
    }

    /** Helper: is this player close enough to the block? */
    private static boolean stillValid(ShowLaserBlockEntity be, ServerPlayer player) {
        double dx = player.getX() - (be.getBlockPos().getX() + 0.5);
        double dy = player.getY() - (be.getBlockPos().getY() + 0.5);
        double dz = player.getZ() - (be.getBlockPos().getZ() + 0.5);
        return dx*dx + dy*dy + dz*dz < 64;
    }
}
