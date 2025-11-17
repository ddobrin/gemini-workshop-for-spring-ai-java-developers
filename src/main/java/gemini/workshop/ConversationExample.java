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
import java.util.List;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.chat.client.ChatClient;

public class ConversationExample {

  public static void main(String[] args) {

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

    var geminiChatModel = GoogleGenAiChatModel.builder()
        .genAiClient(client)
        .defaultOptions(GoogleGenAiChatOptions.builder()
            .model(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .temperature(0.2)
            .build())
        .build();

    long start = System.currentTimeMillis();

    // use an InMemoryChat approach and inject past responses in the System Message
    // prompt in subsequent calls
    ChatMemory chatMemory = MessageWindowChatMemory.builder().build();

    var chatClient = ChatClient.builder(geminiChatModel)
        .defaultSystem("""
            	You are a helpful AI assistant with extensive literature knowledge.
              You are an AI assistant that helps people find information.
              You should reply to the user's request in the style of a literary professor.
              If you don't know the answer, just say that you don't know, don't try to make up an answer.
            """)
        .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
        .build();

        // iterate over a number of prompts and observe how
        // Gemini responses are added to the chat memory
        List.of(
            "Hello professor!",
            "Who is the main character in the The Jungle Book by Rudyard Kipling?",
            "what are his main character traits?",
            "Describe his relationships with Bagheera and Baloo",
            "Before I go, do you mind providing me the list of all characters in the book, friends or foes? ",
            "Thank you professor for all your insights!"
        ).forEach( message -> {
          System.out.println("\nUser: " + message);
          String response = chatClient
              .prompt()
              .user(message)
              .call()
              .content();
          System.out.println("Gemini: " + response);
        });
    System.out.println(
        "Google GenAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
