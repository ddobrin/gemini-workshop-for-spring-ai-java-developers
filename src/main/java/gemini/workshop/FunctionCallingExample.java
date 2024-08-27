/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gemini.workshop;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.model.function.FunctionCallbackWrapper.Builder.SchemaType;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;

public class FunctionCallingExample {
  /**
   * BookStoreService is a function that checks the availability of a book in the bookstore.
   * Invoked by the LLM using the function calling feature.
   */
  @JsonClassDescription("Get the book availability in the bookstore")
  public static class BookStoreService
      implements Function<BookStoreService.Request, BookStoreService.Response> {

    @JsonInclude(Include.NON_NULL)
    @JsonClassDescription("BookStore API Request")
    public record Request(
        @JsonProperty(required = true, value = "title") @JsonPropertyDescription("The title of the book") String title,
        @JsonProperty(required = true, value = "author") @JsonPropertyDescription("The author of the book") String author) {
    }
    @JsonInclude(Include.NON_NULL)
    public record Response(String title, String author, String availability) {
    }

    @Override
    public Response apply(Request request) {
      System.out.printf("Called getBookAvailability(%s, %s)", request.title(), request.author());
      return new Response(request.title(), request.author(), "The book is available for purchase in the book store in paperback format.");
    }
  }

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder()
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    // create system message template
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
        You are a helpful AI assistant.
        You are an AI assistant that helps people get high quality literary information.
        Use function calling.
        Answer with precision.
        If the information was not fetched call the function again. Repeat at most 3 times.
        """
    );
    Message systemMessage = systemPromptTemplate.createMessage();

    // create user message template
    PromptTemplate userPromptTemplate = new PromptTemplate("""
        Write a nice note including book author, book title and availability.
        Find out if the book with the title {title} by author {author} is available in the bookstore.
        Please add also this book summary to the response, with the text available after the column, prefix it with My Book Summary:  {summary}"
        """, Map.of("title", "The Jungle Book",
                    "author", "Rudyard Kipling",
                    "summary", "This is the Jungle Book summary"));
    Message userMessage = userPromptTemplate.createMessage();

    FunctionCallbackWrapper fnWrapper = FunctionCallbackWrapper.builder(new BookStoreService())
        .withName("bookStoreAvailability")
        .withDescription("Get availability of a book in the bookstore")
        .withSchemaType(SchemaType.OPEN_API_SCHEMA)
        .build();

    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withFunctionCallbacks(List.of(fnWrapper))
            .withFunction("bookStoreAvailability")
            .build());

    // Alternatively:
    // var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
    //     VertexAiGeminiChatOptions.builder()
    //         .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
    //         .withFunction("bookStoreAvailability")
    //         .withFunctionCallbacks(List.of(FunctionCallbackWrapper.builder(new BookStoreService())
    //             .withName("bookStoreAvailability")
    //             .withDescription("Get the availability of a book in the bookstore")
    //             .build()))
    //         .build());

    long start = System.currentTimeMillis();
    System.out.println("GEMINI: " + geminiChatModel
        .call(new Prompt(List.of(userMessage, systemMessage)))
        .getResult().getOutput().getContent());
    System.out.println(
        "VertexAI Gemini call with FunctionCalling took " + (System.currentTimeMillis() - start) + " ms");
  }
}
