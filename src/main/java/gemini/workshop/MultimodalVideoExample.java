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
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.protobuf.ByteString;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;

public class MultimodalVideoExample {

  public static void main(String[] args) throws IOException {

    // Initialize the Vertex client that will be used to send requests.
    try (VertexAI vertexAI = new VertexAI.Builder()
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setTransport(Transport.REST)
        .build()) {

        // // Read the MP3 file from the classpath
        ClassPathResource audioUri = new ClassPathResource("/Birds.mp4");

        // Read the MP3 file as a byte array
        byte[] audioBytes = audioUri.getInputStream().readAllBytes();

        // Create a ByteString from the byte array
        ByteString audioByteString = ByteString.copyFrom(audioBytes);

        GenerativeModel model = new GenerativeModel(
            System.getenv("VERTEX_AI_GEMINI_MODEL"),
            vertexAI);

        // Create a GenerationConfig object and set the temperature to 0 for max accuracy
        GenerationConfig generationOptions = GenerationConfig.newBuilder()
            .setTemperature(0)
            .build();
        // add the configuration to the model object
        model.withGenerationConfig(generationOptions);

        GenerateContentResponse response = model.generateContent(
            ContentMaker.fromMultiModalData("""
                Provide a description of the video.
                The description should also contain anything important which people say in the video.
                """,
                PartMaker.fromMimeTypeAndData("video/mp4", audioByteString)
            ));

      String output = ResponseHandler.getText(response);
      System.out.println(output);
    }
  }
}
