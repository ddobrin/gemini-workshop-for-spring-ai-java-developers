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
import java.util.Map;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.DocumentEmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.multimodal.VertexAiMultimodalEmbeddingModel;
import org.springframework.ai.vertexai.embedding.multimodal.VertexAiMultimodalEmbeddingOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

public class MultimodalEmbeddingExample {
  public static void main(String[] args) {
    VertexAiEmbeddingConnectionDetails connectionDetails =
        VertexAiEmbeddingConnectionDetails.builder()
            .projectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
            .location(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
            .build();

    // default multimodal embedding model multimodalembedding@001
    VertexAiMultimodalEmbeddingOptions options = VertexAiMultimodalEmbeddingOptions.builder()
        .model("multimodalembedding")
        .build();

    var embeddingModel = new VertexAiMultimodalEmbeddingModel(connectionDetails, options);

    Media imageMedia = new Media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/Coffee.png"));
    Media videoMedia = new Media(new MimeType("video", "mp4"), new ClassPathResource("/Birds.mp4"));

    var textDocument = Document.builder()
        .text("Explain what you see in this image and this video")
        .build();
    var imageDocument = Document.builder()
        .media(imageMedia)
        .build();
    var videoDocument = Document.builder()
        .media(videoMedia)
        .build();

    // create a new Embedding Request
    DocumentEmbeddingRequest embeddingRequest = new DocumentEmbeddingRequest(List.of(textDocument, imageDocument, videoDocument),
        EmbeddingOptionsBuilder.builder().build());

    // call the embedding model
    long start = System.currentTimeMillis();
    EmbeddingResponse embeddingResponse = embeddingModel.call(embeddingRequest);
    System.out.println("Embedding response: " + Arrays.toString(embeddingResponse.getResult().getOutput()));
    System.out.println(
        "VertexAI call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
