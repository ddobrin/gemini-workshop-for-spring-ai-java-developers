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
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

public class MultimodalImagesExample {

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

    var geminiChatModel = GoogleGenAiChatModel.builder()
        .genAiClient(client)
        .defaultOptions(GoogleGenAiChatOptions.builder()
            .model(System.getenv("GEMINI_MODEL"))
            .temperature(0.2)
            .topK(5)
            .topP(0.95)
            .build())
        .build();

    // create system message template
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
        You are a helpful AI assistant.
        You are an AI assistant that helps people get high quality information from media.
        Your name is {name}
        You should reply to the user's request with your name and also in the style of a {voice}.
        """
    );
    Message systemMessage = systemPromptTemplate.createMessage(
        Map.of("name", "Researcher Gemini", "voice", "multimedia expert"));

    // read image from classpath
    var imageData = new ClassPathResource("/TheJungleBook.jpg");

    // create user message
    String userPrompt = """
      Extract the title and author from the image, strictly in JSON format.
      Add a description of the image to the JSON response
      """;
    Message userMessage = UserMessage.builder()
        .text(userPrompt)
        .media(List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imageData)))
        .build();

    // send the image to Gemini for multimodal analysis
    long start = System.currentTimeMillis();
    System.out.println("GEMINI: " + geminiChatModel
        .call(new Prompt(List.of(userMessage, systemMessage)))
        .getResult().getOutput().getText());
    System.out.println(
        "Google GenAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
