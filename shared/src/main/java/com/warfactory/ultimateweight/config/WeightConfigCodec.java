package com.warfactory.ultimateweight.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public final class WeightConfigCodec {
    public WeightConfig read(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try {
            return read(reader);
        } finally {
            reader.close();
        }
    }

    public WeightConfig read(Reader reader) {
        Object rootObject = new Yaml().load(reader);
        if (!(rootObject instanceof Map)) {
            return WeightConfig.defaults();
        }

        Map<String, Object> root = mapValue(rootObject);
        Map<String, Object> precision = mapValue(root.get("precision"));
        Map<String, Object> limits = mapValue(root.get("limits"));
        Map<String, Object> rules = mapValue(root.get("rules"));
        Map<String, Object> movement = mapValue(root.get("movement"));
        Map<String, Object> fallDamage = mapValue(root.get("fallDamage"));
        Map<String, Object> stamina = mapValue(root.get("stamina"));

        WeightConfig defaults = WeightConfig.defaults();
        WeightConfig.Precision defaultPrecision = defaults.precision();

        return new WeightConfig(
            new WeightConfig.Precision(
                intValue(precision, "hudDecimals", defaultPrecision.hudDecimals()),
                intValue(precision, "tooltipDecimals", defaultPrecision.tooltipDecimals()),
                intValue(precision, "stackDecimals", defaultPrecision.stackDecimals())
            ),
            booleanValue(limits, "enableFailsafeFullScan", defaults.enableFailsafeFullScan()),
            longValue(limits, "fullScanIntervalTicks", defaults.fullScanIntervalTicks()),
            doubleValue(limits, "defaultCarryCapacityKg", defaults.defaultCarryCapacityKg()),
            doubleValue(limits, "hardLockWeightKg", defaults.hardLockWeightKg()),
            stringValue(rules, "componentOverrideKey", defaults.componentOverrideKey()),
            doubleMap(rules, "exact", defaults.exactWeightsKg()),
            doubleMap(rules, "groups", defaults.groupWeightsKg()),
            doubleMap(rules, "prefixes", defaults.prefixWeightsKg()),
            thresholdRules(movement, defaults.thresholds()),
            new WeightConfig.FallDamage(
                booleanValue(fallDamage, "enabled", defaults.fallDamage().enabled()),
                doubleValue(fallDamage, "startLoadPercent", defaults.fallDamage().startLoadPercent()),
                doubleValue(
                    fallDamage,
                    "extraDamageMultiplierPerLoadPercent",
                    defaults.fallDamage().extraDamageMultiplierPerLoadPercent()
                ),
                doubleValue(
                    fallDamage,
                    "hardLockMultiplierBonus",
                    defaults.fallDamage().hardLockMultiplierBonus()
                ),
                doubleValue(fallDamage, "maxDamageMultiplier", defaults.fallDamage().maxDamageMultiplier())
            ),
            new WeightConfig.Stamina(
                doubleValue(stamina, "totalStamina", defaults.stamina().totalStamina()),
                doubleValue(
                    stamina,
                    "sprintStaminaLossRate",
                    doubleValue(
                        stamina,
                        "staminaLossRate",
                        doubleValue(stamina, "setStaminaLossRate", defaults.stamina().sprintStaminaLossRate())
                    )
                ),
                doubleValue(stamina, "jumpStaminaLoss", defaults.stamina().jumpStaminaLoss()),
                doubleValue(stamina, "staminaGainRate", defaults.stamina().staminaGainRate()),
                doubleValue(stamina, "exhaustionThreshold", defaults.stamina().exhaustionThreshold()),
                doubleValue(stamina, "recoveryPercent", defaults.stamina().recoveryPercent()),
                booleanValue(stamina, "drainWhileRunning", defaults.stamina().drainWhileRunning()),
                booleanValue(stamina, "drainOnJump", defaults.stamina().drainOnJump()),
                staminaPenaltyRules(stamina, defaults.stamina().penalties())
            )
        );
    }

    public WeightConfig read(String yamlText) {
        if (yamlText == null || yamlText.trim().isEmpty()) {
            return WeightConfig.defaults();
        }
        Object rootObject = new Yaml().load(yamlText);
        if (!(rootObject instanceof Map)) {
            return WeightConfig.defaults();
        }
        return read(new java.io.StringReader(yamlText));
    }

    public String write(WeightConfig config) {
        LinkedHashMap<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("precision", precisionMap(config));
        root.put("limits", limitsMap(config));
        root.put("rules", rulesMap(config));
        root.put("movement", movementMap(config));
        root.put("fallDamage", fallDamageMap(config));
        root.put("stamina", staminaMap(config));

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        return new Yaml(options).dump(root);
    }

    private static Map<String, Object> precisionMap(WeightConfig config) {
        LinkedHashMap<String, Object> precision = new LinkedHashMap<String, Object>();
        precision.put("hudDecimals", Integer.valueOf(config.precision().hudDecimals()));
        precision.put("tooltipDecimals", Integer.valueOf(config.precision().tooltipDecimals()));
        precision.put("stackDecimals", Integer.valueOf(config.precision().stackDecimals()));
        return precision;
    }

    private static Map<String, Object> limitsMap(WeightConfig config) {
        LinkedHashMap<String, Object> limits = new LinkedHashMap<String, Object>();
        limits.put("defaultCarryCapacityKg", Double.valueOf(config.defaultCarryCapacityKg()));
        limits.put("hardLockWeightKg", Double.valueOf(config.hardLockWeightKg()));
        limits.put("enableFailsafeFullScan", Boolean.valueOf(config.enableFailsafeFullScan()));
        limits.put("fullScanIntervalTicks", Long.valueOf(config.fullScanIntervalTicks()));
        return limits;
    }

    private static Map<String, Object> rulesMap(WeightConfig config) {
        LinkedHashMap<String, Object> rules = new LinkedHashMap<String, Object>();
        rules.put("componentOverrideKey", config.componentOverrideKey());
        rules.put("exact", new LinkedHashMap<String, Double>(config.exactWeightsKg()));
        rules.put("groups", new LinkedHashMap<String, Double>(config.groupWeightsKg()));
        rules.put("prefixes", new LinkedHashMap<String, Double>(config.prefixWeightsKg()));
        return rules;
    }

    private static Map<String, Object> movementMap(WeightConfig config) {
        ArrayList<Map<String, Object>> thresholds = new ArrayList<Map<String, Object>>();
        for (WeightConfig.ThresholdRule threshold : config.thresholds()) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("percent", Double.valueOf(threshold.percent()));
            row.put("speedMultiplier", Double.valueOf(threshold.speedMultiplier()));
            row.put("jumpMultiplier", Double.valueOf(threshold.jumpMultiplier()));
            thresholds.add(row);
        }

        LinkedHashMap<String, Object> movement = new LinkedHashMap<String, Object>();
        movement.put("thresholds", thresholds);
        return movement;
    }

    private static Map<String, Object> fallDamageMap(WeightConfig config) {
        LinkedHashMap<String, Object> fallDamage = new LinkedHashMap<String, Object>();
        fallDamage.put("enabled", Boolean.valueOf(config.fallDamage().enabled()));
        fallDamage.put("startLoadPercent", Double.valueOf(config.fallDamage().startLoadPercent()));
        fallDamage.put(
            "extraDamageMultiplierPerLoadPercent",
            Double.valueOf(config.fallDamage().extraDamageMultiplierPerLoadPercent())
        );
        fallDamage.put(
            "hardLockMultiplierBonus",
            Double.valueOf(config.fallDamage().hardLockMultiplierBonus())
        );
        fallDamage.put("maxDamageMultiplier", Double.valueOf(config.fallDamage().maxDamageMultiplier()));
        return fallDamage;
    }

    private static Map<String, Object> staminaMap(WeightConfig config) {
        ArrayList<Map<String, Object>> penalties = new ArrayList<Map<String, Object>>();
        for (WeightConfig.UsagePenaltyRule penalty : config.stamina().penalties()) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("percent", Double.valueOf(penalty.percent()));
            row.put("useMultiplier", Double.valueOf(penalty.useMultiplier()));
            penalties.add(row);
        }

        LinkedHashMap<String, Object> stamina = new LinkedHashMap<String, Object>();
        stamina.put("totalStamina", Double.valueOf(config.stamina().totalStamina()));
        stamina.put("sprintStaminaLossRate", Double.valueOf(config.stamina().sprintStaminaLossRate()));
        stamina.put("jumpStaminaLoss", Double.valueOf(config.stamina().jumpStaminaLoss()));
        stamina.put("staminaGainRate", Double.valueOf(config.stamina().staminaGainRate()));
        stamina.put("exhaustionThreshold", Double.valueOf(config.stamina().exhaustionThreshold()));
        stamina.put("recoveryPercent", Double.valueOf(config.stamina().recoveryPercent()));
        stamina.put("drainWhileRunning", Boolean.valueOf(config.stamina().drainWhileRunning()));
        stamina.put("drainOnJump", Boolean.valueOf(config.stamina().drainOnJump()));
        stamina.put("penalties", penalties);
        return stamina;
    }

    private static Map<String, Double> doubleMap(
        Map<String, Object> source,
        String key,
        Map<String, Double> fallback
    ) {
        Map<String, Object> raw = mapValue(source.get(key));
        if (raw.isEmpty()) {
            return fallback;
        }

        LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null) {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    private static List<WeightConfig.ThresholdRule> thresholdRules(
        Map<String, Object> movement,
        List<WeightConfig.ThresholdRule> fallback
    ) {
        Object rawThresholds = movement.get("thresholds");
        if (!(rawThresholds instanceof Iterable)) {
            return fallback;
        }

        ArrayList<WeightConfig.ThresholdRule> thresholds = new ArrayList<WeightConfig.ThresholdRule>();
        Iterator<?> iterator = ((Iterable<?>) rawThresholds).iterator();
        while (iterator.hasNext()) {
            Map<String, Object> row = mapValue(iterator.next());
            if (!row.isEmpty()) {
                thresholds.add(
                    new WeightConfig.ThresholdRule(
                        doubleValue(row, "percent", 0.0D),
                        doubleValue(row, "speedMultiplier", 1.0D),
                        doubleValue(row, "jumpMultiplier", 1.0D)
                    )
                );
            }
        }
        return thresholds.isEmpty() ? fallback : thresholds;
    }

    private static List<WeightConfig.UsagePenaltyRule> staminaPenaltyRules(
        Map<String, Object> stamina,
        List<WeightConfig.UsagePenaltyRule> fallback
    ) {
        Object rawPenalties = stamina.get("penalties");
        if (!(rawPenalties instanceof Iterable)) {
            return fallback;
        }

        ArrayList<WeightConfig.UsagePenaltyRule> penalties = new ArrayList<WeightConfig.UsagePenaltyRule>();
        Iterator<?> iterator = ((Iterable<?>) rawPenalties).iterator();
        while (iterator.hasNext()) {
            Map<String, Object> row = mapValue(iterator.next());
            if (!row.isEmpty()) {
                penalties.add(
                    new WeightConfig.UsagePenaltyRule(
                        doubleValue(row, "percent", 0.0D),
                        doubleValue(row, "useMultiplier", 1.0D)
                    )
                );
            }
        }
        return penalties.isEmpty() ? fallback : penalties;
    }

    private static Map<String, Object> mapValue(Object raw) {
        if (!(raw instanceof Map)) {
            return Collections.emptyMap();
        }

        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) raw).entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private static int intValue(Map<String, Object> source, String key, int fallback) {
        Double value = numberValue(source.get(key));
        return value == null ? fallback : value.intValue();
    }

    private static long longValue(Map<String, Object> source, String key, long fallback) {
        Double value = numberValue(source.get(key));
        return value == null ? fallback : value.longValue();
    }

    private static double doubleValue(Map<String, Object> source, String key, double fallback) {
        Double value = numberValue(source.get(key));
        return value == null ? fallback : value.doubleValue();
    }

    private static boolean booleanValue(Map<String, Object> source, String key, boolean fallback) {
        Object value = source.get(key);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }

    private static String stringValue(Map<String, Object> source, String key, String fallback) {
        Object value = source.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static Double numberValue(Object value) {
        if (value instanceof Number) {
            return Double.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return Double.valueOf(Double.parseDouble((String) value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
