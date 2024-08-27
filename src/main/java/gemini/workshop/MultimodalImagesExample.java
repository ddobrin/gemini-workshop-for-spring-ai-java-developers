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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.model.Media;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

public class MultimodalImagesExample {

  public static void main(String[] args) throws IOException {

    VertexAI vertexAI = new VertexAI.Builder()
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withTemperature(0.2f)
            .withTopK(40f)
            .withTopP(0.95f)
            .build());

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
    Message userMessage = new UserMessage(
        userPrompt,
        List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imageData)));

    // send the image to Gemini for multimodal analysis
    long start = System.currentTimeMillis();
    System.out.println("GEMINI: " + geminiChatModel
        .call(new Prompt(List.of(userMessage, systemMessage)))
        .getResult().getOutput().getContent());
    System.out.println(
        "VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
