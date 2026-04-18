package com.warfactory.ultimateweight.core;

import com.warfactory.ultimateweight.config.WeightConfig;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class WeightFormatter {
    private final WeightConfig.Precision precision;

    public WeightFormatter(WeightConfig.Precision precision) {
        this.precision = precision;
    }

    public String formatHud(double totalWeightKg, double carryCapacityKg) {
        return formatHudWeight(totalWeightKg) + " (" + formatHudPercent(totalWeightKg, carryCapacityKg) + ")";
    }

    public String formatHudWeight(double totalWeightKg) {
        return format(totalWeightKg, precision.hudDecimals()) + "kg";
    }

    public String formatHudPercent(double totalWeightKg, double carryCapacityKg) {
        double percent = carryCapacityKg <= 0.0D ? 0.0D : (totalWeightKg / carryCapacityKg) * 100.0D;
        return format(percent, 0) + "%";
    }

    public String formatTooltipWeight(double weightKg) {
        return format(weightKg, precision.tooltipDecimals()) + "kg";
    }

    public String formatStackWeight(double weightKg) {
        return format(weightKg, precision.stackDecimals()) + "kg";
    }

    public String formatStaminaValue(double stamina) {
        return format(stamina, precision.hudDecimals());
    }

    public String formatStaminaPercent(double currentStamina, double maxStamina) {
        double percent = maxStamina <= 0.0D ? 0.0D : (currentStamina / maxStamina) * 100.0D;
        return format(percent, 0) + "%";
    }

    private static String format(double value, int decimals) {
        DecimalFormat format = new DecimalFormat(pattern(decimals), DecimalFormatSymbols.getInstance(Locale.ROOT));
        format.setGroupingUsed(false);
        return format.format(value);
    }

    private static String pattern(int decimals) {
        if (decimals <= 0) {
            return "0";
        }

        StringBuilder builder = new StringBuilder("0.");
        for (int index = 0; index < decimals; index++) {
            builder.append('0');
        }
        return builder.toString();
    }
}
