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
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.core.io.ClassPathResource;

public class MultimodalAudioExample {

  public static void main(String[] args) throws IOException {

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

    // Read the MP3 file from the classpath
    ClassPathResource audioUri = new ClassPathResource("/Aesop-fables-Vol01.mp3");

    // Read the MP3 file as a byte array
    byte[] audioBytes = audioUri.getInputStream().readAllBytes();

    // Create a GenerationConfig object and set the temperature to 0 for max accuracy
    GenerateContentConfig generationOptions = GenerateContentConfig.builder()
        .temperature(0.0f)
        .build();

    long start = System.currentTimeMillis();
    
    Content content = Content.builder()
        .parts(Arrays.asList(
            Part.builder().text("Please transcribe this audiobook with utmost accuracy").build(),
            Part.fromBytes(audioBytes, "audio/mp3")
        ))
        .build();

    GenerateContentResponse response = client.models.generateContent(
        System.getenv("VERTEX_AI_GEMINI_MODEL"),
        content,
        generationOptions
    );

    String output = response.text();
    System.out.println(output);
    System.out.println(
        "Google GenAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
