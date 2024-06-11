package org.springframework.ai.sensetime.sensenova;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaApi;
import org.springframework.ai.sensetime.sensenova.api.SensetimeAiSensenovaChatOptions;
import org.springframework.ai.sensetime.sensenova.util.ApiUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SensetimeAiSensenovaChatClient
        extends AbstractFunctionCallSupport<SensetimeAiSensenovaApi.ChatCompletionMessage, SensetimeAiSensenovaApi.ChatCompletionRequest, ResponseEntity<SensetimeAiSensenovaApi.ChatCompletion>>
        implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private SensetimeAiSensenovaChatOptions defaultOptions;
    /**
     * Low-level 智普 API library.
     */
    private final SensetimeAiSensenovaApi sensetimeAiSensenovaApi;
    private final RetryTemplate retryTemplate;

    public SensetimeAiSensenovaChatClient(SensetimeAiSensenovaApi sensetimeAiSensenovaApi) {
        this(sensetimeAiSensenovaApi, SensetimeAiSensenovaChatOptions.builder()
                        .withModel(SensetimeAiSensenovaApi.ChatModel.SENSECHAT.getValue())
                        .withMaxToken(ApiUtils.DEFAULT_MAX_TOKENS)
                        .withDoSample(Boolean.TRUE)
                        .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                        .withTopP(ApiUtils.DEFAULT_TOP_P)
                        .build());
    }

    public SensetimeAiSensenovaChatClient(SensetimeAiSensenovaApi sensetimeAiSensenovaApi, SensetimeAiSensenovaChatOptions options) {
        this(sensetimeAiSensenovaApi, options, null, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public SensetimeAiSensenovaChatClient(SensetimeAiSensenovaApi sensetimeAiSensenovaApi, SensetimeAiSensenovaChatOptions options,
                                          FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate) {
        super(functionCallbackContext);
        Assert.notNull(sensetimeAiSensenovaApi, "ZhipuAiApi must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.sensetimeAiSensenovaApi = sensetimeAiSensenovaApi;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }


    @Override
    public ChatResponse call(Prompt prompt) {

        var request = createRequest(prompt, false);

        return retryTemplate.execute(ctx -> {

            ResponseEntity<SensetimeAiSensenovaApi.ChatCompletion> completionEntity = this.callWithFunctionSupport(request);

            var chatCompletion = completionEntity.getBody();
            if (chatCompletion == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }

            List<Generation> generations = chatCompletion.choices()
                    .stream()
                    .map(choice -> new Generation(choice.message().content(), toMap(chatCompletion.id(), choice))
                            .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null)))
                    .toList();

            return new ChatResponse(generations);
        });
    }

    private Map<String, Object> toMap(String id, SensetimeAiSensenovaApi.ChatCompletion.Choice choice) {
        Map<String, Object> map = new HashMap<>();

        var message = choice.message();
        if (message.role() != null) {
            map.put("role", message.role().name());
        }
        if (choice.finishReason() != null) {
            map.put("finishReason", choice.finishReason().name());
        }
        map.put("id", id);
        return map;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        var request = createRequest(prompt, true);

        return retryTemplate.execute(ctx -> {

            var completionChunks = this.sensetimeAiSensenovaApi.chatCompletionStream(request);

            // For chunked responses, only the first chunk contains the choice role.
            // The rest of the chunks with same ID share the same role.
            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            return completionChunks.map(chunk -> toChatCompletion(chunk)).map(chatCompletion -> {

                chatCompletion = handleFunctionCallOrReturn(request, ResponseEntity.of(Optional.of(chatCompletion)))
                        .getBody();

                @SuppressWarnings("null")
                String id = chatCompletion.id();

                List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                    if (choice.message().role() != null) {
                        roleMap.putIfAbsent(id, choice.message().role().name());
                    }
                    String finish = (choice.finishReason() != null ? choice.finishReason().name() : "");
                    var generation = new Generation(choice.message().content(),
                            Map.of("id", id, "role", roleMap.get(id), "finishReason", finish));
                    if (choice.finishReason() != null) {
                        generation = generation
                                .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
                    }
                    return generation;
                }).toList();
                return new ChatResponse(generations);
            });
        });
    }

    private SensetimeAiSensenovaApi.ChatCompletion toChatCompletion(SensetimeAiSensenovaApi.ChatCompletionChunk chunk) {
        List<SensetimeAiSensenovaApi.ChatCompletion.Choice> choices = chunk.choices()
                .stream()
                .map(cc -> new SensetimeAiSensenovaApi.ChatCompletion.Choice(cc.index(), cc.delta(), cc.finishReason()))
                .toList();

        return new SensetimeAiSensenovaApi.ChatCompletion(chunk.id(), "chat.completion", chunk.created(), chunk.model(), choices, chunk.requestId(),null);
    }

    /**
     * Accessible for testing.
     */
    SensetimeAiSensenovaApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

        Set<String> functionsForThisRequest = new HashSet<>();

        var chatCompletionMessages = prompt.getInstructions()
                .stream()
                .map(m -> new SensetimeAiSensenovaApi.ChatCompletionMessage(m.getContent(),
                        SensetimeAiSensenovaApi.ChatCompletionMessage.Role.valueOf(m.getMessageType().name())))
                .toList();

        var request = new SensetimeAiSensenovaApi.ChatCompletionRequest(null, chatCompletionMessages, stream);

        if (this.defaultOptions != null) {
            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);

            request = ModelOptionsUtils.merge(request, this.defaultOptions, SensetimeAiSensenovaApi.ChatCompletionRequest.class);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        SensetimeAiSensenovaChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                        IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request,
                        SensetimeAiSensenovaApi.ChatCompletionRequest.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge(
                    SensetimeAiSensenovaChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, SensetimeAiSensenovaApi.ChatCompletionRequest.class);
        }

        return request;
    }

    private List<SensetimeAiSensenovaApi.FunctionTool> getFunctionTools(Set<String> functionNames) {
        return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = new SensetimeAiSensenovaApi.FunctionTool.Function(functionCallback.getDescription(),
                    functionCallback.getName(), functionCallback.getInputTypeSchema());
            return new SensetimeAiSensenovaApi.FunctionTool(function);
        }).toList();
    }

    //
    // Function Calling Support
    //
    @Override
    protected SensetimeAiSensenovaApi.ChatCompletionRequest doCreateToolResponseRequest(SensetimeAiSensenovaApi.ChatCompletionRequest previousRequest,
                                                                                        SensetimeAiSensenovaApi.ChatCompletionMessage responseMessage,
                                                                                        List<SensetimeAiSensenovaApi.ChatCompletionMessage> conversationHistory) {

        // Every tool-call item requires a separate function call and a response (TOOL)
        // message.
        for (SensetimeAiSensenovaApi.ChatCompletionMessage.ToolCall toolCall : responseMessage.toolCalls()) {

            var functionName = toolCall.function().name();
            String functionArguments = toolCall.function().arguments();

            if (!this.functionCallbackRegister.containsKey(functionName)) {
                throw new IllegalStateException("No function callback found for function name: " + functionName);
            }

            String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

            // Add the function response to the conversation.
            conversationHistory
                    .add(new SensetimeAiSensenovaApi.ChatCompletionMessage(functionResponse, SensetimeAiSensenovaApi.ChatCompletionMessage.Role.TOOL, functionName, null));
        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        SensetimeAiSensenovaApi.ChatCompletionRequest newRequest = new SensetimeAiSensenovaApi.ChatCompletionRequest(previousRequest.requestId(), conversationHistory, false);
        newRequest = ModelOptionsUtils.merge(newRequest, previousRequest, SensetimeAiSensenovaApi.ChatCompletionRequest.class);

        return newRequest;
    }

    @Override
    protected List<SensetimeAiSensenovaApi.ChatCompletionMessage> doGetUserMessages(SensetimeAiSensenovaApi.ChatCompletionRequest request) {
        return request.messages();
    }

    @SuppressWarnings("null")
    @Override
    protected SensetimeAiSensenovaApi.ChatCompletionMessage doGetToolResponseMessage(ResponseEntity<SensetimeAiSensenovaApi.ChatCompletion> chatCompletion) {
        return chatCompletion.getBody().choices().iterator().next().message();
    }

    @Override
    protected ResponseEntity<SensetimeAiSensenovaApi.ChatCompletion> doChatCompletion(SensetimeAiSensenovaApi.ChatCompletionRequest request) {
        return this.sensetimeAiSensenovaApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<SensetimeAiSensenovaApi.ChatCompletion> chatCompletion) {

        var body = chatCompletion.getBody();
        if (body == null) {
            return false;
        }

        var choices = body.choices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        return !CollectionUtils.isEmpty(choices.get(0).message().toolCalls());
    }
}
