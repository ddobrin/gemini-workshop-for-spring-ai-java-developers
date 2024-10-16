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
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingOptions;

public class TextEmbeddingExample {
  public static void main(String[] args) {
    VertexAiEmbeddingConnectionDetails connectionDetails =
        VertexAiEmbeddingConnectionDetails.builder()
            .withProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
            .withLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
            .build();

    // Default embedding model: text-embedding-004
    VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
        .withModel(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME)
        .build();

    var embeddingModel = new VertexAiTextEmbeddingModel(connectionDetails, options);

    // read the book to generate embeddings for
    TextReader reader = new TextReader("classpath:/the-jungle-book.txt");
    String embedText = reader.get().getFirst().getContent();

    long start = System.currentTimeMillis();
    EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(embedText));
    System.out.println("Embedding response: " + Arrays.toString(embeddingResponse.getResult().getOutput()));
    System.out.println(
        "VertexAI call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
