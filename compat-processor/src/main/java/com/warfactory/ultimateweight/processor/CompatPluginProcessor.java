package com.warfactory.ultimateweight.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compile-time discovery for {@code @CompatPlugin}. Records every annotated class - plus its
 * required-mod gating - into {@code META-INF/wfweight/compat-plugins.txt} so the runtime bootstrap
 * can read it without scanning the classpath or loading any plugin classes.
 *
 * <p>The annotation type is referenced by name only, so this processor has no dependency on the
 * mod's shared module. Output format must stay in sync with {@code CompatPluginIndex} on the
 * runtime side: {@code fqcn|requiredMods,csv|anyOf,csv|priority|id}.
 */
@SupportedAnnotationTypes(CompatPluginProcessor.ANNOTATION_FQN)
public final class CompatPluginProcessor extends AbstractProcessor {
    static final String ANNOTATION_FQN = "com.warfactory.ultimateweight.api.CompatPlugin";
    private static final String RESOURCE_PATH = "META-INF/wfweight/compat-plugins.txt";

    // Keyed by binary class name so re-processing across rounds never duplicates an entry.
    private final Map<String, String> lines = new LinkedHashMap<String, String>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // Adapt to whichever javac runs us (8 for the 1.12.2 build, newer elsewhere).
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            if (!ANNOTATION_FQN.contentEquals(annotation.getQualifiedName())) {
                continue;
            }
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement) {
                    record((TypeElement) element);
                }
            }
        }

        if (roundEnv.processingOver()) {
            writeIndex();
        }
        return false;
    }

    private void record(TypeElement type) {
        AnnotationMirror mirror = findMirror(type);
        if (mirror == null) {
            return;
        }
        String binaryName = processingEnv.getElementUtils().getBinaryName(type).toString();
        Map<? extends ExecutableElement, ? extends AnnotationValue> values =
            processingEnv.getElementUtils().getElementValuesWithDefaults(mirror);

        List<String> requiredMods = new ArrayList<String>();
        List<String> anyOf = new ArrayList<String>();
        int priority = 0;
        String id = "";
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            if ("requiredMods".equals(key)) {
                requiredMods = asStringList(value);
            } else if ("anyOf".equals(key)) {
                anyOf = asStringList(value);
            } else if ("priority".equals(key)) {
                priority = value instanceof Integer ? (Integer) value : 0;
            } else if ("id".equals(key)) {
                id = String.valueOf(value);
            }
        }

        String line = binaryName + '|' + join(requiredMods) + '|' + join(anyOf) + '|' + priority + '|' + id;
        lines.put(binaryName, line);
    }

    private AnnotationMirror findMirror(TypeElement type) {
        for (AnnotationMirror mirror : type.getAnnotationMirrors()) {
            Element annotationElement = mirror.getAnnotationType().asElement();
            if (annotationElement instanceof TypeElement
                && ANNOTATION_FQN.contentEquals(((TypeElement) annotationElement).getQualifiedName())) {
                return mirror;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object value) {
        List<String> result = new ArrayList<String>();
        if (value instanceof List) {
            for (AnnotationValue element : (List<? extends AnnotationValue>) value) {
                result.add(String.valueOf(element.getValue()));
            }
        }
        return result;
    }

    private static String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private void writeIndex() {
        if (lines.isEmpty()) {
            return;
        }
        Filer filer = processingEnv.getFiler();
        try {
            FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", RESOURCE_PATH);
            try (Writer writer = resource.openWriter()) {
                writer.write("# Generated by CompatPluginProcessor - do not edit.\n");
                for (String line : lines.values()) {
                    writer.write(line);
                    writer.write('\n');
                }
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "Failed to write " + RESOURCE_PATH + ": " + ex.getMessage());
        }
    }
}
