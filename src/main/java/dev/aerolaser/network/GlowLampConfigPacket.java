package dev.aerolaser.network;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.blockentity.GlowLampBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GlowLampConfigPacket(BlockPos pos, int r, int g, int b, int size)
        implements CustomPacketPayload {

    public static final Type<GlowLampConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "glow_lamp_config"));

    public static final StreamCodec<FriendlyByteBuf, GlowLampConfigPacket> CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeBlockPos(p.pos()); buf.writeVarInt(p.r());
                        buf.writeVarInt(p.g()); buf.writeVarInt(p.b()); buf.writeVarInt(p.size()); },
                    buf -> new GlowLampConfigPacket(buf.readBlockPos(),
                            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GlowLampConfigPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level().getBlockEntity(pkt.pos()) instanceof GlowLampBlockEntity be) {
                double dx = player.getX()-(pkt.pos().getX()+0.5);
                double dy = player.getY()-(pkt.pos().getY()+0.5);
                double dz = player.getZ()-(pkt.pos().getZ()+0.5);
                if (dx*dx+dy*dy+dz*dz < 64) {
                    be.setColorR(pkt.r()); be.setColorG(pkt.g());
                    be.setColorB(pkt.b()); be.setSize(pkt.size());
                }
            }
        });
    }
}
