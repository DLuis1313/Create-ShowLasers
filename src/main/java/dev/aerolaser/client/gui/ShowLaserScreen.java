package dev.aerolaser.client.gui;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.ShowLaserMenu;
import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import dev.aerolaser.network.LaserConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * GUI for the Show Laser block.
 *
 * Layout (256 × 220 px):
 *   Title: "Show Laser"
 *   [Zoom -] [===|===] [+]      ← zoom 1–20
 *   [R -]    [===|===] [+]      ← red 0–255
 *   [G -]    [===|===] [+]      ← green 0–255
 *   [B -]    [===|===] [+]      ← blue 0–255
 *   [Speed -][===|===] [+]      ← sweep speed 1–20
 *   [Range -][===|===] [+]      ← range 1–64
 *   [ STATIC ] [ SWEEP ] [ SPIN ] [ BOUNCE ] [ PULSE ]
 */
public class ShowLaserScreen extends AbstractContainerScreen<ShowLaserMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AeroLaserMod.MOD_ID, "textures/gui/show_laser.png");

    private static final String[] MODE_NAMES = {"STATIC","SWEEP","SPIN","BOUNCE","PULSE"};

    private final ShowLaserMenu menu;

    public ShowLaserScreen(ShowLaserMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.menu = menu;
        this.imageWidth  = 256;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        // ── Zoom ─────────────────────────────────────────────────────────────
        addRenderableWidget(makeBtn(x+10, y+30, "-", () -> changeZoom(-1)));
        addRenderableWidget(makeBtn(x+220, y+30, "+", () -> changeZoom(+1)));

        // ── Red ───────────────────────────────────────────────────────────────
        addRenderableWidget(makeBtn(x+10, y+55, "-", () -> changeColor(0,-16)));
        addRenderableWidget(makeBtn(x+220, y+55, "+", () -> changeColor(0,+16)));

        // ── Green ────────────────────────────────────────────────────────────
        addRenderableWidget(makeBtn(x+10, y+80, "-", () -> changeColor(1,-16)));
        addRenderableWidget(makeBtn(x+220, y+80, "+", () -> changeColor(1,+16)));

        // ── Blue ─────────────────────────────────────────────────────────────
        addRenderableWidget(makeBtn(x+10, y+105, "-", () -> changeColor(2,-16)));
        addRenderableWidget(makeBtn(x+220, y+105, "+", () -> changeColor(2,+16)));

        // ── Sweep Speed ───────────────────────────────────────────────────────
        addRenderableWidget(makeBtn(x+10, y+130, "-", () -> changeSpeed(-1)));
        addRenderableWidget(makeBtn(x+220, y+130, "+", () -> changeSpeed(+1)));

        // ── Range ────────────────────────────────────────────────────────────
        addRenderableWidget(makeBtn(x+10, y+155, "-", () -> changeRange(-4)));
        addRenderableWidget(makeBtn(x+220, y+155, "+", () -> changeRange(+4)));

        // ── Mode buttons ─────────────────────────────────────────────────────
        for (int i = 0; i < MODE_NAMES.length; i++) {
            final int mode = i;
            int bx = x + 8 + i * 48;
            addRenderableWidget(Button.builder(
                    Component.literal(MODE_NAMES[i]),
                    btn -> setMode(mode))
                    .pos(bx, y + 185)
                    .size(46, 18)
                    .build());
        }
    }

    private Button makeBtn(int x, int y, String label, Runnable action) {
        return Button.builder(Component.literal(label), btn -> { action.run(); sendPacket(); })
                .pos(x, y).size(16, 16).build();
    }

    // ── Mutators ─────────────────────────────────────────────────────────────

    private void changeZoom(int delta)  { menu.setZoom(clamp(menu.getZoom()+delta,1,20)); sendPacket(); }
    private void changeSpeed(int delta) { menu.setSweepSpeed(clamp(menu.getSweepSpeed()+delta,1,20)); sendPacket(); }
    private void changeRange(int delta) { menu.setRange(clamp(menu.getRange()+delta,1,64)); sendPacket(); }

    private void changeColor(int channel, int delta) {
        switch (channel) {
            case 0 -> menu.setColorR(clamp(menu.getColorR()+delta,0,255));
            case 1 -> menu.setColorG(clamp(menu.getColorG()+delta,0,255));
            case 2 -> menu.setColorB(clamp(menu.getColorB()+delta,0,255));
        }
        sendPacket();
    }

    private void setMode(int mode) { menu.setMode(mode); sendPacket(); }

    private void sendPacket() {
        PacketDistributor.sendToServer(new LaserConfigPacket(
                menu.getBlockEntity().getBlockPos(),
                menu.getZoom(),
                menu.getColorR(),
                menu.getColorG(),
                menu.getColorB(),
                menu.getMode(),
                menu.getSweepSpeed(),
                menu.getRange()
        ));
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mx, int my) {
        // Draw a dark background panel (no custom texture needed, falls back to fill)
        gfx.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC222233);

        // Draw coloured preview square
        int pr = menu.getColorR(), pg = menu.getColorG(), pb = menu.getColorB();
        int previewColor = 0xFF000000 | (pr << 16) | (pg << 8) | pb;
        gfx.fill(leftPos + 105, topPos + 10, leftPos + 150, topPos + 26, previewColor);
    }

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float partialTick) {
        this.renderBackground(gfx, mx, my, partialTick);
        super.render(gfx, mx, my, partialTick);
        renderTooltip(gfx, mx, my);

        int x = leftPos, y = topPos;
        // Labels
        gfx.drawString(font, "Show Laser", x+8, y+10, 0xFFFFFF, false);
        gfx.drawString(font, "Zoom:  " + menu.getZoom(),                           x+32, y+33, 0xEEEEEE, false);
        gfx.drawString(font, "Red:   " + menu.getColorR(),                         x+32, y+58, 0xFF5555, false);
        gfx.drawString(font, "Green: " + menu.getColorG(),                         x+32, y+83, 0x55FF55, false);
        gfx.drawString(font, "Blue:  " + menu.getColorB(),                         x+32, y+108, 0x5555FF, false);
        gfx.drawString(font, "Speed: " + menu.getSweepSpeed(),                     x+32, y+133, 0xEEEEEE, false);
        gfx.drawString(font, "Range: " + menu.getRange() + " blocks",              x+32, y+158, 0xEEEEEE, false);
        gfx.drawString(font, "Mode:  " + MODE_NAMES[menu.getMode()],               x+32, y+175, 0xFFCC00, false);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
