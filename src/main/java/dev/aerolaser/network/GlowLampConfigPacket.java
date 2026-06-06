package dev.aerolaser.network;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.GlowLampMenu;
import dev.aerolaser.blockentity.GlowLampBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Usa o containerId do menu ao invés de BlockPos para identificar o BE.
 * Isso corrige o problema de sublevels/contraptions em movimento do Create:
 * o servidor busca pelo menu ativo do jogador, que já tem referência direta ao BE
 * independente de qual Level ele esteja.
 */
public record GlowLampConfigPacket(
        int containerId, int r, int g, int b, int size, boolean blink, int blinkSpeed
) implements CustomPacketPayload {

    public static final Type<GlowLampConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "glow_lamp_config"));

    public static final StreamCodec<FriendlyByteBuf, GlowLampConfigPacket> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.containerId());
                        buf.writeVarInt(p.r()); buf.writeVarInt(p.g()); buf.writeVarInt(p.b());
                        buf.writeVarInt(p.size()); buf.writeBoolean(p.blink()); buf.writeVarInt(p.blinkSpeed());
                    },
                    buf -> new GlowLampConfigPacket(
                            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readVarInt()
                    )
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GlowLampConfigPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            // Busca pelo menu ativo — funciona em qualquer Level, inclusive contraptions
            if (player.containerMenu.containerId == pkt.containerId()
                    && player.containerMenu instanceof GlowLampMenu m) {
                GlowLampBlockEntity be = m.getBlockEntity();
                if (be != null) {
                    be.setColorR(pkt.r());   be.setColorG(pkt.g());
                    be.setColorB(pkt.b());   be.setSize(pkt.size());
                    be.setBlinkEnabled(pkt.blink()); be.setBlinkSpeed(pkt.blinkSpeed());
                }
            }
        });
    }
}
