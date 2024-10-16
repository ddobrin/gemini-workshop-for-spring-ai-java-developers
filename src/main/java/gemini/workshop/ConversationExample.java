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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;

public class ConversationExample {

  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder()
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withTemperature(0.2)
            .build());

    long start = System.currentTimeMillis();

    // use an InMemoryChat approach and inject past responses in the System Message
    // prompt in subsequent calls
    ChatMemory chatMemory = new InMemoryChatMemory();

    var chatClient = ChatClient.builder(geminiChatModel)
        .defaultSystem("""
            	You are a helpful AI assistant with extensive literature knowledge.
              You are an AI assistant that helps people find information.
              You should reply to the user's request in the style of a literary professor.
              If you don't know the answer, just say that you don't know, don't try to make up an answer.
            """)
        .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
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
        "VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
