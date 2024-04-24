package org.springframework.ai.sensetime.sensenova.autoconfigure;

import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaApi;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaChatOptions;
import org.springframework.ai.sensetime.sensenova.util.ApiUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(SensetimeAiSensenovaChatProperties.CONFIG_PREFIX)
public class SensetimeAiSensenovaChatProperties {

    public static final String CONFIG_PREFIX = "spring.ai.sensetimeai.sensenova.chat";


    /**
     * Enable Sensetime AI Sensenova chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever Sensetime AI Sensenova options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private SensetimeAiSensenovaChatOptions options = SensetimeAiSensenovaChatOptions.builder()
            .withModel(SensetimeAiSensenovaApi.ChatModel.SENSECHAT.getValue())
            .withMaxToken(ApiUtils.DEFAULT_MAX_TOKENS)
            .withDoSample(Boolean.TRUE)
            .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
            .withTopP(ApiUtils.DEFAULT_TOP_P)
            .build();

    public SensetimeAiSensenovaChatOptions getOptions() {
        return this.options;
    }

    public void setOptions(SensetimeAiSensenovaChatOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
