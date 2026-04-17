package com.warfactory.ultimateweight.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v261.WeightViews261;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class UltimateWeightFabric261Client implements ClientModInitializer {
    private static final Identifier HUD_LAYER = Identifier.fromNamespaceAndPath(UltimateWeightCommon.MOD_ID, "hud");
    private static final double EPSILON = 0.000001D;

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> appendTooltip(stack, lines));
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, HUD_LAYER, (graphics, tickCounter) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.options.hideGui || minecraft.player == null) {
                return;
            }

            Player player = minecraft.player;
            double totalWeightKg = WeightViews261.totalWeight(player);
            double carryCapacityKg = UltimateWeightCommon.bootstrap().config().defaultCarryCapacityKg();
            if (carryCapacityKg <= EPSILON) {
                return;
            }

            int x = 8;
            int y = 8;
            int color = hudColor(totalWeightKg, carryCapacityKg);
            int accent = (color & 16777215) | -16777216;
            Component text = Component.translatable(
                "hud.wfweight.status",
                UltimateWeightCommon.bootstrap().formatter().formatHudWeight(totalWeightKg),
                UltimateWeightCommon.bootstrap().formatter().formatHudPercent(totalWeightKg, carryCapacityKg)
            );

            graphics.fill(x, y + 8, x + 12, y + 10, accent);
            graphics.fill(x + 2, y + 2, x + 4, y + 8, accent);
            graphics.fill(x + 8, y + 2, x + 10, y + 8, accent);
            graphics.fill(x + 3, y + 2, x + 9, y + 3, accent);
            graphics.text(minecraft.font, text, x + 16, y + 2, color, true);
        });
    }

    private static void appendTooltip(ItemStack stack, java.util.List<Component> lines) {
        if (stack.isEmpty()) {
            return;
        }

        double singleWeightKg = UltimateWeightCommon.bootstrap().resolver().resolve(WeightViews261.stack(stack)).singleItemWeightKg();
        if (singleWeightKg <= EPSILON) {
            return;
        }

        lines.add(
            Component.translatable(
                "tooltip.wfweight.weight",
                UltimateWeightCommon.bootstrap().formatter().formatTooltipWeight(singleWeightKg)
            )
        );
        if (stack.getCount() > 1) {
            lines.add(
                Component.translatable(
                    "tooltip.wfweight.stack_weight",
                    UltimateWeightCommon.bootstrap().formatter().formatStackWeight(singleWeightKg * stack.getCount())
                )
            );
        }
    }

    private static int hudColor(double totalWeightKg, double carryCapacityKg) {
        double loadPercent = totalWeightKg / carryCapacityKg;
        if (loadPercent >= 1.0D) {
            return 14575104;
        }
        if (loadPercent >= 0.75D) {
            return 16759808;
        }
        return 10919845;
    }
}
