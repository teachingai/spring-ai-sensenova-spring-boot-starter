package org.springframework.ai.sensetime.sensenova.metadata;

import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaImageApi;
import org.springframework.util.Assert;

import java.util.Objects;

public class SensetimeAiSensenovaImageResponseMetadata implements ImageResponseMetadata {

    private final Long created;

    public static SensetimeAiSensenovaImageResponseMetadata from(SensetimeAiSensenovaImageApi.ZhipuAiImageResponse ZhipuAiImageResponse) {
        Assert.notNull(ZhipuAiImageResponse, "ZhipuAiImageResponse must not be null");
        return new SensetimeAiSensenovaImageResponseMetadata(ZhipuAiImageResponse.created());
    }

    protected SensetimeAiSensenovaImageResponseMetadata(Long created) {
        this.created = created;
    }

    @Override
    public Long created() {
        return this.created;
    }

    @Override
    public String toString() {
        return "ZhipuAiImageResponseMetadata{" + "created=" + created + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SensetimeAiSensenovaImageResponseMetadata that))
            return false;
        return Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(created);
    }

}
