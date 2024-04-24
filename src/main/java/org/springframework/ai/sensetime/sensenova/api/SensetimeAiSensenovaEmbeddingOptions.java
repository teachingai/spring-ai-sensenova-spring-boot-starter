package org.springframework.ai.sensetime.sensenova.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.embedding.EmbeddingOptions;

import java.util.Map;

public class SensetimeAiSensenovaEmbeddingOptions implements EmbeddingOptions {

    /**
     * NOTE: Synthetic field not part of the official ZhiPuAi API.
     * Used to allow overriding the model name with prompt options.
     */
    @JsonProperty("model")
    private String model;

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected SensetimeAiSensenovaEmbeddingOptions options;

        public Builder() {
            this.options = new SensetimeAiSensenovaEmbeddingOptions();
        }

        public Builder withModel(String model) {
            this.options.setModel(model);
            return this;
        }

        public SensetimeAiSensenovaEmbeddingOptions build() {
            return this.options;
        }

    }

    /**
     * Convert the {@link SensetimeAiSensenovaEmbeddingOptions} object to a {@link Map} of key/value pairs.
     * @return The {@link Map} of key/value pairs.
     */
    public Map<String, Object> toMap() {
        try {
            var json = new ObjectMapper().writeValueAsString(this);
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
