package gemini.workshop;

import com.google.genai.Client;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

public class FunctionCallingJsonSchemaExample {

  public record WeatherRequest(String location) {}

  public static class WeatherService implements Function<WeatherRequest, Map<String, Object>> {
    @Override
    public Map<String, Object> apply(WeatherRequest request) {
      System.out.printf("Function Call: Called getCurrentWeather(%s)\n", request.location());
      return Map.of("weather", "The weather in " + request.location() + " is very nice.");
    }
  }

  public static void main(String[] args) {
    String useVertexAiEnv = System.getenv("USE_VERTEX_AI");
    boolean useVertexAi = useVertexAiEnv != null ? Boolean.parseBoolean(useVertexAiEnv) : true;
    Client client;
    if (useVertexAi) {
      client = Client.builder()
          .project(System.getenv("GOOGLE_CLOUD_PROJECT"))
          .location(System.getenv("GOOGLE_CLOUD_LOCATION"))
          .vertexAI(true)
          .build();
    } else {
      client = Client.builder()
          .apiKey(System.getenv("GOOGLE_API_KEY"))
          .build();
    }

    // In Spring AI, the schema is typically generated from the input type class.
    // While the Google SDK allows raw JSON schema strings, Spring AI abstracts this.
    // This example demonstrates using a Record to define the schema, which is the idiomatic Spring AI way.
    FunctionToolCallback weatherTool = FunctionToolCallback.builder("get_weather", new WeatherService())
        .description("Returns the weather in a given location.")
        .inputType(WeatherRequest.class)
        .build();

    var geminiChatModel = GoogleGenAiChatModel.builder()
        .genAiClient(client)
        .defaultOptions(GoogleGenAiChatOptions.builder()
            .model(System.getenv("GEMINI_MODEL"))
            .temperature(0.2)
            .toolCallbacks(List.of(weatherTool))
            .build())
        .build();

    String userText = "What is the weather in Vancouver?";
    Message userMessage = new PromptTemplate(userText).createMessage();

    long start = System.currentTimeMillis();
    System.out.println("GEMINI Response: " + geminiChatModel
        .call(new Prompt(userMessage))
        .getResult().getOutput().getText());
    System.out.println("Google GenAI Gemini call with JSON Schema (via Record) took " + (System.currentTimeMillis() - start) + " ms");
  }
}
