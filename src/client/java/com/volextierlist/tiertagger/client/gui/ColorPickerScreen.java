package com.volextierlist.tiertagger.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final Consumer<Integer> onColorSelected;
    private int selectedColor;
    
    private float hue = 0;
    private float saturation = 1;
    private float brightness = 1;
    
    private TextFieldWidget hexInput;
    private TextFieldWidget rInput;
    private TextFieldWidget gInput;
    private TextFieldWidget bInput;
    
    private static final int PICKER_SIZE = 200;
    private static final int HUE_BAR_WIDTH = 20;
    private int pickerX;
    private int pickerY;
    private int hueBarX;
    
    private boolean draggingSatBright = false;
    private boolean draggingHue = false;
    private boolean updatingFields = false;

    public ColorPickerScreen(Screen parent, int initialColor, Consumer<Integer> onColorSelected) {
        super(Text.literal("Pick a Color"));
        this.parent = parent;
        this.onColorSelected = onColorSelected;
        this.selectedColor = initialColor;
        this.updateHSBFromColor(initialColor);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        pickerX = centerX - PICKER_SIZE / 2;
        pickerY = 60;
        hueBarX = pickerX + PICKER_SIZE + 15;
        
        // Hex input
        this.hexInput = new TextFieldWidget(this.textRenderer, centerX - 100, pickerY + PICKER_SIZE + 20, 200, 20, Text.literal("Hex"));
        this.hexInput.setMaxLength(7);
        this.hexInput.setText(String.format("#%06X", this.selectedColor));
        this.hexInput.setChangedListener(hex -> {
            if (updatingFields) return;
            if (hex.startsWith("#") && hex.length() == 7) {
                try {
                    int color = Integer.parseInt(hex.substring(1), 16);
                    this.selectedColor = color;
                    this.updateHSBFromColor(color);
                    this.updateRGBInputs();
                } catch (NumberFormatException ignored) {}
            }
        });
        this.addDrawableChild(this.hexInput);
        
        // RGB inputs
        int rgbY = pickerY + PICKER_SIZE + 50;
        this.rInput = createRGBInput(centerX - 120, rgbY, "R");
        this.gInput = createRGBInput(centerX - 40, rgbY, "G");
        this.bInput = createRGBInput(centerX + 40, rgbY, "B");
        
        this.addDrawableChild(this.rInput);
        this.addDrawableChild(this.gInput);
        this.addDrawableChild(this.bInput);
        
        this.updateRGBInputs();
        
        // Done button - positioned below preview with safe spacing
        int doneButtonY = Math.max(pickerY + PICKER_SIZE + 130, this.height - 40);
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> {
                this.onColorSelected.accept(this.selectedColor);
                if (this.client != null) {
                    this.client.setScreen(parent);
                }
            }
        ).dimensions(centerX - 100, doneButtonY, 200, 20).build());
    }
    
    private TextFieldWidget createRGBInput(int x, int y, String label) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, 70, 20, Text.literal(label));
        field.setMaxLength(3);
        field.setChangedListener(value -> this.updateColorFromRGB());
        return field;
    }
    
    private void updateRGBInputs() {
        if (updatingFields) return;
        updatingFields = true;
        
        int r = (this.selectedColor >> 16) & 0xFF;
        int g = (this.selectedColor >> 8) & 0xFF;
        int b = this.selectedColor & 0xFF;
        
        this.rInput.setText(String.valueOf(r));
        this.gInput.setText(String.valueOf(g));
        this.bInput.setText(String.valueOf(b));
        
        updatingFields = false;
    }
    
    private void updateColorFromRGB() {
        if (updatingFields) return;
        updatingFields = true;
        
        try {
            int r = MathHelper.clamp(Integer.parseInt(this.rInput.getText()), 0, 255);
            int g = MathHelper.clamp(Integer.parseInt(this.gInput.getText()), 0, 255);
            int b = MathHelper.clamp(Integer.parseInt(this.bInput.getText()), 0, 255);
            
            this.selectedColor = (r << 16) | (g << 8) | b;
            this.hexInput.setText(String.format("#%06X", this.selectedColor));
            this.updateHSBFromColor(this.selectedColor);
        } catch (NumberFormatException ignored) {}
        
        updatingFields = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, Colors.WHITE);
        
        // Saturation-Brightness picker
        this.renderSaturationBrightnessPicker(context);
        
        // Hue bar
        this.renderHueBar(context);
        
        // Color preview
        int previewX = this.width / 2 - 50;
        int previewY = pickerY + PICKER_SIZE + 80;
        context.fill(previewX, previewY, previewX + 100, previewY + 30, 0xFF000000);
        context.fill(previewX + 1, previewY + 1, previewX + 99, previewY + 29, 0xFF000000 | this.selectedColor);
        
        // Labels
        context.drawText(this.textRenderer, "Hex:", this.width / 2 - 130, pickerY + PICKER_SIZE + 25, Colors.WHITE, false);
        context.drawText(this.textRenderer, "Red:", this.width / 2 - 150, pickerY + PICKER_SIZE + 55, Colors.WHITE, false);
        context.drawText(this.textRenderer, "Green:", this.width / 2 - 70, pickerY + PICKER_SIZE + 55, Colors.WHITE, false);
        context.drawText(this.textRenderer, "Blue:", this.width / 2 + 10, pickerY + PICKER_SIZE + 55, Colors.WHITE, false);
        context.drawText(this.textRenderer, "Preview:", previewX - 60, previewY + 10, Colors.WHITE, false);
    }
    
    private void renderSaturationBrightnessPicker(DrawContext context) {
        // Get base color from hue
        int baseColor = hsbToRgb(this.hue, 1f, 1f);
        
        // Draw gradient (brightness vertical, saturation horizontal)
        for (int y = 0; y < PICKER_SIZE; y++) {
            for (int x = 0; x < PICKER_SIZE; x++) {
                float sat = x / (float) PICKER_SIZE;
                float bright = 1f - (y / (float) PICKER_SIZE);
                
                int color = hsbToRgb(this.hue, sat, bright);
                context.fill(pickerX + x, pickerY + y, pickerX + x + 1, pickerY + y + 1, 0xFF000000 | color);
            }
        }
        
        // Draw selector circle
        int selectorX = pickerX + (int) (this.saturation * PICKER_SIZE);
        int selectorY = pickerY + (int) ((1f - this.brightness) * PICKER_SIZE);
        context.drawBorder(selectorX - 4, selectorY - 4, 8, 8, 0xFFFFFFFF);
        context.drawBorder(selectorX - 3, selectorY - 3, 6, 6, 0xFF000000);
    }
    
    private void renderHueBar(DrawContext context) {
        // Draw hue gradient
        for (int y = 0; y < PICKER_SIZE; y++) {
            float h = y / (float) PICKER_SIZE;
            int color = hsbToRgb(h, 1f, 1f);
            context.fill(hueBarX, pickerY + y, hueBarX + HUE_BAR_WIDTH, pickerY + y + 1, 0xFF000000 | color);
        }
        
        // Draw selector
        int selectorY = pickerY + (int) (this.hue * PICKER_SIZE);
        context.fill(hueBarX - 2, selectorY - 1, hueBarX + HUE_BAR_WIDTH + 2, selectorY + 2, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check sat/bright picker
            if (mouseX >= pickerX && mouseX <= pickerX + PICKER_SIZE &&
                mouseY >= pickerY && mouseY <= pickerY + PICKER_SIZE) {
                this.draggingSatBright = true;
                this.updateSatBright(mouseX, mouseY);
                return true;
            }
            
            // Check hue bar
            if (mouseX >= hueBarX && mouseX <= hueBarX + HUE_BAR_WIDTH &&
                mouseY >= pickerY && mouseY <= pickerY + PICKER_SIZE) {
                this.draggingHue = true;
                this.updateHue(mouseY);
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.draggingSatBright) {
            this.updateSatBright(mouseX, mouseY);
            return true;
        }
        
        if (this.draggingHue) {
            this.updateHue(mouseY);
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingSatBright = false;
        this.draggingHue = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    private void updateSatBright(double mouseX, double mouseY) {
        this.saturation = MathHelper.clamp((float) (mouseX - pickerX) / PICKER_SIZE, 0f, 1f);
        this.brightness = MathHelper.clamp(1f - (float) (mouseY - pickerY) / PICKER_SIZE, 0f, 1f);
        this.selectedColor = hsbToRgb(this.hue, this.saturation, this.brightness);
        
        updatingFields = true;
        this.hexInput.setText(String.format("#%06X", this.selectedColor));
        updatingFields = false;
        
        this.updateRGBInputs();
    }
    
    private void updateHue(double mouseY) {
        this.hue = MathHelper.clamp((float) (mouseY - pickerY) / PICKER_SIZE, 0f, 1f);
        this.selectedColor = hsbToRgb(this.hue, this.saturation, this.brightness);
        
        updatingFields = true;
        this.hexInput.setText(String.format("#%06X", this.selectedColor));
        updatingFields = false;
        
        this.updateRGBInputs();
    }
    
    private void updateHSBFromColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        
        float[] hsb = rgbToHsb(r, g, b);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }
    
    private static float[] rgbToHsb(int r, int g, int b) {
        float[] hsb = new float[3];
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        
        hsb[2] = max / 255f;
        
        if (max == 0) {
            hsb[1] = 0;
            hsb[0] = 0;
        } else {
            hsb[1] = (max - min) / (float) max;
            
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
    
    private static int hsbToRgb(float h, float s, float b) {
        int r = 0, g = 0, bl = 0;
        
        if (s == 0) {
            r = g = bl = (int) (b * 255f + 0.5f);
        } else {
            float h6 = (h - (float) Math.floor(h)) * 6f;
            float f = h6 - (float) Math.floor(h6);
            float p = b * (1f - s);
            float q = b * (1f - s * f);
            float t = b * (1f - s * (1f - f));
            
            switch ((int) h6) {
                case 0 -> { r = (int) (b * 255f + 0.5f); g = (int) (t * 255f + 0.5f); bl = (int) (p * 255f + 0.5f); }
                case 1 -> { r = (int) (q * 255f + 0.5f); g = (int) (b * 255f + 0.5f); bl = (int) (p * 255f + 0.5f); }
                case 2 -> { r = (int) (p * 255f + 0.5f); g = (int) (b * 255f + 0.5f); bl = (int) (t * 255f + 0.5f); }
                case 3 -> { r = (int) (p * 255f + 0.5f); g = (int) (q * 255f + 0.5f); bl = (int) (b * 255f + 0.5f); }
                case 4 -> { r = (int) (t * 255f + 0.5f); g = (int) (p * 255f + 0.5f); bl = (int) (b * 255f + 0.5f); }
                case 5 -> { r = (int) (b * 255f + 0.5f); g = (int) (p * 255f + 0.5f); bl = (int) (q * 255f + 0.5f); }
            }
        }
        
        return (r << 16) | (g << 8) | bl;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
