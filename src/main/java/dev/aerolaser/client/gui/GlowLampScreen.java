package dev.aerolaser.client.gui;

import dev.aerolaser.block.GlowLampMenu;
import dev.aerolaser.network.GlowLampConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class GlowLampScreen extends AbstractContainerScreen<GlowLampMenu> {

    private GlowLampMenu m() { return (GlowLampMenu) this.menu; }

    public GlowLampScreen(GlowLampMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 220; imageHeight = 185;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos, y = topPos;

        // Cor R/G/B
        addRenderableWidget(btn(x+10,y+30,"-",  () -> { m().setColorR(clamp(m().getColorR()-16,0,255)); send(); }));
        addRenderableWidget(btn(x+195,y+30,"+", () -> { m().setColorR(clamp(m().getColorR()+16,0,255)); send(); }));
        addRenderableWidget(btn(x+10,y+55,"-",  () -> { m().setColorG(clamp(m().getColorG()-16,0,255)); send(); }));
        addRenderableWidget(btn(x+195,y+55,"+", () -> { m().setColorG(clamp(m().getColorG()+16,0,255)); send(); }));
        addRenderableWidget(btn(x+10,y+80,"-",  () -> { m().setColorB(clamp(m().getColorB()-16,0,255)); send(); }));
        addRenderableWidget(btn(x+195,y+80,"+", () -> { m().setColorB(clamp(m().getColorB()+16,0,255)); send(); }));

        // Tamanho
        addRenderableWidget(btn(x+10,y+105,"-", () -> { m().setSize(clamp(m().getSizeInt()-1,5,30)); send(); }));
        addRenderableWidget(btn(x+195,y+105,"+",() -> { m().setSize(clamp(m().getSizeInt()+1,5,30)); send(); }));

        // Toggle piscar
        addRenderableWidget(Button.builder(
                Component.literal(m().isBlinkEnabled() ? "Piscar: ON" : "Piscar: OFF"),
                btn -> {
                    m().toggleBlink();
                    btn.setMessage(Component.literal(m().isBlinkEnabled() ? "Piscar: ON" : "Piscar: OFF"));
                    send();
                }).pos(x+10, y+130).size(90, 18).build());

        // Velocidade do piscar
        addRenderableWidget(btn(x+110,y+130,"-",() -> { m().setBlinkSpeed(clamp(m().getBlinkSpeed()-1,1,20)); send(); }));
        addRenderableWidget(btn(x+195,y+130,"+",() -> { m().setBlinkSpeed(clamp(m().getBlinkSpeed()+1,1,20)); send(); }));
    }

    private Button btn(int x, int y, String label, Runnable action) {
        return Button.builder(Component.literal(label), b -> action.run()).pos(x,y).size(16,16).build();
    }

    private void send() {
        PacketDistributor.sendToServer(new GlowLampConfigPacket(
                m().getBlockEntity().getBlockPos(),
                m().getColorR(), m().getColorG(), m().getColorB(),
                m().getSizeInt(), m().isBlinkEnabled(), m().getBlinkSpeed()
        ));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partial, int mx, int my) {
        gfx.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, 0xCC0E0E18);
        // Preview de cor
        int pr=m().getColorR(), pg=m().getColorG(), pb=m().getColorB();
        gfx.fill(leftPos+75, topPos+10, leftPos+115, topPos+25, 0xFF000000|(pr<<16)|(pg<<8)|pb);
    }

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float partial) {
        renderBackground(gfx, mx, my, partial);
        super.render(gfx, mx, my, partial);
        renderTooltip(gfx, mx, my);

        int x=leftPos, y=topPos;
        gfx.drawString(font, "Glow Lamp",                                     x+8,  y+10,  0xFFFFFF, false);
        gfx.drawString(font, "Red:    " + m().getColorR(),                     x+30, y+33,  0xFF6666, false);
        gfx.drawString(font, "Green:  " + m().getColorG(),                     x+30, y+58,  0x66FF66, false);
        gfx.drawString(font, "Blue:   " + m().getColorB(),                     x+30, y+83,  0x6699FF, false);
        gfx.drawString(font, "Size:   " + String.format("%.1f",m().getSize()), x+30, y+108, 0xFFDD44, false);
        gfx.drawString(font, "Vel.:   " + m().getBlinkSpeed(),                 x+130,y+133, 0xAAAAAA, false);
        gfx.drawString(font, "(Ligue com redstone)",                            x+30, y+158, 0x666666, false);
        // Aviso sobre sublevel
        gfx.drawString(font, "Nota: salve antes de mover",                     x+8,  y+170, 0x555555, false);
    }

    private static int clamp(int v, int min, int max) { return Math.max(min,Math.min(max,v)); }
}
