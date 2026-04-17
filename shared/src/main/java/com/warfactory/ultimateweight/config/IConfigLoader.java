package com.warfactory.ultimateweight.config;

import java.io.IOException;
import java.io.InputStream;

public interface IConfigLoader {
    String bundledResource();

    WeightConfig loadBundled();

    WeightConfig load(String text);

    WeightConfig load(InputStream stream) throws IOException;

    String write(WeightConfig config);
}
