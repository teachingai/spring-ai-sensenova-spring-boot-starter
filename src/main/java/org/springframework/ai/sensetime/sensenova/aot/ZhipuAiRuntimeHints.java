package org.springframework.ai.sensetime.sensenova.aot;

import org.springframework.ai.sensetime.sensenova.api.ZhipuAiApi;
import org.springframework.ai.sensetime.sensenova.api.ZhipuAiChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class ZhipuAiRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(ZhipuAiApi.class)) {
            hints.reflection().registerType(tr, mcs);
        }
        for (var tr : findJsonAnnotatedClassesInPackage(ZhipuAiChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
