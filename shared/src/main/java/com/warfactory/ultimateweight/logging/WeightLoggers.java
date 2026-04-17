package com.warfactory.ultimateweight.logging;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class WeightLoggers {
    private static final ConcurrentMap<String, WeightLogger> CACHE = new ConcurrentHashMap<String, WeightLogger>();

    private WeightLoggers() {
    }

    public static WeightLogger core() {
        return component("core");
    }

    public static WeightLogger component(String component) {
        String suffix = component == null ? "unknown" : component.trim().toLowerCase();
        if (suffix.isEmpty()) {
            suffix = "unknown";
        }
        String name = UltimateWeightCommon.MOD_ID + "." + suffix;
        return CACHE.computeIfAbsent(name, WeightLogger::new);
    }

    public static final class WeightLogger {
        private final String name;

        private WeightLogger(String name) {
            this.name = name;
        }

        public void debug(String message, Object... args) {
            if (!UltimateWeightCommon.isDebugEnabled()) {
                return;
            }
            log("DEBUG", message, args);
        }

        public void info(String message, Object... args) {
            log("INFO", message, args);
        }

        public void warn(String message, Object... args) {
            log("WARN", message, args);
        }

        private void log(String level, String message, Object... args) {
            String rendered = render(message, args);
            System.out.println("[" + level + "][" + name + "] " + rendered);
        }

        private static String render(String message, Object... args) {
            if (message == null) {
                return "";
            }
            if (args == null || args.length == 0) {
                return message;
            }
            String rendered = message;
            for (Object arg : args) {
                rendered = rendered.replaceFirst("\\{\\}", java.util.regex.Matcher.quoteReplacement(String.valueOf(arg)));
            }
            return rendered;
        }
    }
}
