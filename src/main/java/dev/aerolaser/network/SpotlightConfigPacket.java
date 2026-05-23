package dev.aerolaser.network;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.blockentity.VeilSpotlightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpotlightConfigPacket(
        BlockPos pos, int r, int g, int b,
        float brightness, float distance, float angle, float sizeX, float sizeY
) implements CustomPacketPayload {

    public static final Type<SpotlightConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "spotlight_config"));

    public static final StreamCodec<FriendlyByteBuf, SpotlightConfigPacket> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeBlockPos(p.pos());
                        buf.writeVarInt(p.r()); buf.writeVarInt(p.g()); buf.writeVarInt(p.b());
                        buf.writeFloat(p.brightness()); buf.writeFloat(p.distance());
                        buf.writeFloat(p.angle()); buf.writeFloat(p.sizeX()); buf.writeFloat(p.sizeY());
                    },
                    buf -> new SpotlightConfigPacket(
                            buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()
                    )
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SpotlightConfigPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level().getBlockEntity(pkt.pos()) instanceof VeilSpotlightBlockEntity be) {
                double dx = player.getX()-(pkt.pos().getX()+0.5);
                double dy = player.getY()-(pkt.pos().getY()+0.5);
                double dz = player.getZ()-(pkt.pos().getZ()+0.5);
                if (dx*dx+dy*dy+dz*dz < 64) {
                    be.setColorR(pkt.r()); be.setColorG(pkt.g()); be.setColorB(pkt.b());
                    be.setBrightness(pkt.brightness()); be.setDistance(pkt.distance());
                    be.setAngle(pkt.angle()); be.setSizeX(pkt.sizeX()); be.setSizeY(pkt.sizeY());
                }
            }
        });
    }
}
