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
import com.google.genai.types.Content;
import com.google.genai.types.CountTokensConfig;
import com.google.genai.types.CountTokensResponse;
import com.google.genai.types.Part;

public class CountTokensWithConfigsExample {

  public static void main(String[] args) {

    boolean useVertexAi = Boolean.parseBoolean(System.getenv("USE_VERTEX_AI"));
    Client client;
    if (useVertexAi) {
      client = Client.builder()
          .project(System.getenv("GOOGLE_CLOUD_PROJECT"))
          .location(System.getenv("GOOGLE_CLOUD_LOCATION"))
          .vertexAI(true)
          .build();
      System.out.println("Using Vertex AI");
    } else {
      client = Client.builder()
          .apiKey(System.getenv("GOOGLE_API_KEY"))
          .build();
      System.out.println("Using Gemini Developer API");
      System.out.println("Warning: System instructions might not be supported on Gemini Developer API for token counting.");
    }

    String modelId = System.getenv("GEMINI_MODEL");

    // Sets the system instruction in the config.
    Content systemInstruction = Content.fromParts(Part.fromText("You are a history teacher."));

    CountTokensConfig config =
        CountTokensConfig.builder()
            .systemInstruction(systemInstruction)
            .build();

    long start = System.currentTimeMillis();
    CountTokensResponse response =
        client.models.countTokens(modelId, "Tell me the history of LLM", config);

    System.out.println("Response: " + response);
    System.out.println(
        "Google GenAI CountTokensWithConfigs call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
