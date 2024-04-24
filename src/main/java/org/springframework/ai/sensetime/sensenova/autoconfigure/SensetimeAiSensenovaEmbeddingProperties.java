package org.springframework.ai.sensetime.sensenova.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaApi;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(SensetimeAiSensenovaEmbeddingProperties.CONFIG_PREFIX)
public class SensetimeAiSensenovaEmbeddingProperties {

    public static final String CONFIG_PREFIX = "spring.ai.sensetimeai.sensenova.embedding";

    public static final String DEFAULT_EMBEDDING_MODEL = SensetimeAiSensenovaApi.EmbeddingModel.EMBED.getValue();

    /**
     * Enable Sensetime AI Sensenova embedding client.
     */
    private boolean enabled = true;

    public MetadataMode metadataMode = MetadataMode.EMBED;

    /**
     * Client lever Sensetime AI Sensenova options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private SensetimeAiSensenovaEmbeddingOptions options = SensetimeAiSensenovaEmbeddingOptions.builder()
            .withModel(DEFAULT_EMBEDDING_MODEL)
            .build();

    public SensetimeAiSensenovaEmbeddingOptions getOptions() {
        return this.options;
    }

    public void setOptions(SensetimeAiSensenovaEmbeddingOptions options) {
        this.options = options;
    }

    public MetadataMode getMetadataMode() {
        return this.metadataMode;
    }

    public void setMetadataMode(MetadataMode metadataMode) {
        this.metadataMode = metadataMode;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
