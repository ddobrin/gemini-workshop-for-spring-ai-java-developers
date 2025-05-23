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
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import reactor.core.publisher.Flux;

public class SimpleChatStreamingExample {

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder()
            .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
            .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
            .setTransport(Transport.REST)
            .build();

    var geminiChatModel = VertexAiGeminiChatModel.builder()
        .vertexAI(vertexAI)
        .defaultOptions(VertexAiGeminiChatOptions.builder()
            .model(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .temperature(0.2)
            .build())
        .build();

    String prompt = "Recommend five great fiction books to read during my vacation, while travelling around Europe";

    // stream responses and print as they are received
    long start = System.currentTimeMillis();
    Flux<String> responseStream = geminiChatModel.stream(prompt);
    responseStream
        .doOnNext(content -> System.out.println("Gemini response chunk: " + content.trim()))
        .blockLast();
    System.out.println(
        "VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
