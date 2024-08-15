package gemini.workshop;

import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import reactor.core.publisher.Flux;

public class SimpleChatStreamingExample {

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder().setLocation(
            System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withTemperature(0.2f)
            .build());

    String prompt = "Recommend five great fiction books to read during my vacation, while travelling around Europe";

    // print responses as they are receeived
    long start = System.currentTimeMillis();
    Flux<String> responseStream = geminiChatModel.stream(prompt);
    responseStream
        .doOnNext(content -> System.out.println("Gemini response chunk: " + content.trim()))
        .blockLast();
    System.out.println(
        "VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
