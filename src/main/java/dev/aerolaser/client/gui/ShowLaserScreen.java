package dev.aerolaser.client.gui;

import dev.aerolaser.block.ShowLaserMenu;
import dev.aerolaser.network.LaserConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ShowLaserScreen extends AbstractContainerScreen<ShowLaserMenu> {

    private static final String[] MODE_NAMES = {"STATIC","SWEEP","SPIN","BOUNCE","PULSE"};
    private ShowLaserMenu m() { return (ShowLaserMenu) this.menu; }

    public ShowLaserScreen(ShowLaserMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 256; imageHeight = 210;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos, y = topPos;
        addRenderableWidget(btn(x+10, y+30, "-", () -> { m().setZoom(clamp(m().getZoom()-1,1,20));                send(); }));
        addRenderableWidget(btn(x+220,y+30, "+", () -> { m().setZoom(clamp(m().getZoom()+1,1,20));                send(); }));
        addRenderableWidget(btn(x+10, y+55, "-", () -> { m().setColorR(clamp(m().getColorR()-16,0,255));          send(); }));
        addRenderableWidget(btn(x+220,y+55, "+", () -> { m().setColorR(clamp(m().getColorR()+16,0,255));          send(); }));
        addRenderableWidget(btn(x+10, y+80, "-", () -> { m().setColorG(clamp(m().getColorG()-16,0,255));          send(); }));
        addRenderableWidget(btn(x+220,y+80, "+", () -> { m().setColorG(clamp(m().getColorG()+16,0,255));          send(); }));
        addRenderableWidget(btn(x+10, y+105,"-", () -> { m().setColorB(clamp(m().getColorB()-16,0,255));          send(); }));
        addRenderableWidget(btn(x+220,y+105,"+", () -> { m().setColorB(clamp(m().getColorB()+16,0,255));          send(); }));
        addRenderableWidget(btn(x+10, y+130,"-", () -> { m().setSweepSpeed(clamp(m().getSweepSpeed()-1,1,20));    send(); }));
        addRenderableWidget(btn(x+220,y+130,"+", () -> { m().setSweepSpeed(clamp(m().getSweepSpeed()+1,1,20));    send(); }));
        addRenderableWidget(btn(x+10, y+155,"-", () -> { m().setRange(clamp(m().getRange()-4,1,64));              send(); }));
        addRenderableWidget(btn(x+220,y+155,"+", () -> { m().setRange(clamp(m().getRange()+4,1,64));              send(); }));
        for (int i = 0; i < MODE_NAMES.length; i++) {
            final int mode = i;
            addRenderableWidget(Button.builder(Component.literal(MODE_NAMES[i]),
                    b -> { m().setMode(mode); send(); })
                    .pos(x + 8 + i*48, y+180).size(46,18).build());
        }
    }

    private Button btn(int x, int y, String label, Runnable action) {
        return Button.builder(Component.literal(label), b -> action.run()).pos(x,y).size(16,16).build();
    }

    private void send() {
        PacketDistributor.sendToServer(new LaserConfigPacket(
                m().getBlockEntity().getBlockPos(),
                m().getZoom(), m().getColorR(), m().getColorG(), m().getColorB(),
                m().getMode(), m().getSweepSpeed(), m().getRange()
        ));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partial, int mx, int my) {
        gfx.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, 0xCC1A1A2E);
        int pr=m().getColorR(), pg=m().getColorG(), pb=m().getColorB();
        gfx.fill(leftPos+105, topPos+10, leftPos+150, topPos+26, 0xFF000000|(pr<<16)|(pg<<8)|pb);
    }

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float partial) {
        renderBackground(gfx, mx, my, partial);
        super.render(gfx, mx, my, partial);
        renderTooltip(gfx, mx, my);
        int x=leftPos, y=topPos;
        gfx.drawString(font, "Show Laser",                              x+8,  y+10,  0xFFFFFF, false);
        gfx.drawString(font, "Zoom:  " +m().getZoom(),                  x+32, y+33,  0xEEEEEE, false);
        gfx.drawString(font, "Red:   " +m().getColorR(),                x+32, y+58,  0xFF5555, false);
        gfx.drawString(font, "Green: " +m().getColorG(),                x+32, y+83,  0x55FF55, false);
        gfx.drawString(font, "Blue:  " +m().getColorB(),                x+32, y+108, 0x5555FF, false);
        gfx.drawString(font, "Speed: " +m().getSweepSpeed(),            x+32, y+133, 0xEEEEEE, false);
        gfx.drawString(font, "Range: " +m().getRange()+" blocks",       x+32, y+158, 0xEEEEEE, false);
        gfx.drawString(font, "Mode:  " +MODE_NAMES[m().getMode()],      x+32, y+175, 0xFFCC00, false);
    }

    private static int clamp(int v, int min, int max) { return Math.max(min,Math.min(max,v)); }
}
