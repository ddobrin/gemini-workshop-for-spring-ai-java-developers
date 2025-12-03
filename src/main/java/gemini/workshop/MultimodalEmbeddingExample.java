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
import java.io.IOException;

public class MultimodalEmbeddingExample {
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

    System.out.println("Multimodal embedding using com.google.genai.Client is not yet supported in the current SDK version.");
    /*
    // default multimodal embedding model multimodalembedding@001
    String model = "multimodalembedding";

    byte[] imageBytes = new ClassPathResource("/Coffee.png").getContentAsByteArray();
    byte[] videoBytes = new ClassPathResource("/Birds.mp4").getContentAsByteArray();

    Content content = Content.builder()
        .parts(Arrays.asList(
            Part.builder().text("Explain what you see in this image and this video").build(),
            Part.fromBytes(imageBytes, "image/png"),
            Part.fromBytes(videoBytes, "video/mp4")
        ))
        .build();

    // call the embedding model
    long start = System.currentTimeMillis();
    // embedContent only supports String or List<String> in current SDK version
    EmbedContentResponse embeddingResponse = client.models.embedContent(model, content, null);
    
    if (embeddingResponse.embedding() != null) {
        System.out.println("Embedding response: " + embeddingResponse.embedding().values());
    }
    System.out.println(
        "Google GenAI call took " + (System.currentTimeMillis() - start) + " ms");
    */
  }
}
