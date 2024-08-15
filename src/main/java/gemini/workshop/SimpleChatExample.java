package gemini.workshop;

import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;

public class SimpleChatExample {

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder().setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withTemperature(0.2f)
            .build());

    String prompt = "Recommend a great book to read during my vacation";
    long start = System.currentTimeMillis();
    System.out.println("GEMINI: " + geminiChatModel
        .call(new Prompt(prompt))
        .getResult().getOutput().getContent());
    System.out.println(
        "VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  };
}
