package org.springframework.ai.sensetime.sensenova.aot;

import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaApi;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class SensetimeAiSensenovaRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(SensetimeAiSensenovaApi.class)) {
            hints.reflection().registerType(tr, mcs);
        }
        for (var tr : findJsonAnnotatedClassesInPackage(SensetimeAiSensenovaChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
