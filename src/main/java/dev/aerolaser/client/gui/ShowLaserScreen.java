package dev.aerolaser.client.gui;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.ShowLaserMenu;
import dev.aerolaser.network.LaserConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

public class ShowLaserScreen extends AbstractContainerScreen<ShowLaserMenu> {

    private static final String[] MODE_NAMES = {"STATIC","SWEEP","SPIN","BOUNCE","PULSE"};
    private final boolean veilInstalled;

    public ShowLaserScreen(ShowLaserMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 256;
        this.imageHeight = 230;
        this.veilInstalled = ModList.get().isLoaded("veil");
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos, y = topPos;

        addRenderableWidget(makeBtn(x+10, y+30,  "-", () -> { menu.setZoom(clamp(menu.getZoom()-1,1,20));           sendPacket(); }));
        addRenderableWidget(makeBtn(x+220,y+30,  "+", () -> { menu.setZoom(clamp(menu.getZoom()+1,1,20));           sendPacket(); }));
        addRenderableWidget(makeBtn(x+10, y+55,  "-", () -> { menu.setColorR(clamp(menu.getColorR()-16,0,255));     sendPacket(); }));
        addRenderableWidget(makeBtn(x+220,y+55,  "+", () -> { menu.setColorR(clamp(menu.getColorR()+16,0,255));     sendPacket(); }));
        addRenderableWidget(makeBtn(x+10, y+80,  "-", () -> { menu.setColorG(clamp(menu.getColorG()-16,0,255));     sendPacket(); }));
        addRenderableWidget(makeBtn(x+220,y+80,  "+", () -> { menu.setColorG(clamp(menu.getColorG()+16,0,255));     sendPacket(); }));
        addRenderableWidget(makeBtn(x+10, y+105, "-", () -> { menu.setColorB(clamp(menu.getColorB()-16,0,255));     sendPacket(); }));
        addRenderableWidget(makeBtn(x+220,y+105, "+", () -> { menu.setColorB(clamp(menu.getColorB()+16,0,255));     sendPacket(); }));
        addRenderableWidget(makeBtn(x+10, y+130, "-", () -> { menu.setSweepSpeed(clamp(menu.getSweepSpeed()-1,1,20)); sendPacket(); }));
        addRenderableWidget(makeBtn(x+220,y+130, "+", () -> { menu.setSweepSpeed(clamp(menu.getSweepSpeed()+1,1,20)); sendPacket(); }));
        addRenderableWidget(makeBtn(x+10, y+155, "-", () -> { menu.setRange(clamp(menu.getRange()-4,1,64));         sendPacket(); }));
        addRenderableWidget(makeBtn(x+220,y+155, "+", () -> { menu.setRange(clamp(menu.getRange()+4,1,64));         sendPacket(); }));

        // Botões de modo
        for (int i = 0; i < MODE_NAMES.length; i++) {
            final int mode = i;
            addRenderableWidget(Button.builder(Component.literal(MODE_NAMES[i]),
                    btn -> { menu.setMode(mode); sendPacket(); })
                    .pos(x + 8 + i * 48, y + 180).size(46, 18).build());
        }

        // Botão toggle Veil — só aparece se o Veil estiver instalado
        if (veilInstalled) {
            addRenderableWidget(Button.builder(
                    Component.literal(menu.isUseVeil() ? "Veil: ON" : "Veil: OFF"),
                    btn -> {
                        menu.toggleVeil();
                        btn.setMessage(Component.literal(menu.isUseVeil() ? "Veil: ON" : "Veil: OFF"));
                        sendPacket();
                    })
                    .pos(x + 8, y + 205).size(100, 18).build());
        }
    }

    private Button makeBtn(int x, int y, String label, Runnable action) {
        return Button.builder(Component.literal(label), btn -> action.run())
                .pos(x, y).size(16, 16).build();
    }

    private void sendPacket() {
        PacketDistributor.sendToServer(new LaserConfigPacket(
                menu.getBlockEntity().getBlockPos(),
                menu.getZoom(), menu.getColorR(), menu.getColorG(), menu.getColorB(),
                menu.getMode(), menu.getSweepSpeed(), menu.getRange(), menu.isUseVeil()
        ));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partial, int mx, int my) {
        gfx.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC1A1A2E);
        int pr = menu.getColorR(), pg = menu.getColorG(), pb = menu.getColorB();
        gfx.fill(leftPos+105, topPos+10, leftPos+150, topPos+26,
                0xFF000000 | (pr << 16) | (pg << 8) | pb);
    }

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float partial) {
        renderBackground(gfx, mx, my, partial);
        super.render(gfx, mx, my, partial);
        renderTooltip(gfx, mx, my);

        int x = leftPos, y = topPos;
        gfx.drawString(font, "Show Laser",                              x+8,  y+10,  0xFFFFFF, false);
        gfx.drawString(font, "Zoom:  "  + menu.getZoom(),              x+32, y+33,  0xEEEEEE, false);
        gfx.drawString(font, "Red:   "  + menu.getColorR(),            x+32, y+58,  0xFF5555, false);
        gfx.drawString(font, "Green: "  + menu.getColorG(),            x+32, y+83,  0x55FF55, false);
        gfx.drawString(font, "Blue:  "  + menu.getColorB(),            x+32, y+108, 0x5555FF, false);
        gfx.drawString(font, "Speed: "  + menu.getSweepSpeed(),        x+32, y+133, 0xEEEEEE, false);
        gfx.drawString(font, "Range: "  + menu.getRange() + " blocks", x+32, y+158, 0xEEEEEE, false);
        gfx.drawString(font, "Mode:  "  + MODE_NAMES[menu.getMode()],  x+32, y+175, 0xFFCC00, false);

        if (!veilInstalled) {
            gfx.drawString(font, "Veil: nao instalado", x+8, y+208, 0x888888, false);
        }
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}
