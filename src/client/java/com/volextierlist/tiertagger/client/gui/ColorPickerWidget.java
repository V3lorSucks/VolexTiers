package com.volextierlist.tiertagger.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ColorPickerWidget extends ClickableWidget {
    private int color;
    private final Consumer<Integer> onColorChange;
    private boolean expanded = false;
    private int hue = 0;
    private int saturation = 100;
    private int brightness = 100;

    public ColorPickerWidget(int x, int y, int width, int height, int initialColor, Consumer<Integer> onColorChange) {
        super(x, y, width, height, Text.empty());
        this.color = initialColor;
        this.onColorChange = onColorChange;
        this.updateHSBFromColor();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw color preview box
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
        context.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF000000 | this.color);
        
        // Draw border
        int borderColor = this.isHovered() ? 0xFFFFFFFF : 0xFF8B8B8B;
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.expanded = !this.expanded;
        if (this.expanded && MinecraftClient.getInstance().currentScreen != null) {
            MinecraftClient.getInstance().setScreen(
                new ColorPickerScreen(
                    MinecraftClient.getInstance().currentScreen,
                    this.color,
                    newColor -> {
                        this.color = newColor;
                        this.onColorChange.accept(newColor);
                        this.updateHSBFromColor();
                    }
                )
            );
        }
    }

    private void updateHSBFromColor() {
        int r = (this.color >> 16) & 0xFF;
        int g = (this.color >> 8) & 0xFF;
        int b = this.color & 0xFF;
        
        float[] hsb = rgbToHsb(r, g, b);
        this.hue = (int) (hsb[0] * 360);
        this.saturation = (int) (hsb[1] * 100);
        this.brightness = (int) (hsb[2] * 100);
    }

    private static float[] rgbToHsb(int r, int g, int b) {
        float[] hsb = new float[3];
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        
        // Brightness
        hsb[2] = max / 255f;
        
        if (max == 0) {
            hsb[1] = 0;
            hsb[0] = 0;
        } else {
            // Saturation
            hsb[1] = (max - min) / (float) max;
            
            // Hue
            float delta = max - min;
            if (delta == 0) {
                hsb[0] = 0;
            } else if (max == r) {
                hsb[0] = ((g - b) / delta % 6) / 6f;
            } else if (max == g) {
                hsb[0] = ((b - r) / delta + 2) / 6f;
            } else {
                hsb[0] = ((r - g) / delta + 4) / 6f;
            }
            
            if (hsb[0] < 0) hsb[0] += 1;
        }
        
        return hsb;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
        this.updateHSBFromColor();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, "Color picker");
    }
}
