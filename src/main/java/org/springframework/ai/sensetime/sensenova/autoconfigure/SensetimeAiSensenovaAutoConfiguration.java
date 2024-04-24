package org.springframework.ai.sensetime.sensenova.autoconfigure;

import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.sensetime.sensenova.SensetimeAiSensenovaChatClient;
import org.springframework.ai.sensetime.sensenova.SensetimeAiSensenovaEmbeddingClient;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Sensetime AI Sensenova Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ SensetimeAiSensenovaChatProperties.class, SensetimeAiSensenovaConnectionProperties.class, SensetimeAiSensenovaEmbeddingProperties.class })
@ConditionalOnClass(SensetimeAiSensenovaApi.class)
public class SensetimeAiSensenovaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SensetimeAiSensenovaApi sensetimeAiSensenovaApi(SensetimeAiSensenovaConnectionProperties properties, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

        Assert.hasText(properties.getApiKey(), "Sensetime AI Sensenova API key must be set");
        Assert.hasText(properties.getBaseUrl(), "Sensetime AI Sensenova base URL must be set");

        return new SensetimeAiSensenovaApi(properties.getBaseUrl(), properties.getApiKey(), restClientBuilder, responseErrorHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SensetimeAiSensenovaChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public SensetimeAiSensenovaChatClient sensetimeAiSensenovaChatClient(SensetimeAiSensenovaApi sensetimeAiSensenovaApi,
                                                            SensetimeAiSensenovaChatProperties chatProperties,
                                                            List<FunctionCallback> toolFunctionCallbacks,
                                                            FunctionCallbackContext functionCallbackContext,
                                                            RetryTemplate retryTemplate) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        return new SensetimeAiSensenovaChatClient(sensetimeAiSensenovaApi, chatProperties.getOptions(), functionCallbackContext, retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SensetimeAiSensenovaEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public SensetimeAiSensenovaEmbeddingClient sensetimeAiSensenovaEmbeddingClient(SensetimeAiSensenovaApi sensetimeAiSensenovaApi,
                                                                      SensetimeAiSensenovaEmbeddingProperties embeddingProperties,
                                                                      RetryTemplate retryTemplate) {

        return new SensetimeAiSensenovaEmbeddingClient(sensetimeAiSensenovaApi, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
