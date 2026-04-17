package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.UltimateWeightCommon;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.IEarlyMixinLoader;
import java.util.Collections;

@IFMLLoadingPlugin.Name("wfweight")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("com.warfactory.ultimateweight.coremod")
public final class UltimateWeightLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    public UltimateWeightLoadingPlugin() {
        if (UltimateWeightCommon.isDebugEnabled()) {
            LOGGER.info("wfweight debug enabled. Initialized 1.12.2 mixin bootstrap.");
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("wfweight.mixins.json");
    }
}
