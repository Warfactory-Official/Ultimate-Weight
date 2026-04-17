package com.warfactory.ultimateweight.v1201.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class UltimateWeightClient1201 {
    private static final double EPSILON = 0.000001D;
    private static final int EXHAUSTED_HUD_COLOR = 11184810;

    private UltimateWeightClient1201() {
    }

    public static void renderHud(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null) {
            return;
        }

        var latest = UltimateWeightClientState1201.latest();
        if (latest.carryCapacityKg() <= EPSILON) {
            return;
        }

        int x = 8;
        int y = 8;
        boolean exhausted = UltimateWeightClientState1201.isExhausted();
        int color = hudColor(latest.totalWeightKg(), latest.carryCapacityKg(), latest.hardLocked(), exhausted);
        int accent = (color & 16777215) | -16777216;
        Component text = Component.translatable(
            "hud.wfweight.status",
            UltimateWeightCommon.bootstrap().formatter().formatHudWeight(latest.totalWeightKg()),
            UltimateWeightCommon.bootstrap().formatter().formatHudPercent(
                latest.totalWeightKg(),
                latest.carryCapacityKg()
            )
        );

        graphics.fill(x, y + 8, x + 12, y + 10, accent);
        graphics.fill(x + 2, y + 2, x + 4, y + 8, accent);
        graphics.fill(x + 8, y + 2, x + 10, y + 8, accent);
        graphics.fill(x + 3, y + 2, x + 9, y + 3, accent);
        graphics.drawString(minecraft.font, text, x + 16, y + 2, color, true);

        var latestStamina = UltimateWeightClientState1201.latestStamina();
        if (latestStamina.staminaEnabled() && latestStamina.maxStamina() > EPSILON) {
            int staminaY = y + 14;
            int staminaColor = staminaHudColor(latestStamina.currentStamina(), latestStamina.maxStamina(), exhausted);
            int staminaAccent = (staminaColor & 16777215) | -16777216;
            Component staminaText = Component.translatable(
                "hud.wfweight.stamina",
                UltimateWeightCommon.bootstrap().formatter().formatStaminaValue(latestStamina.currentStamina()),
                UltimateWeightCommon.bootstrap().formatter().formatStaminaValue(latestStamina.maxStamina()),
                UltimateWeightCommon.bootstrap().formatter().formatStaminaPercent(
                    latestStamina.currentStamina(),
                    latestStamina.maxStamina()
                )
            );

            graphics.fill(x, staminaY + 8, x + 12, staminaY + 10, staminaAccent);
            graphics.fill(x + 2, staminaY + 2, x + 4, staminaY + 8, staminaAccent);
            graphics.fill(x + 8, staminaY + 2, x + 10, staminaY + 8, staminaAccent);
            graphics.fill(x + 3, staminaY + 2, x + 9, staminaY + 3, staminaAccent);
            graphics.drawString(minecraft.font, staminaText, x + 16, staminaY + 2, staminaColor, true);
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
