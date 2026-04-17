package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.compat.CompatibilityPatchLoader;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CompatibilityNestedWeightProvider1122 implements WeightViews1122.NestedWeightProvider {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);
    private static final double EPSILON = 0.000001D;
    private static final int CACHE_LIMIT = 512;

    private static final List<CompatibilityPatchLoader.PatchSpec> PATCH_SPECS = Arrays.asList(
        new CompatibilityPatchLoader.PatchSpec(
            "gregtech",
            "com.warfactory.ultimateweight.v1122.compat.GregTechNestedWeightPatch1122"
        ),
        new CompatibilityPatchLoader.PatchSpec(
            "storagedrawers",
            "com.warfactory.ultimateweight.v1122.compat.StorageDrawersNestedWeightPatch1122"
        )
    );

    private final List<WeightViews1122.NestedWeightProvider> providers;
    private final Map<CacheKey, Double> cache = new LinkedHashMap<CacheKey, Double>(CACHE_LIMIT, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<CacheKey, Double> eldest) {
            return size() > CACHE_LIMIT;
        }
    };

    private CompatibilityNestedWeightProvider1122(List<WeightViews1122.NestedWeightProvider> providers) {
        this.providers = providers;
    }

    public static CompatibilityNestedWeightProvider1122 create() {
        ArrayList<WeightViews1122.NestedWeightProvider> providers = new ArrayList<WeightViews1122.NestedWeightProvider>();
        providers.add(new GenericNestedWeightPatch1122());
        providers.addAll(
            CompatibilityPatchLoader.load(
                new ModPresenceChecker() {
                    @Override
                    public boolean isModLoaded(String modId) {
                        return Loader.isModLoaded(modId);
                    }
                },
                PATCH_SPECS,
                WeightViews1122.NestedWeightProvider.class
            )
        );

        if (UltimateWeightCommon.isDebugEnabled()) {
            LOGGER.info(
                "1.12.2 compatibility nested providers active: {}",
                Integer.valueOf(providers.size())
            );
        }
        return new CompatibilityNestedWeightProvider1122(providers);
    }

    @Override
    public double additionalWeight(ItemStack stack, int depth) {
        if (stack.isEmpty()) {
            return 0.0D;
        }

        CacheKey cacheKey = CacheKey.of(stack, depth);
        synchronized (cache) {
            Double cached = cache.get(cacheKey);
            if (cached != null) {
                return cached.doubleValue();
            }
        }

        double total = 0.0D;
        for (WeightViews1122.NestedWeightProvider provider : providers) {
            try {
                double additional = provider.additionalWeight(stack, depth);
                if (additional > EPSILON) {
                    total += additional;
                }
            } catch (Throwable throwable) {
                if (UltimateWeightCommon.isDebugEnabled()) {
                    LOGGER.warn(
                        "1.12.2 nested provider {} failed for stack {}.",
                        provider.getClass().getName(),
                        stack.isEmpty() || stack.getItem().getRegistryName() == null
                            ? "empty"
                            : stack.getItem().getRegistryName().toString(),
                        throwable
                    );
                }
            }
        }

        synchronized (cache) {
            cache.put(cacheKey, Double.valueOf(total));
        }
        return total;
    }

    private static final class CacheKey {
        private final String itemId;
        private final int meta;
        private final int count;
        private final int depth;
        private final int tagHash;

        private CacheKey(String itemId, int meta, int count, int depth, int tagHash) {
            this.itemId = itemId;
            this.meta = meta;
            this.count = count;
            this.depth = depth;
            this.tagHash = tagHash;
        }

        private static CacheKey of(ItemStack stack, int depth) {
            NBTTagCompound tag = stack.getTagCompound();
            return new CacheKey(
                stack.getItem().getRegistryName() == null
                    ? "minecraft:air"
                    : stack.getItem().getRegistryName().toString(),
                stack.getMetadata(),
                stack.getCount(),
                depth,
                tag == null ? 0 : tag.hashCode()
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return meta == other.meta
                && count == other.count
                && depth == other.depth
                && tagHash == other.tagHash
                && itemId.equals(other.itemId);
        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + meta;
            result = 31 * result + count;
            result = 31 * result + depth;
            result = 31 * result + tagHash;
            return result;
        }
    }
}
