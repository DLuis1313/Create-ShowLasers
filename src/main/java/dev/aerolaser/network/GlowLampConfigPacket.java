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

public record GlowLampConfigPacket(
        BlockPos pos, int r, int g, int b, int size, boolean blink, int blinkSpeed
) implements CustomPacketPayload {

    public static final Type<GlowLampConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "glow_lamp_config"));

    public static final StreamCodec<FriendlyByteBuf, GlowLampConfigPacket> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeBlockPos(p.pos());
                        buf.writeVarInt(p.r()); buf.writeVarInt(p.g()); buf.writeVarInt(p.b());
                        buf.writeVarInt(p.size()); buf.writeBoolean(p.blink()); buf.writeVarInt(p.blinkSpeed());
                    },
                    buf -> new GlowLampConfigPacket(
                            buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readVarInt()
                    )
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GlowLampConfigPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            // Tenta no mundo do jogador primeiro (mundo normal)
            GlowLampBlockEntity be = findBE(player, pkt.pos());
            if (be == null) return;
            be.setColorR(pkt.r()); be.setColorG(pkt.g()); be.setColorB(pkt.b());
            be.setSize(pkt.size()); be.setBlinkEnabled(pkt.blink()); be.setBlinkSpeed(pkt.blinkSpeed());
        });
    }

    private static GlowLampBlockEntity findBE(ServerPlayer player, BlockPos pos) {
        // Busca no nível do jogador
        if (player.level().getBlockEntity(pos) instanceof GlowLampBlockEntity be) {
            double dx = player.getX()-(pos.getX()+0.5);
            double dy = player.getY()-(pos.getY()+0.5);
            double dz = player.getZ()-(pos.getZ()+0.5);
            if (dx*dx+dy*dy+dz*dz < 64) return be;
        }
        // Tenta no servidor inteiro (para sublevels/contraptions do Create)
        for (var level : player.getServer().getAllLevels()) {
            if (level.getBlockEntity(pos) instanceof GlowLampBlockEntity be) return be;
        }
        return null;
    }
}
