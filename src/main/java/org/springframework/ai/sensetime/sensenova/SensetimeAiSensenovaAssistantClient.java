package org.springframework.ai.sensetime.sensenova;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaFileApi;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * Sensetime AI Sensenova Assistant Client.
 * https://platform.sensenova.cn/doc?path=/assistants/GetStarted/APIList.md
 */
public class SensetimeAiSensenovaAssistantClient {

    private final static Logger logger = LoggerFactory.getLogger(SensetimeAiSensenovaAssistantClient.class);

    private final SensetimeAiSensenovaFileApi sensetimeAiSensenovaFileApi;

    public final RetryTemplate retryTemplate;

    public SensetimeAiSensenovaAssistantClient(SensetimeAiSensenovaFileApi sensetimeAiSensenovaFileApi, RetryTemplate retryTemplate) {
        Assert.notNull(sensetimeAiSensenovaFileApi, "ZhipuAiFileApi must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.sensetimeAiSensenovaFileApi = sensetimeAiSensenovaFileApi;
        this.retryTemplate = retryTemplate;
    }

    public ResponseEntity<SensetimeAiSensenovaFileApi.ZhipuAiFileResponse> listFile() {
        return retryTemplate.execute(context -> {
            logger.debug("Listing files");
            return sensetimeAiSensenovaFileApi.listFile();
        });
    }

    public ResponseEntity<SensetimeAiSensenovaFileApi.ZhipuAiFileResponse.Data> uploadFile(SensetimeAiSensenovaFileApi.ZhipuAiFileRequest request) throws IOException {
        return retryTemplate.execute(context -> {
            logger.debug("Uploading file");
            return sensetimeAiSensenovaFileApi.uploadFile(request);
        });
    }

}
