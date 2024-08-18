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
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GoogleSearchRetrieval;
import com.google.cloud.vertexai.api.GroundingMetadata;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class GroundingWithWebsearchExample {

  public static void main(String[] args) throws IOException {

    // Initialize the Vertex client that will be used to send requests.
    try (VertexAI vertexAI = new VertexAI.Builder()
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setTransport(Transport.REST)
        .build()) {

      // Enable using the result from this tool in detecting grounding
      Tool googleSearchTool =
          Tool.newBuilder()
              .setGoogleSearchRetrieval(GoogleSearchRetrieval.newBuilder())
              .build();

      //--- create 2 models, one with grounding enabled and one without
      GenerativeModel nonGroundedModel = new GenerativeModel(
          System.getenv("VERTEX_AI_GEMINI_MODEL"),
          vertexAI);

      GenerativeModel groundedModel = new GenerativeModel(
          System.getenv("VERTEX_AI_GEMINI_MODEL"),
          vertexAI)
          .withTools(Collections.singletonList(googleSearchTool));

      String prompt = "Which country won most medals at the Paris 2024 Olympics";

      // call the 2 models
      askModel(nonGroundedModel, "Non-grounded model search:", prompt);
      askModel(groundedModel, "Model grounded with web search:", prompt);
    }
  }

  private static void askModel(GenerativeModel model, String modelType, String prompt) throws IOException {
    GenerateContentResponse response = model.generateContent(prompt);
    GroundingMetadata groundingMetadata = response.getCandidates(0).getGroundingMetadata();

    String output = ResponseHandler.getText(response);
    System.out.println(modelType);
    System.out.println("Response: " + output.trim());
    Optional.ofNullable(groundingMetadata)
        .ifPresent(s -> System.out.println("Grounding Metadata: " + s));
  }
}
