package com.warfactory.ultimateweight.compat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Reads the build-time compat plugin index that the annotation processor writes into each jar.
 * Every jar on the classpath may carry its own copy at {@link #RESOURCE_PATH}; this merges them all
 * so that, for example, the 1.20.1 forge jar contributes both the shared common plugins and its
 * forge-only ones.
 *
 * <p>Line format (one plugin per line, {@code #} comments and blank lines ignored):
 * <pre>fully.qualified.ClassName|mod1,mod2|anyA,anyB|priority|id</pre>
 * Trailing fields may be empty. The {@code requiredMods}/{@code anyOf} fields are comma-separated.
 */
public final class CompatPluginIndex {
    public static final String RESOURCE_PATH = "META-INF/wfweight/compat-plugins.txt";
    public static final char FIELD_SEPARATOR = '|';
    public static final char LIST_SEPARATOR = ',';

    private CompatPluginIndex() {
    }

    /**
     * Reads and merges every {@link #RESOURCE_PATH} resource visible to the class loader. Returns an
     * empty list (never null) if none are present or on read error.
     */
    public static List<DiscoveredPlugin> read(ClassLoader classLoader) {
        if (classLoader == null) {
            return Collections.emptyList();
        }
        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(RESOURCE_PATH);
        } catch (IOException ex) {
            return Collections.emptyList();
        }

        ArrayList<DiscoveredPlugin> plugins = new ArrayList<DiscoveredPlugin>();
        while (resources.hasMoreElements()) {
            readResource(resources.nextElement(), plugins);
        }
        return plugins;
    }

    private static void readResource(URL url, List<DiscoveredPlugin> out) {
        try (InputStream stream = url.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                DiscoveredPlugin plugin = parseLine(line);
                if (plugin != null) {
                    out.add(plugin);
                }
            }
        } catch (IOException ignored) {
            // A single unreadable index must not break discovery from the others.
        }
    }

    /** Parses one index line. Returns null for blank lines, comments, and malformed entries. */
    public static DiscoveredPlugin parseLine(String rawLine) {
        if (rawLine == null) {
            return null;
        }
        String line = rawLine.trim();
        if (line.isEmpty() || line.charAt(0) == '#') {
            return null;
        }

        String[] fields = splitFields(line);
        String className = fields[0].trim();
        if (className.isEmpty()) {
            return null;
        }
        String[] requiredMods = splitList(field(fields, 1));
        String[] anyOf = splitList(field(fields, 2));
        int priority = parseInt(field(fields, 3));
        String id = field(fields, 4).trim();
        return new DiscoveredPlugin(className, requiredMods, anyOf, priority, id);
    }

    private static String field(String[] fields, int index) {
        return index < fields.length ? fields[index] : "";
    }

    private static String[] splitFields(String line) {
        // -1 keeps trailing empties so positional fields stay aligned.
        return line.split("\\" + FIELD_SEPARATOR, -1);
    }

    private static String[] splitList(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }
        String[] parts = trimmed.split(String.valueOf(LIST_SEPARATOR));
        ArrayList<String> cleaned = new ArrayList<String>(parts.length);
        for (String part : parts) {
            String token = part.trim();
            if (!token.isEmpty()) {
                cleaned.add(token);
            }
        }
        return cleaned.toArray(new String[0]);
    }

    private static int parseInt(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
