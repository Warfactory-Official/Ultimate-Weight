package com.warfactory.ultimateweight.compat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CompatibilityPatchLoader {
    private CompatibilityPatchLoader() {
    }

    public static <T> List<T> load(
        ModPresenceChecker modPresence,
        Collection<PatchSpec> patchSpecs,
        Class<T> patchType
    ) {
        if (modPresence == null || patchSpecs == null || patchSpecs.isEmpty() || patchType == null) {
            return Collections.emptyList();
        }

        ArrayList<T> loaded = new ArrayList<T>();
        for (PatchSpec spec : patchSpecs) {
            T patch = instantiatePatch(modPresence, spec, patchType);
            if (patch != null) {
                loaded.add(patch);
            }
        }
        return loaded;
    }

    private static <T> T instantiatePatch(
        ModPresenceChecker modPresence,
        PatchSpec spec,
        Class<T> patchType
    ) {
        if (spec == null || isBlank(spec.modId()) || isBlank(spec.implementationClassName())) {
            return null;
        }
        if (!modPresence.isModLoaded(spec.modId())) {
            return null;
        }

        try {
            Class<?> patchClass = Class.forName(spec.implementationClassName(), false, CompatibilityPatchLoader.class.getClassLoader());
            if (!patchType.isAssignableFrom(patchClass)) {
                return null;
            }
            Object instance = patchClass.getDeclaredConstructor().newInstance();
            return patchType.cast(instance);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class PatchSpec {
        private final String modId;
        private final String implementationClassName;

        public PatchSpec(String modId, String implementationClassName) {
            this.modId = modId;
            this.implementationClassName = implementationClassName;
        }

        public String modId() {
            return modId;
        }

        public String implementationClassName() {
            return implementationClassName;
        }
    }
}
