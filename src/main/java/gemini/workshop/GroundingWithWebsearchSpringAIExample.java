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

import com.google.genai.Client;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

public class GroundingWithWebsearchSpringAIExample {

  public static void main(String[] args) {

    boolean useVertexAi = Boolean.parseBoolean(System.getenv("USE_VERTEX_AI"));
    Client client;
    if (useVertexAi) {
      client = Client.builder()
          .project(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
          .location(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
          .vertexAI(true)
          .build();
    } else {
      client = Client.builder()
          .apiKey(System.getenv("GOOGLE_API_KEY"))
          .build();
    }

    // call the 2 models
    // observe that the non-grounded call can't provide the requested info
    askModel(client,"Non-grounded Gemini model", false);
    // grounded call can provide the requested info
    askModel(client,"Grounded Gemini model", true);
  }

  private static void askModel(Client client, String modelType, boolean useWebSearch) {
    // enable or disable Web Search with Google in the ChatOptions
    var geminiChatModel = GoogleGenAiChatModel.builder()
        .genAiClient(client)
        .defaultOptions(GoogleGenAiChatOptions.builder()
            .model(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .temperature(0.2)
            .topK(5)
            .topP(0.95)
            .googleSearchRetrieval(useWebSearch)
            .build())
        .build();

    String prompt = "Which country won most medals at the Paris 2024 Olympics";
    // Spring AI issue - fixed  in upcoming release
    // Websearch flag must be set in a Prompt object creation
    // currently, setting it in the ChatOptions won't copy the flag in the prompt
    Prompt promptObject = new Prompt(prompt, GoogleGenAiChatOptions.builder().googleSearchRetrieval(useWebSearch).build());

    long start = System.currentTimeMillis();
    System.out.println("Model type: " + modelType);
    ChatResponse chatResponse = geminiChatModel.call(promptObject);

    System.out.println("GEMINI: " + chatResponse.getResult().getOutput().getText());
    System.out.println(
        "Google GenAI " + modelType + " Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
