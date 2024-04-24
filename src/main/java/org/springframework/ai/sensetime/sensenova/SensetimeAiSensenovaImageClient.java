package org.springframework.ai.sensetime.sensenova;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaImageApi;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaImageOptions;
import org.springframework.ai.sensetime.sensenova.metadata.SensetimeAiSensenovaImageGenerationMetadata;
import org.springframework.ai.sensetime.sensenova.metadata.SensetimeAiSensenovaImageResponseMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;

public class SensetimeAiSensenovaImageClient implements ImageClient {

    private final static Logger logger = LoggerFactory.getLogger(SensetimeAiSensenovaImageClient.class);
    private final static SensetimeAiSensenovaImageGenerationMetadata DEFAULT_METADATA =  new SensetimeAiSensenovaImageGenerationMetadata("");

    private SensetimeAiSensenovaImageOptions defaultOptions;

    private final SensetimeAiSensenovaImageApi sensetimeAiSensenovaImageApi;

    public final RetryTemplate retryTemplate;

    public SensetimeAiSensenovaImageClient(SensetimeAiSensenovaImageApi sensetimeAiSensenovaImageApi) {
        this(sensetimeAiSensenovaImageApi, SensetimeAiSensenovaImageOptions.builder()
                .withModel(SensetimeAiSensenovaImageApi.DEFAULT_IMAGE_MODEL)
                .build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public SensetimeAiSensenovaImageClient(SensetimeAiSensenovaImageApi sensetimeAiSensenovaImageApi, SensetimeAiSensenovaImageOptions defaultOptions,
                                           RetryTemplate retryTemplate) {
        Assert.notNull(sensetimeAiSensenovaImageApi, "ZhipuAiImageApi must not be null");
        Assert.notNull(defaultOptions, "defaultOptions must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.sensetimeAiSensenovaImageApi = sensetimeAiSensenovaImageApi;
        this.defaultOptions = defaultOptions;
        this.retryTemplate = retryTemplate;
    }

    public SensetimeAiSensenovaImageOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    @Override
    public ImageResponse call(ImagePrompt imagePrompt) {
        return this.retryTemplate.execute(ctx -> {

            String instructions = imagePrompt.getInstructions().get(0).getText();
            SensetimeAiSensenovaImageApi.ZhipuAiImageRequest imageRequest = new SensetimeAiSensenovaImageApi.ZhipuAiImageRequest(instructions,
                    SensetimeAiSensenovaImageApi.DEFAULT_IMAGE_MODEL);

            if (this.defaultOptions != null) {
                imageRequest = ModelOptionsUtils.merge(this.defaultOptions, imageRequest,
                        SensetimeAiSensenovaImageApi.ZhipuAiImageRequest.class);
            }

            if (imagePrompt.getOptions() != null) {
                imageRequest = ModelOptionsUtils.merge(toZhipuAiImageOptions(imagePrompt.getOptions()), imageRequest,
                        SensetimeAiSensenovaImageApi.ZhipuAiImageRequest.class);
            }

            // Make the request
            ResponseEntity<SensetimeAiSensenovaImageApi.ZhipuAiImageResponse> imageResponseEntity = this.sensetimeAiSensenovaImageApi
                    .createImage(imageRequest);

            // Convert to org.springframework.ai.model derived ImageResponse data type
            return convertResponse(imageResponseEntity, imageRequest);
        });
    }

    private ImageResponse convertResponse(ResponseEntity<SensetimeAiSensenovaImageApi.ZhipuAiImageResponse> imageResponseEntity,
                                          SensetimeAiSensenovaImageApi.ZhipuAiImageRequest ZhipuAiImageRequest) {
        SensetimeAiSensenovaImageApi.ZhipuAiImageResponse imageApiResponse = imageResponseEntity.getBody();
        if (imageApiResponse == null) {
            logger.warn("No image response returned for request: {}", ZhipuAiImageRequest);
            return new ImageResponse(List.of());
        }

        List<ImageGeneration> imageGenerationList = imageApiResponse.data().stream().map(entry -> {
            return new ImageGeneration(new Image(entry.url(), null), DEFAULT_METADATA);
        }).toList();

        ImageResponseMetadata imageResponseMetadata = SensetimeAiSensenovaImageResponseMetadata.from(imageApiResponse);
        return new ImageResponse(imageGenerationList, imageResponseMetadata);
    }

    /**
     * Convert the {@link ImageOptions} into {@link SensetimeAiSensenovaImageOptions}.
     * @param runtimeImageOptions the image options to use.
     * @return the converted {@link SensetimeAiSensenovaImageOptions}.
     */
    private SensetimeAiSensenovaImageOptions toZhipuAiImageOptions(ImageOptions runtimeImageOptions) {
        SensetimeAiSensenovaImageOptions.Builder ZhipuAiImageOptionsBuilder = SensetimeAiSensenovaImageOptions.builder();
        if (runtimeImageOptions != null) {
            // Handle portable image options
            if (runtimeImageOptions.getN() != null) {
                ZhipuAiImageOptionsBuilder.withN(runtimeImageOptions.getN());
            }
            if (runtimeImageOptions.getModel() != null) {
                ZhipuAiImageOptionsBuilder.withModel(runtimeImageOptions.getModel());
            }
            if (runtimeImageOptions.getResponseFormat() != null) {
                ZhipuAiImageOptionsBuilder.withResponseFormat(runtimeImageOptions.getResponseFormat());
            }
            if (runtimeImageOptions.getWidth() != null) {
                ZhipuAiImageOptionsBuilder.withWidth(runtimeImageOptions.getWidth());
            }
            if (runtimeImageOptions.getHeight() != null) {
                ZhipuAiImageOptionsBuilder.withHeight(runtimeImageOptions.getHeight());
            }
            // Handle Sensetime AI Sensenova specific image options
            if (runtimeImageOptions instanceof SensetimeAiSensenovaImageOptions) {
                SensetimeAiSensenovaImageOptions runtimeSensetimeAiSensenovaImageOptions = (SensetimeAiSensenovaImageOptions) runtimeImageOptions;
                if (runtimeSensetimeAiSensenovaImageOptions.getQuality() != null) {
                    ZhipuAiImageOptionsBuilder.withQuality(runtimeSensetimeAiSensenovaImageOptions.getQuality());
                }
                if (runtimeSensetimeAiSensenovaImageOptions.getStyle() != null) {
                    ZhipuAiImageOptionsBuilder.withStyle(runtimeSensetimeAiSensenovaImageOptions.getStyle());
                }
                if (runtimeSensetimeAiSensenovaImageOptions.getUser() != null) {
                    ZhipuAiImageOptionsBuilder.withUser(runtimeSensetimeAiSensenovaImageOptions.getUser());
                }
            }
        }
        return ZhipuAiImageOptionsBuilder.build();
    }

}
