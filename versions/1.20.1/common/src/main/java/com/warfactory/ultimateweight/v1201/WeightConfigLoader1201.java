package com.warfactory.ultimateweight.v1201;

import com.warfactory.ultimateweight.config.*;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class WeightConfigLoader1201 implements IConfigLoader {
    public static final String FILE_NAME = "weight_config_modern.yaml";

    @Override
    public String bundledResource() {
        return FILE_NAME;
    }

    @Override
    public WeightConfig loadBundled() {
        InputStream stream = WeightConfigLoader1201.class.getClassLoader().getResourceAsStream(FILE_NAME);
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
        root.put("groupLimits", groupLimitsMap(config));
        root.put("equipmentBonuses", equipmentBonusesMap(config));
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
        Map<String, Object> groupLimits = mapValue(root.get("groupLimits"));
        Map<String, Object> equipmentBonuses = mapValue(root.get("equipmentBonuses"));

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
            inventoryGroups(groupLimits, defaults.inventoryGroupRules()),
            equipmentBonuses(equipmentBonuses, defaults.equipmentBonusRules()),
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
                doubleValue(stamina, "sprintStaminaLossRate", defaults.stamina().sprintStaminaLossRate()),
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
        builder.putMatch("minecraft:planks", 0.45D);
        builder.putMatch("minecraft:logs", 2.4D);
        builder.putMatch("c:ingots/iron", 0.9D);
        builder.putMatch("forge:ingots/iron", 0.9D);
        builder.putMatch("c:ingots/copper", 0.85D);
        builder.putMatch("forge:ingots/copper", 0.85D);
        builder.putMatch("c:ingots/gold", 1.3D);
        builder.putMatch("forge:ingots/gold", 1.3D);
        builder.putMatch("c:gems/diamond", 0.35D);
        builder.putMatch("forge:gems/diamond", 0.35D);
        builder.putMatch("c:gems/emerald", 0.32D);
        builder.putMatch("forge:gems/emerald", 0.32D);
        builder.putMatch("c:dusts/redstone", 0.05D);
        builder.putMatch("forge:dusts/redstone", 0.05D);
        builder.putMatch("minecraft:coals", 0.25D);
        return WeightConfig.defaults(builder.build());
    }

    private static WeightResolverRules resolverRules(Map<String, Object> rules, WeightResolverRules fallback) {
        WeightResolverRules.Builder builder = new WeightResolverRules.Builder();
        copyRules(builder, fallback);

        Map<String, Object> exact = mapValue(rules.get("exact"));
        for (Map.Entry<String, Object> entry : exact.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null) {
                builder.putExact(entry.getKey(), 0, value.doubleValue());
            }
        }

        Map<String, Object> wildcards = mapValue(rules.get("wildcards"));
        for (Map.Entry<String, Object> entry : wildcards.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null) {
                builder.putWildcard(entry.getKey(), value.doubleValue());
            }
        }

        Map<String, Object> tags = mapValue(rules.get("tags"));
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null) {
                builder.putMatch(entry.getKey(), value.doubleValue());
            }
        }

        return builder.build();
    }

    private static InventoryGroupRules inventoryGroups(Map<String, Object> groups, InventoryGroupRules fallback) {
        InventoryGroupRules.Builder builder = new InventoryGroupRules.Builder();
        copyGroups(builder, fallback);
        for (Map.Entry<String, Object> entry : groups.entrySet()) {
            String groupId = entry.getKey();
            Map<String, Object> group = mapValue(entry.getValue());
            if (groupId == null || groupId.trim().isEmpty()) {
                continue;
            }
            builder.define(groupId, stringValue(group, "label", groupId), intValue(group, "limit", 0));
            for (String exact : stringList(group.get("exact"))) {
                builder.addExact(groupId, exact, 0);
            }
            for (String wildcard : stringList(group.get("wildcards"))) {
                builder.addWildcard(groupId, wildcard);
            }
            for (String match : stringList(group.get("tags"))) {
                builder.addMatch(groupId, match);
            }
        }
        return builder.build();
    }

    private static EquipmentBonusRules equipmentBonuses(Map<String, Object> bonuses, EquipmentBonusRules fallback) {
        EquipmentBonusRules.Builder builder = new EquipmentBonusRules.Builder();
        copyEquipmentBonuses(builder, fallback);

        Map<String, Object> exact = mapValue(bonuses.get("exact"));
        for (Map.Entry<String, Object> entry : exact.entrySet()) {
            EquipmentBonusRules.EquipmentBonus bonus = equipmentBonus(mapValue(entry.getValue()));
            if (!bonus.isEmpty()) {
                builder.putExact(entry.getKey(), 0, bonus);
            }
        }

        Map<String, Object> wildcards = mapValue(bonuses.get("wildcards"));
        for (Map.Entry<String, Object> entry : wildcards.entrySet()) {
            EquipmentBonusRules.EquipmentBonus bonus = equipmentBonus(mapValue(entry.getValue()));
            if (!bonus.isEmpty()) {
                builder.putWildcard(entry.getKey(), bonus);
            }
        }
        return builder.build();
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

    private static void copyGroups(InventoryGroupRules.Builder builder, InventoryGroupRules rules) {
        for (InventoryGroupRules.GroupDefinition definition : rules.definitions()) {
            builder.define(definition.id(), definition.label(), definition.limit());
        }
        for (Map.Entry<String, List<String>> entry : rules.exactMatches().entrySet()) {
            for (String groupId : entry.getValue()) {
                String key = stripModernExactKey(entry.getKey());
                builder.addExact(groupId, key, 0);
            }
        }
        for (Map.Entry<String, List<String>> entry : rules.wildcardMatches().entrySet()) {
            for (String groupId : entry.getValue()) {
                builder.addWildcard(groupId, entry.getKey());
            }
        }
        for (Map.Entry<String, List<String>> entry : rules.dictionaryMatches().entrySet()) {
            for (String groupId : entry.getValue()) {
                builder.addMatch(groupId, entry.getKey());
            }
        }
    }

    private static void copyEquipmentBonuses(EquipmentBonusRules.Builder builder, EquipmentBonusRules rules) {
        for (Map.Entry<String, EquipmentBonusRules.EquipmentBonus> entry : rules.exactBonuses().entrySet()) {
            builder.putExact(stripModernExactKey(entry.getKey()), 0, entry.getValue());
        }
        for (Map.Entry<String, EquipmentBonusRules.EquipmentBonus> entry : rules.wildcardBonuses().entrySet()) {
            builder.putWildcardKey(entry.getKey(), entry.getValue());
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
            exact.put(stripModernExactKey(key), Double.valueOf(exactRules.getDouble(key)));
        }
        LinkedHashMap<String, Double> wildcards = new LinkedHashMap<String, Double>();
        Object2DoubleOpenHashMap<String> wildcardRules = config.resolverRules().wildcardWeights();
        for (String key : wildcardRules.keySet()) {
            wildcards.put(key, Double.valueOf(wildcardRules.getDouble(key)));
        }
        LinkedHashMap<String, Double> tags = new LinkedHashMap<String, Double>();
        Object2DoubleOpenHashMap<String> matchRules = config.resolverRules().matchWeights();
        for (String key : matchRules.keySet()) {
            tags.put(key, Double.valueOf(matchRules.getDouble(key)));
        }
        rules.put("exact", exact);
        rules.put("wildcards", wildcards);
        rules.put("tags", tags);
        return rules;
    }

    private static Map<String, Object> groupLimitsMap(WeightConfig config) {
        LinkedHashMap<String, Object> groups = new LinkedHashMap<String, Object>();
        InventoryGroupRules rules = config.inventoryGroupRules();
        for (InventoryGroupRules.GroupDefinition definition : rules.definitions()) {
            LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
            group.put("label", definition.label());
            group.put("limit", Integer.valueOf(definition.limit()));
            group.put("exact", entriesForGroup(rules.exactMatches(), definition.id(), true));
            group.put("wildcards", entriesForGroup(rules.wildcardMatches(), definition.id(), false));
            group.put("tags", entriesForGroup(rules.dictionaryMatches(), definition.id(), false));
            groups.put(definition.id(), group);
        }
        return groups;
    }

    private static Map<String, Object> equipmentBonusesMap(WeightConfig config) {
        LinkedHashMap<String, Object> root = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> exact = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, EquipmentBonusRules.EquipmentBonus> entry : config.equipmentBonusRules().exactBonuses().entrySet()) {
            exact.put(stripModernExactKey(entry.getKey()), equipmentBonusMap(entry.getValue()));
        }
        LinkedHashMap<String, Object> wildcards = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, EquipmentBonusRules.EquipmentBonus> entry : config.equipmentBonusRules().wildcardBonuses().entrySet()) {
            wildcards.put(entry.getKey(), equipmentBonusMap(entry.getValue()));
        }
        root.put("exact", exact);
        root.put("wildcards", wildcards);
        return root;
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

    private static EquipmentBonusRules.EquipmentBonus equipmentBonus(Map<String, Object> source) {
        LinkedHashMap<String, Integer> groupBonuses = new LinkedHashMap<String, Integer>();
        Map<String, Object> groupLimits = mapValue(source.get("groupLimits"));
        for (Map.Entry<String, Object> entry : groupLimits.entrySet()) {
            Double value = numberValue(entry.getValue());
            if (value != null && Math.abs(value.doubleValue()) > 0.000001D) {
                groupBonuses.put(entry.getKey(), Integer.valueOf(value.intValue()));
            }
        }
        return new EquipmentBonusRules.EquipmentBonus(
            doubleValue(source, "carryCapacityKg", 0.0D),
            doubleValue(source, "stamina", 0.0D),
            groupBonuses
        );
    }

    private static Map<String, Object> equipmentBonusMap(EquipmentBonusRules.EquipmentBonus bonus) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("carryCapacityKg", Double.valueOf(bonus.carryCapacityKg()));
        map.put("stamina", Double.valueOf(bonus.stamina()));
        map.put("groupLimits", new LinkedHashMap<String, Integer>(bonus.groupLimitBonuses()));
        return map;
    }

    private static List<String> entriesForGroup(Map<String, List<String>> entries, String groupId, boolean stripExactSuffix) {
        ArrayList<String> values = new ArrayList<String>();
        for (Map.Entry<String, List<String>> entry : entries.entrySet()) {
            if (entry.getValue().contains(groupId)) {
                values.add(stripExactSuffix ? stripModernExactKey(entry.getKey()) : entry.getKey());
            }
        }
        return values;
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

    private static String stringValue(Map<String, Object> source, String key, String fallback) {
        Object value = source.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof Iterable)) {
            return Collections.emptyList();
        }
        ArrayList<String> result = new ArrayList<String>();
        Iterator<?> iterator = ((Iterable<?>) value).iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next != null) {
                result.add(next.toString());
            }
        }
        return result;
    }

    private static String stripModernExactKey(String key) {
        return key != null && key.endsWith("@0") ? key.substring(0, key.length() - 2) : key;
    }
}
