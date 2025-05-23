# Gemini in Java with Vertex AI and Spring AI
Gemini workshop for Java developers, using the Spring AI orchestration framework

> [!NOTE]
> This is the code for [Gemini in Java with Vertex AI and Spring AI]()
> codelab geared towards Java developers to discover [Gemini](https://deepmind.google/technologies/gemini/)
> and its open-source variant [Gemma](https://ai.google.dev/gemma) Large Language Model by Google using [Spring AI](https://docs.spring.io/spring-ai/reference/index.html)
> framework.

## Prerequisites

The code examples have been tested on the following environment:

* Java 21
* Maven >= 3.9.6

In order to run these examples, you need to have a Google Cloud account and project ready.

Before running the examples, you'll need to set up three environment variables:

```bash
export VERTEX_AI_GEMINI_PROJECT_ID=<your-project-id>
export VERTEX_AI_GEMINI_LOCATION=<your region>, ex: us-central1
export VERTEX_AI_GEMINI_MODEL=<model>, ex: gemini-2.0-flash-exp

# Note: you can test in another region or using the gemini-1.5-flash-001 model
```

> [!IMPORTANT]
> Please update the project ID and location to match your project and select the model of your choice

Create the Maven wrapper:

```bash
mvn wrapper:wrapper
```

## Codelab Samples
The samples in this codelab are grouped by various capabilities and patterns. You will find, in order:
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
  * Function Calling with Spring AI
* Document utilities
  * Document Readers and Splitters
* Embeddings
  * Generating Text Embeddings with Vertex AI
  * Generating Multimodal Embeddings with Vertex AI
* AI use-cases and patterns
  * Retrieval-augmented generation(RAG)
  * Text classification with Few-shot prompting
  * Sentiment analysis with few-shot prompting 
  * Summarization Patterns with Gemini: Stuffing, Map-Reduce Patterns
* Local environments
  *  Running Open-models with Ollama and Testcontainers

### Build
> [!TIP]
> Note the profiles `complete` used for the build

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
  * [Grounding responses with Web Search with Vertex SDK](src/main/java/gemini/workshop/GroundingWithWebsearchExample.java)
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
* Document utilities
  * [Document Readers and Splitters](src/main/java/gemini/workshop/DocumentProcessingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.DocumentProcessingExample
      ```

* Embeddings
  * [Generating Text Embeddings with Vertex AI](src/main/java/gemini/workshop/TextEmbeddingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.TextEmbeddingExample
      ```

  * [Generating Multimodal Embeddings with Vertex AI](src/main/java/gemini/workshop/MultimodalEmbeddingExample.java)
      ```shell
      java -cp ./target/spring-ai-workshop-1.0.0-jar-with-dependencies.jar gemini.workshop.MultimodalEmbeddingExample
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
