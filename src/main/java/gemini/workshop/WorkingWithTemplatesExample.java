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
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.core.io.ClassPathResource;

public class WorkingWithTemplatesExample {

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder()
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    var geminiChatModel = VertexAiGeminiChatModel.builder()
        .vertexAI(vertexAI)
        .defaultOptions(VertexAiGeminiChatOptions.builder()
            .model(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .temperature(0.2)
            .topK(5)
            .topP(0.95)
            .build())
        .build();

    //-------------------------------------------
    // option1 : templates directly as strings
    //-------------------------------------------

    // create system message template
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
        You are a helpful AI assistant.
        You are an AI assistant that helps people get high quality literary information.
        Your name is {name}
        You should reply to the user's request with your name and also in the style of a {voice}.
        """
    );
    Message systemMessage = systemPromptTemplate.createMessage(
        Map.of("name", "Professor Gemini", "voice", "literary professor"));

    // create user message template
    PromptTemplate userPromptTemplate = new PromptTemplate("""
        Please recommend no more than {number} great {genre} book to read during my vacation.
        Return to me strictly the name and author
        """, Map.of("number", "4", "genre", "fiction"));
    Message userMessage = userPromptTemplate.createMessage();

    long start = System.currentTimeMillis();
    System.out.println("GEMINI: " + geminiChatModel
        .call(new Prompt(List.of(userMessage, systemMessage)))
        .getResult().getOutput().getText());
    System.out.println(
        "VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");

    //-------------------------------------------
    // option2 : templates as resource files
    // Can be governed, versioned, etc
    //-------------------------------------------
    SystemPromptTemplate templatefromFile = new SystemPromptTemplate(new ClassPathResource("/prompts/system-message.st"));
    System.out.println("\n\nTemplate read as resource from disk: \n" + templatefromFile.getTemplate());
  }
}
