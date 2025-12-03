package gemini.workshop;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.genai.Client;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

public class FunctionCallingAsyncExample {

  @JsonClassDescription("Get the weather in a location")
  public static class WeatherService implements Function<WeatherService.Request, WeatherService.Response> {
    public record Request(
        @JsonProperty(required = true, value = "location") @JsonPropertyDescription("The city and state, e.g. San Francisco, CA") String location,
        @JsonProperty(required = true, value = "unit") @JsonPropertyDescription("The unit of temperature, e.g. celsius or fahrenheit") String unit) {}
    public record Response(String weather) {}

    @Override
    public Response apply(Request request) {
      System.out.printf("Function Call: Called getCurrentWeather(%s, %s)\n", request.location(), request.unit());
      return new Response("The weather in " + request.location() + " is very nice.");
    }
  }

  @JsonClassDescription("Divide two integers")
  public static class DivisionService implements Function<DivisionService.Request, DivisionService.Response> {
    public record Request(
        @JsonProperty(required = true, value = "numerator") @JsonPropertyDescription("The numerator") int numerator,
        @JsonProperty(required = true, value = "denominator") @JsonPropertyDescription("The denominator") int denominator) {}
    public record Response(int result) {}

    @Override
    public Response apply(Request request) {
      System.out.printf("Function Call: Called divideTwoIntegers(%d, %d)\n", request.numerator(), request.denominator());
      return new Response(request.numerator() / request.denominator());
    }
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
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

    FunctionToolCallback weatherTool = FunctionToolCallback.builder("getCurrentWeather", new WeatherService())
        .description("Get the weather in a location")
        .inputType(WeatherService.Request.class)
        .build();
    FunctionToolCallback divisionTool = FunctionToolCallback.builder("divideTwoIntegers", new DivisionService())
        .description("Divide two integers")
        .inputType(DivisionService.Request.class)
        .build();

    var geminiChatModel = GoogleGenAiChatModel.builder()
        .genAiClient(client)
        .defaultOptions(GoogleGenAiChatOptions.builder()
            .model(System.getenv("GEMINI_MODEL"))
            .temperature(0.2)
            .toolCallbacks(List.of(weatherTool, divisionTool))
            .build())
        .build();

    String userText = "What is the weather in Vancouver? And can you divide 10 by 2?";
    Message userMessage = new PromptTemplate(userText).createMessage();

    long start = System.currentTimeMillis();
    System.out.println("Starting Async Call...");

    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> 
        geminiChatModel.call(new Prompt(userMessage)).getResult().getOutput().getText()
    );

    // Do other work here if needed
    System.out.println("Async call initiated, doing other work...");

    String response = future.get();
    System.out.println("GEMINI Response: " + response);
    System.out.println("Google GenAI Gemini Async call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
