package dev.aerolaser.client.gui;

import dev.aerolaser.block.VeilSpotlightMenu;
import dev.aerolaser.network.SpotlightConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class VeilSpotlightScreen extends AbstractContainerScreen<VeilSpotlightMenu> {

    private VeilSpotlightMenu spotMenu() { return (VeilSpotlightMenu) this.menu; }

    public VeilSpotlightScreen(VeilSpotlightMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 256;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos, y = topPos;

        // Red
        addRenderableWidget(makeBtn(x+10, y+30,  "-", () -> { spotMenu().setColorR(clamp(spotMenu().getColorR()-16,0,255));       send(); }));
        addRenderableWidget(makeBtn(x+220,y+30,  "+", () -> { spotMenu().setColorR(clamp(spotMenu().getColorR()+16,0,255));       send(); }));
        // Green
        addRenderableWidget(makeBtn(x+10, y+55,  "-", () -> { spotMenu().setColorG(clamp(spotMenu().getColorG()-16,0,255));       send(); }));
        addRenderableWidget(makeBtn(x+220,y+55,  "+", () -> { spotMenu().setColorG(clamp(spotMenu().getColorG()+16,0,255));       send(); }));
        // Blue
        addRenderableWidget(makeBtn(x+10, y+80,  "-", () -> { spotMenu().setColorB(clamp(spotMenu().getColorB()-16,0,255));       send(); }));
        addRenderableWidget(makeBtn(x+220,y+80,  "+", () -> { spotMenu().setColorB(clamp(spotMenu().getColorB()+16,0,255));       send(); }));
        // Brightness
        addRenderableWidget(makeBtn(x+10, y+105, "-", () -> { spotMenu().setBrightness(clampF(spotMenu().getBrightness()-0.5f,0.1f,20f)); send(); }));
        addRenderableWidget(makeBtn(x+220,y+105, "+", () -> { spotMenu().setBrightness(clampF(spotMenu().getBrightness()+0.5f,0.1f,20f)); send(); }));
        // Distance
        addRenderableWidget(makeBtn(x+10, y+130, "-", () -> { spotMenu().setDistance(clampF(spotMenu().getDistance()-2f,1f,64f)); send(); }));
        addRenderableWidget(makeBtn(x+220,y+130, "+", () -> { spotMenu().setDistance(clampF(spotMenu().getDistance()+2f,1f,64f)); send(); }));
        // Angle
        addRenderableWidget(makeBtn(x+10, y+155, "-", () -> { spotMenu().setAngle(clampF(spotMenu().getAngle()-5f,5f,90f));       send(); }));
        addRenderableWidget(makeBtn(x+220,y+155, "+", () -> { spotMenu().setAngle(clampF(spotMenu().getAngle()+5f,5f,90f));       send(); }));
        // Size
        addRenderableWidget(makeBtn(x+10, y+180, "-", () -> { spotMenu().setSizeX(clampF(spotMenu().getSizeX()-0.1f,0.1f,4f)); spotMenu().setSizeY(clampF(spotMenu().getSizeY()-0.1f,0.1f,4f)); send(); }));
        addRenderableWidget(makeBtn(x+220,y+180, "+", () -> { spotMenu().setSizeX(clampF(spotMenu().getSizeX()+0.1f,0.1f,4f)); spotMenu().setSizeY(clampF(spotMenu().getSizeY()+0.1f,0.1f,4f)); send(); }));
    }

    private Button makeBtn(int x, int y, String label, Runnable action) {
        return Button.builder(Component.literal(label), btn -> action.run()).pos(x, y).size(16, 16).build();
    }

    private void send() {
        PacketDistributor.sendToServer(new SpotlightConfigPacket(
                spotMenu().getBlockEntity().getBlockPos(),
                spotMenu().getColorR(), spotMenu().getColorG(), spotMenu().getColorB(),
                spotMenu().getBrightness(), spotMenu().getDistance(),
                spotMenu().getAngle(), spotMenu().getSizeX(), spotMenu().getSizeY()
        ));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partial, int mx, int my) {
        gfx.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC0D0D1A);
        int pr = spotMenu().getColorR(), pg = spotMenu().getColorG(), pb = spotMenu().getColorB();
        gfx.fill(leftPos+110, topPos+10, leftPos+150, topPos+26, 0xFF000000|(pr<<16)|(pg<<8)|pb);
    }

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float partial) {
        renderBackground(gfx, mx, my, partial);
        super.render(gfx, mx, my, partial);
        renderTooltip(gfx, mx, my);

        int x = leftPos, y = topPos;
        gfx.drawString(font, "Veil Spotlight",                                          x+8,  y+10,  0xFFFFFF, false);
        gfx.drawString(font, "Red:        " + spotMenu().getColorR(),                   x+32, y+33,  0xFF5555, false);
        gfx.drawString(font, "Green:      " + spotMenu().getColorG(),                   x+32, y+58,  0x55FF55, false);
        gfx.drawString(font, "Blue:       " + spotMenu().getColorB(),                   x+32, y+83,  0x5599FF, false);
        gfx.drawString(font, "Brightness: " + String.format("%.1f", spotMenu().getBrightness()), x+32, y+108, 0xFFDD44, false);
        gfx.drawString(font, "Distance:   " + String.format("%.0f blocks", spotMenu().getDistance()), x+32, y+133, 0xEEEEEE, false);
        gfx.drawString(font, "Angle:      " + String.format("%.0f°", spotMenu().getAngle()),     x+32, y+158, 0xEEEEEE, false);
        gfx.drawString(font, "Size:       " + String.format("%.1f", spotMenu().getSizeX()),      x+32, y+183, 0xEEEEEE, false);
        gfx.drawString(font, "(Ligue com redstone)", x+32, y+200, 0x888888, false);
    }

    private static int   clamp(int v, int min, int max)       { return Math.max(min, Math.min(max, v)); }
    private static float clampF(float v, float min, float max){ return Math.max(min, Math.min(max, v)); }
}
