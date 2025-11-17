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

import java.util.Arrays;
import java.util.List;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;

public class TextEmbeddingExample {
  public static void main(String[] args) {

    boolean useVertexAi = Boolean.parseBoolean(System.getenv("USE_VERTEX_AI"));
    GoogleGenAiEmbeddingConnectionDetails connectionDetails;
    if (useVertexAi) {
        connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
            .projectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
            .location(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
            .build();
    } else {
        connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
            .apiKey(System.getenv("GOOGLE_API_KEY"))
            .build();
    }

    // Default embedding model: text-embedding-004
    GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
        .model("text-embedding-004")
        .build();

    var embeddingModel = new GoogleGenAiTextEmbeddingModel(connectionDetails, options);

    // read the book to generate embeddings for
    TextReader reader = new TextReader("classpath:/the-jungle-book.txt");
    String embedText = reader.get().getFirst().getText();

    long start = System.currentTimeMillis();
    EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(embedText));
    System.out.println("Embedding response: " + Arrays.toString(embeddingResponse.getResult().getOutput()));
    System.out.println(
        "Text embedding call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
