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
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;

public class GroundingWithWebsearchSpringAIExample {

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder()
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    // call the 2 models
    // observe that the non-grounded call can't provide the requested info
    askModel(vertexAI,"Non-grounded Gemini model", false);
    // grounded call can provide the requested info
    askModel(vertexAI,"Grounded Gemini model", true);
  }

  private static void askModel(VertexAI vertexAI, String modelType, boolean useWebSearch) {
    // enable or disable Web Search with Google in the ChatOptions
    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withTemperature(0.8)
            .withTopK(5.0f)
            .withTopP(0.95)
            .withGoogleSearchRetrieval(useWebSearch)
            .build());

    String prompt = "Which country won most medals at the Paris 2024 Olympics";
    // Spring AI issue - fixed  in upcoming release
    // Websearch flag must be set in a Prompt object creation
    // currently, setting it in the ChatOptions won't copy the flag in the prompt
    Prompt promptObject = new Prompt(prompt, VertexAiGeminiChatOptions.builder().withGoogleSearchRetrieval(useWebSearch).build());

    long start = System.currentTimeMillis();
    System.out.println("Model type: " + modelType);
    ChatResponse chatResponse = geminiChatModel.call(promptObject);

    System.out.println("GEMINI: " + chatResponse.getResult().getOutput().getContent());
    System.out.println(
        "VertexAI " + modelType + " Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
