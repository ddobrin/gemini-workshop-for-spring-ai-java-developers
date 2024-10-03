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

import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;

public class SummarizationExample {
  private static final int CHUNK_SIZE = 10000;  // Number of words in each window
  private static final int OVERLAP_SIZE = 2000;

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder()
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withTemperature(0.2f)
            .build());

    try{
      // summarization using the Stuffing pattern
      summarizationStuffing(geminiChatModel);

      // summarization using the MapReduce pattern
      summarizationMapReduce(geminiChatModel);
    }catch(IOException | ExecutionException | InterruptedException e){
      System.out.println("Exception encountered while summarizing a document: " + e.getMessage());
    }
  }

  private static void summarizationStuffing(VertexAiGeminiChatModel geminiChatModel) throws IOException {
    // read book
    TextReader textReader = new TextReader("classpath:/The-Wasteland-TSEliot-public.txt");
    String bookText = textReader.get().getFirst().getContent();

    // create system message template
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
        You are a helpful AI assistant.
        You are an AI assistant that helps people summarize information.
        Your name is {name}
        You should reply to the user's request with your name and also in the style of a {voice}.
        Strictly ignore Project Gutenberg & ignore copyright notice in summary output.
        """
    );
    Message systemMessage = systemPromptTemplate.createMessage(
        Map.of("name", "Gemini", "voice", "literary critic"));
      String title = "", author = "";

    // create user message template
    PromptTemplate userPromptTemplate = new PromptTemplate("""
            Please provide a concise summary covering the key points of the book {title} by {author}.
            If you do not have the information, use Google web search to ground your answer.
            Do not make information up
        """, Map.of("title", title, "author", author));
    Message userMessage = userPromptTemplate.createMessage();

    // summarize document by stuffing the prompt with the content of the document
    long start = System.currentTimeMillis();
    ChatResponse response = geminiChatModel.call(new Prompt(List.of(userMessage, systemMessage),
        VertexAiGeminiChatOptions.builder()
            .withTemperature(0.2f)
            .build()));

    System.out.println("Gemini response - Stuffing Pattern: \n" + response.getResult().getOutput().getContent());
    System.out.print("Summarization (stuffing test) took " + (System.currentTimeMillis() - start) + " milliseconds");
  }

  private static void summarizationMapReduce(VertexAiGeminiChatModel geminiChatModel)
      throws ExecutionException, InterruptedException {
    // read book
    TextReader textReader = new TextReader("classpath:/The-Wasteland-TSEliot-public.txt");
    String bookText = textReader.get().getFirst().getContent();

    // create system message template
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
        You are a helpful AI assistant.
        You are an AI assistant that helps people summarize information in a concise way.
        Strictly ignore Project Gutenberg & ignore copyright notice in summary output.
        """
    );
    Message systemMessage = systemPromptTemplate.createMessage();

    long startTime = System.currentTimeMillis();

    int length = bookText.length();
    List<CompletableFuture<Map<Integer, String>>> futures = new ArrayList<>();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    Map<Integer, String> resultMap = new TreeMap<>(); // TreeMap to automatically sort by key

    //------
    // Note: test with different values for the CHUNG and OVERLAP_SIZE
    //----
    for (int i = 0; i < length; i += (CHUNK_SIZE - OVERLAP_SIZE)) {
      final int index = i / (CHUNK_SIZE - OVERLAP_SIZE); // Calculate chunk index
      int end = Math.min(i + CHUNK_SIZE, length);
      String chunk = bookText.substring(i, end);

      CompletableFuture<Map<Integer, String>> future = CompletableFuture.supplyAsync(() -> processChunk(index,
          chunk, systemMessage, geminiChatModel), executor);
      futures.add(future);
    }

    // Wait for all futures to complete and collect the results in resultMap
    CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenAccept(v -> futures.forEach(f -> f.thenAccept(resultMap::putAll)));

    allDone.get(); // Wait for all processing to complete

    // Build the final context string from the sorted entries in the resultMap
    StringBuilder contextBuilder = new StringBuilder();
    for (Entry<Integer, String> entry : resultMap.entrySet()) {
          //            System.out.println("Index " + entry.getKey() + ": " + entry.getValue());
      contextBuilder.append(entry.getValue()).append("\n");
    }

    String context = contextBuilder.toString();
    String output = processSummary(context, systemMessage, geminiChatModel);
    System.out.println(output);
    System.out.print("Summarization (map-reduce) took " + (System.currentTimeMillis() - startTime) + " milliseconds");

    executor.shutdown(); // Shutdown the executor
  }


  //--- Helper methods ---
  private static String processSummary(String context, Message systemMessage, VertexAiGeminiChatModel geminiChatModel) {
    long start = System.currentTimeMillis();
    System.out.println(context+"\n\n");

    // create user message template
    PromptTemplate userPromptTemplate = new PromptTemplate("""
      Strictly please give me a summary with an introduction, three one sentence bullet points, and a conclusion from the following text delimited by triple backquotes.
      
      ```Text:{content}```

      Output starts with SUMMARY:
      """, Map.of("content", context));
    Message userMessage = userPromptTemplate.createMessage();

    ChatResponse response = geminiChatModel.call(new Prompt(List.of(userMessage, systemMessage),
        VertexAiGeminiChatOptions.builder()
            .withTemperature(0.2f)
            .build()));
    System.out.println("Summarization (final summary) took " + (System.currentTimeMillis() - start) + " milliseconds");
    return response.getResult().getOutput().getContent();
  }

  private static Map<Integer, String> processChunk(
      Integer index,
      String chunk,
      Message systemMessage,
      VertexAiGeminiChatModel geminiChatModel) {

    Map<Integer, String> outputWithIndex = new HashMap<>();
    String output = processChunk("", chunk, systemMessage, geminiChatModel);
    outputWithIndex.put(index, output);
    return outputWithIndex;
  }

  private static String processChunk(
      String context,
      String chunk,
      Message systemMessage,
      VertexAiGeminiChatModel geminiChatModel) {
    long start = System.currentTimeMillis();

    PromptTemplate userPromptTemplate;
    String subSummaryOverlapTemplate =
        """
        Write a concise summary of the following text delimited by triple backquotes.
        
        ```{content}```
        
        Output starts with CONCISE SUB-SUMMARY:
        """;

    String subSummaryResource =
        """
        Taking the following context delimited by triple backquotes into consideration
        
        ```{context}```
        
        Write a concise summary of the following text delimited by triple backquotes.
        
        ```{content}```
        
        Output starts with CONCISE SUB-SUMMARY:
        """;

    if(context.trim().isEmpty()) {
      userPromptTemplate = new PromptTemplate(subSummaryOverlapTemplate, Map.of("content", chunk));
    } else {
      userPromptTemplate = new PromptTemplate(subSummaryResource, Map.of("context", context, "content", chunk));
    }
    Message userMessage = userPromptTemplate.createMessage();

    ChatResponse response = geminiChatModel.call(new Prompt(List.of(userMessage, systemMessage),
        VertexAiGeminiChatOptions.builder()
            .withTemperature(0.2f)
            .build()));
    System.out.println("Summarization (single chunk) took " + (System.currentTimeMillis() - start) + " milliseconds");
    String output = response.getResult().getOutput().getContent();
    System.out.println(output+"\n\n");
    return output;
  }
  //--- end helper methods ---
}
