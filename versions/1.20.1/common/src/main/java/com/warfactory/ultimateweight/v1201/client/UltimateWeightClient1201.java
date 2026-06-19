package com.warfactory.ultimateweight.v1201.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class UltimateWeightClient1201 {
    private static final double EPSILON = 0.000001D;
    private static final int EXHAUSTED_HUD_COLOR = 11184810;

    private static final ResourceLocation WEIGHT_ICON =
        new ResourceLocation(UltimateWeightCommon.MOD_ID, "textures/gui/weight.png");
    private static final ResourceLocation STAMINA_ICON =
        new ResourceLocation(UltimateWeightCommon.MOD_ID, "textures/gui/sprint.png");
    private static final int ICON_SIZE = 18;
    // Half-width of the vanilla hotbar widget, and the width of the offhand slot box drawn beside it.
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int OFFHAND_SLOT_WIDTH = 29;
    private static final int MARGIN = 4;
    private static final int ICON_TEXT_GAP = 2;

    private UltimateWeightClient1201() {
    }

    public static void renderHud(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.options.hideGui || player == null) {
            return;
        }

        var latest = UltimateWeightClientState1201.latest();
        if (latest.carryCapacityKg() <= EPSILON) {
            return;
        }

        int centerX = graphics.guiWidth() / 2;
        int screenHeight = graphics.guiHeight();

        // The offhand slot box sits on the side opposite the main arm, but only when something is held
        // there - so the weight/stamina indicators must shift out by its width to clear it.
        boolean hasOffhand = !player.getOffhandItem().isEmpty();
        HumanoidArm offhandArm = player.getMainArm().getOpposite();
        int leftEdge = centerX - HOTBAR_HALF_WIDTH
            - (hasOffhand && offhandArm == HumanoidArm.LEFT ? OFFHAND_SLOT_WIDTH : 0);
        int rightEdge = centerX + HOTBAR_HALF_WIDTH
            + (hasOffhand && offhandArm == HumanoidArm.RIGHT ? OFFHAND_SLOT_WIDTH : 0);

        int iconY = screenHeight - 20;  // 18px icon vertically centered in the 22px hotbar
        int textY = screenHeight - 16;  // 9px font centered against the icon

        boolean exhausted = UltimateWeightClientState1201.isExhausted();

        // Weight: [icon] value, to the LEFT of the hotbar.
        int weightColor = hudColor(latest.totalWeightKg(), latest.carryCapacityKg(), latest.hardLocked(), exhausted);
        String weightText = UltimateWeightCommon.bootstrap().formatter().formatHudWeight(latest.totalWeightKg());
        int weightGroupWidth = ICON_SIZE + ICON_TEXT_GAP + minecraft.font.width(weightText);
        int weightX = leftEdge - MARGIN - weightGroupWidth;
        graphics.blit(WEIGHT_ICON, weightX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.drawString(minecraft.font, weightText, weightX + ICON_SIZE + ICON_TEXT_GAP, textY, weightColor, true);

        // Stamina: value [icon], to the RIGHT of the hotbar.
        var latestStamina = UltimateWeightClientState1201.latestStamina();
        if (latestStamina.staminaEnabled() && latestStamina.maxStamina() > EPSILON) {
            int staminaColor = staminaHudColor(latestStamina.currentStamina(), latestStamina.maxStamina(), exhausted);
            String staminaText = UltimateWeightCommon.bootstrap().formatter()
                .formatStaminaValue(latestStamina.currentStamina());
            int staminaX = rightEdge + MARGIN;
            graphics.drawString(minecraft.font, staminaText, staminaX, textY, staminaColor, true);
            int iconX = staminaX + minecraft.font.width(staminaText) + ICON_TEXT_GAP;
            graphics.blit(STAMINA_ICON, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }
    }

    private static int hudColor(double totalWeightKg, double carryCapacityKg, boolean hardLocked, boolean exhausted) {
        if (exhausted) {
            return EXHAUSTED_HUD_COLOR;
        }
        if (hardLocked) {
            return 14556416;
        }

        double loadPercent = carryCapacityKg <= EPSILON ? 0.0D : totalWeightKg / carryCapacityKg;
        if (loadPercent >= 1.0D) {
            return 14575104;
        }
        if (loadPercent >= 0.75D) {
            return 16759808;
        }
        return 10919845;
    }

    private static int staminaHudColor(double currentStamina, double maxStamina, boolean exhausted) {
        if (exhausted) {
            return EXHAUSTED_HUD_COLOR;
        }
        if (maxStamina <= EPSILON) {
            return 10919845;
        }

        double percent = currentStamina / maxStamina;
        if (percent <= 0.10D) {
            return 14556416;
        }
        if (percent <= 0.35D) {
            return 16759808;
        }
        return 5635925;
    }
}
