package com.warfactory.ultimateweight.v1122.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.config.EquipmentBonusRules;
import com.warfactory.ultimateweight.config.InventoryGroupRules;
import com.warfactory.ultimateweight.core.InventoryConstraintEvaluator;
import com.warfactory.ultimateweight.v1122.UltimateWeightConfigFile1122;
import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public final class UltimateWeightClientEvents1122 {
    private static final ResourceLocation WEIGHT_ICON =
        new ResourceLocation(UltimateWeightCommon.MOD_ID, "textures/gui/weight.png");
    private static final ResourceLocation STAMINA_ICON =
        new ResourceLocation(UltimateWeightCommon.MOD_ID, "textures/gui/sprint.png");
    private static final int ICON_SIZE = 18;
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int OFFHAND_SLOT_WIDTH = 29;
    private static final int MARGIN = 4;
    private static final int ICON_TEXT_GAP = 2;

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        appendTooltip(event.getItemStack(), event.getToolTip(), event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null || minecraft.gameSettings.hideGUI) {
            return;
        }

        ScaledResolution resolution = new ScaledResolution(minecraft);
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        int centerX = screenWidth / 2;

        boolean hasOffhand = !player.getHeldItemOffhand().isEmpty();
        EnumHandSide offhandSide = player.getPrimaryHand().opposite();
        int leftEdge = centerX - HOTBAR_HALF_WIDTH
            - (hasOffhand && offhandSide == EnumHandSide.LEFT ? OFFHAND_SLOT_WIDTH : 0);
        int rightEdge = centerX + HOTBAR_HALF_WIDTH
            + (hasOffhand && offhandSide == EnumHandSide.RIGHT ? OFFHAND_SLOT_WIDTH : 0);

        int iconY = screenHeight - 20;  // 18px icon vertically centered in the 22px hotbar
        int textY = screenHeight - 15;  // 8px font centered against the icon

        // Weight: [icon] value, to the left of the hotbar.
        int weightColor = UltimateWeightState1122.hudColor(player);
        String weightText = UltimateWeightCommon.bootstrap().formatter()
            .formatHudWeight(UltimateWeightState1122.hudWeightKg(player));
        int weightGroupWidth = ICON_SIZE + ICON_TEXT_GAP + minecraft.fontRenderer.getStringWidth(weightText);
        int weightX = leftEdge - MARGIN - weightGroupWidth;
        drawIcon(minecraft, WEIGHT_ICON, weightX, iconY);
        minecraft.fontRenderer.drawStringWithShadow(
            weightText, (float) (weightX + ICON_SIZE + ICON_TEXT_GAP), (float) textY, weightColor);

        // Stamina: value [icon], to the right of the hotbar.
        if (UltimateWeightState1122.hudStaminaEnabled(player)) {
            int staminaColor = UltimateWeightState1122.hudStaminaColor(player);
            String staminaText = UltimateWeightCommon.bootstrap().formatter()
                .formatStaminaValue(UltimateWeightState1122.hudStamina(player));
            int staminaX = rightEdge + MARGIN;
            minecraft.fontRenderer.drawStringWithShadow(
                staminaText, (float) staminaX, (float) textY, staminaColor);
            int iconX = staminaX + minecraft.fontRenderer.getStringWidth(staminaText) + ICON_TEXT_GAP;
            drawIcon(minecraft, STAMINA_ICON, iconX, iconY);
        }
    }

    private static void drawIcon(Minecraft minecraft, ResourceLocation texture, int x, int y) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(texture);
        Gui.drawModalRectWithCustomSizedTexture(
            x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, (float) ICON_SIZE, (float) ICON_SIZE);
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        MovementInput input = event.getMovementInput();
        if (UltimateWeightState1122.effectiveSpeedMultiplier(event.getEntityPlayer()) < 0.00001D) {

            input.moveForward = 0.0F;
            input.moveStrafe = 0.0F;
            event.getEntityPlayer().setSprinting(false);
        }

        if (UltimateWeightState1122.effectiveJumpMultiplier(event.getEntityPlayer()) < 0.00001D) {
            input.jump = false;
        }

    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        UltimateWeightState1122.resetClientState();
        UltimateWeightConfigFile1122.reloadFromDisk();
    }

    private static void appendTooltip(ItemStack stack, java.util.List<String> lines, EntityPlayer player) {
        if (stack.isEmpty()) {
            return;
        }

        double singleWeightKg = UltimateWeightCommon.bootstrap().resolver().resolve(WeightViews1122.stack(stack)).singleItemWeightKg();
        if (singleWeightKg > 0.000001D) {
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

        InventoryConstraintEvaluator evaluator = UltimateWeightCommon.bootstrap().constraintEvaluator();
        for (InventoryConstraintEvaluator.GroupLimitDescription group : evaluator.describeStackGroups(
            player == null ? null : WeightViews1122.player(player),
            WeightViews1122.stack(stack)
        )) {
            lines.add(TextFormatting.DARK_AQUA + I18n.format(
                "tooltip.wfweight.group_limit",
                group.label(),
                Integer.valueOf(group.limit())
            ));
        }

        EquipmentBonusRules.EquipmentBonus bonus = evaluator.equipmentBonus(WeightViews1122.stack(stack));
        if (bonus.carryCapacityKg() > 0.000001D) {
            lines.add(TextFormatting.BLUE + I18n.format(
                "tooltip.wfweight.attr_carry_capacity",
                UltimateWeightCommon.bootstrap().formatter().formatTooltipWeight(bonus.carryCapacityKg())
            ));
        }
        if (bonus.stamina() > 0.000001D) {
            lines.add(TextFormatting.BLUE + I18n.format(
                "tooltip.wfweight.attr_stamina",
                UltimateWeightCommon.bootstrap().formatter().formatStaminaValue(bonus.stamina())
            ));
        }
        for (java.util.Map.Entry<String, Integer> entry : bonus.groupLimitBonuses().entrySet()) {
            InventoryGroupRules.GroupDefinition definition = UltimateWeightCommon.bootstrap().config().inventoryGroupRules().definition(entry.getKey());
            lines.add(TextFormatting.BLUE + I18n.format(
                "tooltip.wfweight.attr_group_limit",
                definition == null ? entry.getKey() : definition.label(),
                Integer.valueOf(entry.getValue().intValue())
            ));
        }
    }
}
