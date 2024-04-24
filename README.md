# spring-ai-sensetime-sensenova-spring-boot-starter

 > 基于 [商汤日日新（SenseNova）](https://techday.sensetime.com/list) 系列大模型 和 Spring AI 的 Spring Boot Starter 实现

### 商汤日日新（SenseNova）

- 官网地址：[https://techday.sensetime.com](https://techday.sensetime.com)
- API文档：[https://platform.sensenova.cn/doc?path=/chat/GetStarted/APIList.md](https://platform.sensenova.cn/doc?path=/chat/GetStarted/APIList.md)
- 模型更新: [https://platform.sensenova.cn/release?path=/release-202404.md](https://platform.sensenova.cn/release?path=/release-202404.md)
- 体验中心: [https://platform.sensenova.cn/trialcenter](https://platform.sensenova.cn/trialcenter)

#### 支持的功能包括：

- 支持文本生成（Chat Completion API）
- 支持多轮对话（Chat Completion API），支持返回流式输出结果
- 支持函数调用（Function Calling）：用户传入各类自定义工具，自动选择并调用工具，准确度达到99%
- 支持文本嵌入（Embeddings）
- 支持图片生成（Image Generation API）
- 支持助手API（Assistants API）
- 支持 code interpreter功能：自动生成Python代码解决数学问题，降低直接数值计算错误，提升数学解题能力。在公开数学测评数据集上逼近GPT-4 Turbo的水平

#### 资源

- [查看模型列表](https://platform.sensenova.cn/doc?path=/chat/Models/GetModelList.md)
- [商量大语言模型-通用](https://platform.sensenova.cn/doc?path=/model/llm/GeneralLLM.md)
- [大语言模型](https://platform.sensenova.cn/doc?path=/chat/Introduction.md)

#### 模型

商汤日日新（SenseNova）开放平台提供了包括通用大模型、图像大模型、超拟人大模型、向量大模型等多种模型。

##### 日日新-商量大语言模型

| 模型 |  描述 |
| ------------ | ------------ |
| Embedding-2	  |  。 |


### Maven

``` xml
<dependency>
	<groupId>com.github.teachingai</groupId>
	<artifactId>spring-ai-sensetime-sensenova-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```

### Sample

使用示例请参见 [Spring AI Examples](https://github.com/TeachingAI/spring-ai-examples)

### License

[Apache License 2.0](LICENSE)
