package com.warfactory.ultimateweight.v1122.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1122.UltimateWeightConfigFile1122;
import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public final class UltimateWeightClientEvents1122 {
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        appendTooltip(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.hideGUI) {
            return;
        }

        int x = 8;
        int y = 8;
        int color = UltimateWeightState1122.hudColor(minecraft.player);
        int accent = -16777216 | (color & 16777215);
        double totalWeightKg = UltimateWeightState1122.hudWeightKg(minecraft.player);
        double carryCapacityKg = UltimateWeightState1122.hudCarryCapacityKg(minecraft.player);
        String text = I18n.format(
            "hud.wfweight.status",
            UltimateWeightCommon.bootstrap().formatter().formatHudWeight(totalWeightKg),
            UltimateWeightCommon.bootstrap().formatter().formatHudPercent(totalWeightKg, carryCapacityKg)
        );

        Gui.drawRect(x, y + 8, x + 12, y + 10, accent);
        Gui.drawRect(x + 2, y + 2, x + 4, y + 8, accent);
        Gui.drawRect(x + 8, y + 2, x + 10, y + 8, accent);
        Gui.drawRect(x + 3, y + 2, x + 9, y + 3, accent);
        minecraft.fontRenderer.drawStringWithShadow(text, (float) (x + 16), (float) (y + 2), color);

        if (UltimateWeightState1122.hudStaminaEnabled(minecraft.player)) {
            int staminaY = y + 14;
            int staminaColor = UltimateWeightState1122.hudStaminaColor(minecraft.player);
            int staminaAccent = -16777216 | (staminaColor & 16777215);
            double currentStamina = UltimateWeightState1122.hudStamina(minecraft.player);
            double maxStamina = UltimateWeightState1122.hudMaxStamina(minecraft.player);
            String staminaText = I18n.format(
                "hud.wfweight.stamina",
                UltimateWeightCommon.bootstrap().formatter().formatStaminaValue(currentStamina),
                UltimateWeightCommon.bootstrap().formatter().formatStaminaValue(maxStamina),
                UltimateWeightCommon.bootstrap().formatter().formatStaminaPercent(currentStamina, maxStamina)
            );

            Gui.drawRect(x, staminaY + 8, x + 12, staminaY + 10, staminaAccent);
            Gui.drawRect(x + 2, staminaY + 2, x + 4, staminaY + 8, staminaAccent);
            Gui.drawRect(x + 8, staminaY + 2, x + 10, staminaY + 8, staminaAccent);
            Gui.drawRect(x + 3, staminaY + 2, x + 9, staminaY + 3, staminaAccent);
            minecraft.fontRenderer.drawStringWithShadow(staminaText, (float) (x + 16), (float) (staminaY + 2), staminaColor);
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        UltimateWeightState1122.resetClientState();
        UltimateWeightConfigFile1122.reloadFromDisk();
    }

    private static void appendTooltip(ItemStack stack, java.util.List<String> lines) {
        if (stack.isEmpty()) {
            return;
        }

        double singleWeightKg = UltimateWeightCommon.bootstrap().resolver().resolve(WeightViews1122.stack(stack)).singleItemWeightKg();
        if (singleWeightKg <= 0.000001D) {
            return;
        }

        lines.add(TextFormatting.GRAY + I18n.format(
            "tooltip.wfweight.weight",
            UltimateWeightCommon.bootstrap().formatter().formatTooltipWeight(singleWeightKg)
        ));
        if (stack.getCount() > 1) {
            lines.add(TextFormatting.DARK_GRAY + I18n.format(
                "tooltip.wfweight.stack_weight",
                UltimateWeightCommon.bootstrap().formatter().formatStackWeight(singleWeightKg * stack.getCount())
            ));
        }
    }
}
