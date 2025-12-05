# Gemini in Java with Google GenAI SDK and Spring AI
Gemini workshop for Java developers, using the Spring AI orchestration framework

> [!NOTE]
> 
> These snippets are geared towards Java developers, 
> allowing them to discover capabilities and patterns supported by [Gemini](https://deepmind.google/technologies/gemini/)
> using the [Spring AI](https://docs.spring.io/spring-ai/reference/index.html)
> framework.

## Library support
* Released
  * Spring AI 1.1.1
  * Google GenAI SDK 1.28.0
* Milestone
  * upcoming Spring AI 2.0.0

## Prerequisites

The code snippets have been tested on the following environment:
* Java 21/25
* Maven >= 3.9.6

In order to run these code snippets, you need to have a Google Cloud account and project ready.

Before running the examples, you'll need to set up environment variables, for either Google credentials or [or an API Key from Google AI Studio](https://aistudio.google.com/welcome):

```bash
Credentials: 
  Google creds set:
    export GOOGLE_CLOUD_PROJECT=<your-project-id>
    export GOOGLE_CLOUD_LOCATION=<your region>, ex: us-central1
    export USE_VERTEX_AI=true

  Google API Key:
    export GOOGLE_API_KEY=...
    export USE_VERTEX_AI=false

Model:     
  export GEMINI_MODEL=<model>, ex: gemini-3-pro-preview, gemini-2.5-flash
```

> [!IMPORTANT]
> Please update the project ID and location to match your project and select the model of your choice

Create the Maven wrapper:

```bash
mvn wrapper:wrapper
```

## Complete list
The snippets in this workshop are grouped by various capabilities and patterns. You will find, in order:
* Chat
  * Simple Q&A with Gemini
  * Conversation with Gemini with chat history
  * Simple Q&A via streaming
* Multimodality
  * Analyzing & extracting image data using Multimodality
  * Transcribing audio data using Multimodality
  * Transcribing video data using Multimodality
* Capabilities
  * Structure prompts with prompt templates
  * Extracting structured data from unstructured text
  * Grounding responses with Web Search
  * Function Calling with Spring AI (multiple functions, JSON Schema, Async, Streaming)
* Document utilities
  * Document Readers and Splitters
* Embeddings
  * Generating Text Embeddings with Vertex AI
  * Generating Multimodal Embeddings with Vertex AI
* Token Management
  * Compute Tokens
  * Count Tokens
  * Count Tokens with Configs
* File Search Store
  * File Search Store (Sync)
  * File Search Store (Async)
* AI use-cases and patterns
  * Retrieval-augmented generation(RAG)
  * Text classification with Few-shot prompting
  * Sentiment analysis with few-shot prompting 
  * Summarization Patterns with Gemini: Stuffing, Map-Reduce Patterns
* Local environments
  *  Running Open-models with Ollama and Testcontainers

### Build
> [!TIP]
> Note the profile `complete` used for the build

Build the samples in a single JAR, then run them individually for the respective use-case:
```shell
./mvnw clean package -Pcomplete
```
### Run
> [!TIP]
> List of samples, by use-case. Each sample can be run independently

* Chat
  * [Simple Q&A with Gemini](src/main/java/gemini/workshop/SimpleChatExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.SimpleChatExample
      ```

  * [Conversation with Gemini with chat history](src/main/java/gemini/workshop/ConversationExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.ConversationExample
      ```

  * [Simple Q&A via streaming](src/main/java/gemini/workshop/SimpleChatStreamingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.SimpleChatStreamingExample
      ```

* Multimodality
  * [Analyzing & extracting image data using Multimodality](src/main/java/gemini/workshop/MultimodalImagesExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.MultimodalImagesExample
      ```

  * [Transcribing audio data using Multimodality](src/main/java/gemini/workshop/MultimodalAudioExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.MultimodalAudioExample
      ```

  * [Transcribing video data using Multimodality](src/main/java/gemini/workshop/MultimodalVideoExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.MultimodalVideoExample
      ```

* Capabilities
  * [Structure prompts with prompt templates](src/main/java/gemini/workshop/WorkingWithTemplatesExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.WorkingWithTemplatesExample
      ```

  * [Extracting structured data from unstructured text](src/main/java/gemini/workshop/StructuredOutputExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.StructuredOutputExample
      ```
  * [Grounding responses with Web Search with GenAI SDK SDK](src/main/java/gemini/workshop/GroundingWithWebsearchExample.java)
    ```shell
    java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.GroundingWithWebsearchExample
    ```

  * [Grounding responses with Web Search with SpringAI](src/main/java/gemini/workshop/GroundingWithWebsearchSpringAIExample.java)
    ```shell
    java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.GroundingWithWebsearchSpringAIExample
    ```
    
  * [Function Calling with Spring AI](src/main/java/gemini/workshop/FunctionCallingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.FunctionCallingExample
      ```

  * [Function Calling with Spring AI (Streaming)](src/main/java/gemini/workshop/FunctionCallingStreamingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.FunctionCallingStreamingExample
      ```

  * [Function Calling with Spring AI (Multiple Functions)](src/main/java/gemini/workshop/FunctionCallingMultipleExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.FunctionCallingMultipleExample
      ```

  * [Function Calling with Spring AI (Async)](src/main/java/gemini/workshop/FunctionCallingAsyncExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.FunctionCallingAsyncExample
      ```

  * [Function Calling with Spring AI (JSON Schema)](src/main/java/gemini/workshop/FunctionCallingJsonSchemaExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.FunctionCallingJsonSchemaExample
      ```

  * [File Search Store (Sync)](src/main/java/gemini/workshop/FileSearchStoreExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.FileSearchStoreExample
      ```

  * [File Search Store (Async)](src/main/java/gemini/workshop/FileSearchStoreAsyncExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.FileSearchStoreAsyncExample
      ```
* Document utilities
  * [Document Readers and Splitters](src/main/java/gemini/workshop/DocumentProcessingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.DocumentProcessingExample
      ```

* Embeddings
  * [Generating Text Embeddings with GenAI SDK](src/main/java/gemini/workshop/TextEmbeddingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.TextEmbeddingExample
      ```

  * [Generating Multimodal Embeddings with GenAI SDK](src/main/java/gemini/workshop/MultimodalEmbeddingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.MultimodalEmbeddingExample
      ```

* Token Management
  * [Compute Tokens](src/main/java/gemini/workshop/ComputeTokensExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.ComputeTokensExample
      ```

  * [Count Tokens](src/main/java/gemini/workshop/CountTokensExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.CountTokensExample
      ```

  * [Count Tokens with Configs](src/main/java/gemini/workshop/CountTokensWithConfigsExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.CountTokensWithConfigsExample
      ```

* AI use-cases and patterns
  * [Retrieval-augmented generation(RAG)](src/main/java/gemini/workshop/RAGExample.java)
    ```shell
    java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.RAGExample
    ```
     
  * [Text classification with Few-shot prompting](src/main/java/gemini/workshop/TextClassificationExample.java)
    ```shell
    java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.TextClassificationExample
    ```

  * [Sentiment analysis with Few-shot prompting](src/main/java/gemini/workshop/SentimentAnalysisExample.java)
    ```shell
    java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.SentimentAnalysisExample
    ```

  * [Summarization Patterns with Gemini: Stuffing, Map-Reduce Patterns](src/main/java/gemini/workshop/SummarizationExample.java)
    ```shell
    java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.SummarizationExample
    ```
* Local environments
  * [Running Open-models with Ollama and Testcontainers](src/main/java/gemini/workshop/LocalTestingWithOllamaContainers.java.unused)
    ```shell
    java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.LocalTestingWithOllamaContainers
    ```
--------
This is not an official Google product.
