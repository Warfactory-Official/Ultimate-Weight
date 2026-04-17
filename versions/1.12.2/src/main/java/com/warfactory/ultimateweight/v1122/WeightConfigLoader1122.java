package com.warfactory.ultimateweight.v1122;

import com.warfactory.ultimateweight.config.IConfigLoader;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.config.WeightResolverRules;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
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

public final class WeightConfigLoader1122 implements IConfigLoader {
    public static final String FILE_NAME = "weight_config_1_12.yaml";

    @Override
    public String bundledResource() {
        return FILE_NAME;
    }

    @Override
    public WeightConfig loadBundled() {
        InputStream stream = WeightConfigLoader1122.class.getClassLoader().getResourceAsStream(FILE_NAME);
        if (stream == null) {
            return defaults();
        }
        try {
            return load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load bundled config " + FILE_NAME, exception);
        }
    }

    @Override
    public WeightConfig load(String text) {
        if (text == null || text.trim().isEmpty()) {
            return defaults();
        }
        Object rootObject = new Yaml().load(text);
        if (!(rootObject instanceof Map)) {
            return defaults();
        }
        return read(mapValue(rootObject));
    }

    @Override
    public WeightConfig load(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try {
            Object rootObject = new Yaml().load(reader);
            if (!(rootObject instanceof Map)) {
                return defaults();
            }
            return read(mapValue(rootObject));
        } finally {
            reader.close();
        }
    }

    @Override
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

    private static WeightConfig read(Map<String, Object> root) {
        WeightConfig defaults = defaults();
        WeightConfig.Precision defaultPrecision = defaults.precision();
        Map<String, Object> precision = mapValue(root.get("precision"));
        Map<String, Object> limits = mapValue(root.get("limits"));
        Map<String, Object> rules = mapValue(root.get("rules"));
        Map<String, Object> movement = mapValue(root.get("movement"));
        Map<String, Object> fallDamage = mapValue(root.get("fallDamage"));
        Map<String, Object> stamina = mapValue(root.get("stamina"));

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
            resolverRules(rules, defaults.resolverRules()),
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
                    defaults.stamina().sprintStaminaLossRate()
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

    private static WeightConfig defaults() {
        WeightResolverRules.Builder builder = new WeightResolverRules.Builder();
        builder.putExact("minecraft:water_bucket", 0, 4.5D);
        builder.putExact("minecraft:lava_bucket", 0, 4.8D);
        builder.putExact("minecraft:shield", 0, 5.0D);
        builder.putExact("minecraft:elytra", 0, 6.0D);
        builder.putWildcard("minecraft:shulker_box", 8.0D);
        builder.putMatch("cobblestone", 1.4D);
        builder.putMatch("stone", 1.5D);
        builder.putMatch("logWood", 2.4D);
        builder.putMatch("plankWood", 0.45D);
        builder.putMatch("stickWood", 0.08D);
        builder.putMatch("oreIron", 3.8D);
        builder.putMatch("ingotIron", 0.9D);
        builder.putMatch("ingotCopper", 0.85D);
        builder.putMatch("ingotGold", 1.3D);
        builder.putMatch("ingotSilver", 1.1D);
        builder.putMatch("gemEmerald", 0.32D);
        builder.putMatch("gemDiamond", 0.35D);
        builder.putMatch("dustRedstone", 0.05D);
        builder.putMatch("dustGlowstone", 0.07D);
        builder.putMatch("coal", 0.25D);
        builder.putMatch("paper", 0.03D);
        builder.putMatch("string", 0.02D);
        return WeightConfig.defaults(builder.build());
    }

    private static WeightResolverRules resolverRules(Map<String, Object> rules, WeightResolverRules fallback) {
        WeightResolverRules.Builder builder = new WeightResolverRules.Builder();
        copyRules(builder, fallback);

        Map<String, Object> exact = mapValue(rules.get("exact"));
        for (Map.Entry<String, Object> entry : exact.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null) {
                putLegacyExact(builder, entry.getKey(), value.doubleValue());
            }
        }

        Map<String, Object> wildcards = mapValue(rules.get("wildcards"));
        for (Map.Entry<String, Object> entry : wildcards.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null) {
                builder.putWildcard(entry.getKey(), value.doubleValue());
            }
        }

        Map<String, Object> oredict = mapValue(rules.get("oredict"));
        for (Map.Entry<String, Object> entry : oredict.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null) {
                builder.putMatch(entry.getKey(), value.doubleValue());
            }
        }

        return builder.build();
    }

    private static void putLegacyExact(WeightResolverRules.Builder builder, String key, double value) {
        if (key == null) {
            return;
        }
        String trimmed = key.trim();
        int separator = trimmed.lastIndexOf('@');
        if (separator < 0) {
            builder.putExact(trimmed, 0, value);
            return;
        }
        String itemId = trimmed.substring(0, separator).trim();
        String metaToken = trimmed.substring(separator + 1).trim();
        if ("*".equals(metaToken)) {
            builder.putWildcard(itemId, value);
            return;
        }
        try {
            builder.putExact(itemId, Integer.parseInt(metaToken), value);
        } catch (NumberFormatException ignored) {
        }
    }

    private static void copyRules(WeightResolverRules.Builder builder, WeightResolverRules rules) {
        Object2DoubleOpenHashMap<String> exact = rules.exactWeights();
        for (String key : exact.keySet()) {
            builder.putExactKey(key, exact.getDouble(key));
        }
        Object2DoubleOpenHashMap<String> wildcard = rules.wildcardWeights();
        for (String key : wildcard.keySet()) {
            builder.putWildcardKey(key, wildcard.getDouble(key));
        }
        Object2DoubleOpenHashMap<String> matches = rules.matchWeights();
        for (String key : matches.keySet()) {
            builder.putMatchKey(key, matches.getDouble(key));
        }
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
        LinkedHashMap<String, Double> exact = new LinkedHashMap<String, Double>();
        Object2DoubleOpenHashMap<String> exactRules = config.resolverRules().exactWeights();
        for (String key : exactRules.keySet()) {
            exact.put(key, Double.valueOf(exactRules.getDouble(key)));
        }
        LinkedHashMap<String, Double> wildcards = new LinkedHashMap<String, Double>();
        Object2DoubleOpenHashMap<String> wildcardRules = config.resolverRules().wildcardWeights();
        for (String key : wildcardRules.keySet()) {
            wildcards.put(key, Double.valueOf(wildcardRules.getDouble(key)));
        }
        LinkedHashMap<String, Double> oredict = new LinkedHashMap<String, Double>();
        Object2DoubleOpenHashMap<String> matchRules = config.resolverRules().matchWeights();
        for (String key : matchRules.keySet()) {
            oredict.put(key, Double.valueOf(matchRules.getDouble(key)));
        }
        rules.put("exact", exact);
        rules.put("wildcards", wildcards);
        rules.put("oredict", oredict);
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
        fallDamage.put("hardLockMultiplierBonus", Double.valueOf(config.fallDamage().hardLockMultiplierBonus()));
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

    private static Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map)) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() != null) {
                result.put(entry.getKey().toString(), entry.getValue());
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
        return value instanceof Boolean ? ((Boolean) value).booleanValue() : fallback;
    }

    private static Double numberValue(Object value) {
        return value instanceof Number ? Double.valueOf(((Number) value).doubleValue()) : null;
    }
}
