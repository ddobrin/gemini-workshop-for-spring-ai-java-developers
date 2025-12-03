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
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.google.genai.types.GenerateContentConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class GroundingWithWebsearchExample {

  public static void main(String[] args) throws IOException {

    boolean useVertexAi = Boolean.parseBoolean(System.getenv("USE_VERTEX_AI"));
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

    // Enable using the result from this tool in detecting grounding
    Tool googleSearchTool = Tool.builder()
        .googleSearch(GoogleSearch.builder().build())
        .build();

    String modelName = System.getenv("GEMINI_MODEL");
    String prompt = "Which country won most medals at the Paris 2024 Olympics";

    // call the 2 models
    // observe that the non-grounded call can't provide the requested info
    askModel(client, modelName, "Non-grounded model search:", prompt, null);
    // grounded call can provide the requested info
    askModel(client, modelName, "Model grounded with web search:", prompt, googleSearchTool);
  }

  // Call model with Google Websearch enabled|disabled
  private static void askModel(Client client, String modelName, String modelType, String prompt, Tool tool) throws IOException {
    long start = System.currentTimeMillis();
    
    GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();
    if (tool != null) {
        configBuilder.tools(Collections.singletonList(tool));
    }
    
    GenerateContentResponse response = client.models.generateContent(
        modelName,
        Content.builder().parts(Collections.singletonList(Part.builder().text(prompt).build())).build(),
        configBuilder.build()
    );

    String output = response.text();
    System.out.println(modelType);
    System.out.println("Response: " + output.trim());
    
    if (response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
        var candidate = response.candidates().get().get(0);
        if (candidate.groundingMetadata().isPresent()) {
             System.out.println("Grounding Metadata: " + candidate.groundingMetadata().get());
        }
    }
    
    System.out.println(
        "Google GenAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
