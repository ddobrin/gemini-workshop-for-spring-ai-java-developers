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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

public class SentimentAnalysisExample {

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
            .topK(5)
            .topP(0.95)
            .build())
        .build();

    // Build Few-shot history
    List<Message> messages = List.of(
        new UserMessage("Lord of the Rings by J.R.R. Tolkien"),
        new AssistantMessage("Fantasy"),
        new UserMessage("Dune by Frank Herbert"),
        new AssistantMessage("Science Fiction"),
        new UserMessage("Murder on the Orient Express by Agatha Christie"),
        new AssistantMessage("Mistery"),
        new UserMessage("Pride and Prejudice by Jane Austen"),
        new AssistantMessage("Romance"),
        new UserMessage("Dracula by Bram Stoker"),
        new AssistantMessage("Horror"),
        new UserMessage("Gone With the Wind by Margaret Mitchell"),
        new AssistantMessage("Historical Fiction"),
        new UserMessage("1984 by George Orwell"),
        new AssistantMessage("Dystopian"),
        new UserMessage("Catch-22 by Joseph Heller"),
        new AssistantMessage("Comedy"),
        new UserMessage("The Autobiography of Benjamin Franklin"),
        new AssistantMessage("Biography/Autobiography"),
        new UserMessage("Sapiens: A Brief History of Humankind by Yuval Noah Harari"),
        new AssistantMessage("History"),
        new UserMessage("A Brief History of Time by Stephen Hawking"),
        new AssistantMessage("Science"),
        new UserMessage("How to Win Friends and Influence People by Dale Carnegie"),
        new AssistantMessage("Self-Help"),
        new UserMessage("The Wealth of Nations by Adam Smith\n"),
        new AssistantMessage("Business/Economics"),
        new UserMessage("Joy of Cooking"),
        new AssistantMessage("Cookbook"),
        new UserMessage("Eat, Pray, Love by Elizabeth Gilbert"),
        new AssistantMessage("Travel")
    );

    String systemMessage =
        """
        You are a helpful AI assistant.
        You are an AI assistant that helps people classify the provided text into the following categories

        Fantasy: Includes magic, mythical creatures, and alternate universes.
        Science Fiction: Speculative fiction dealing with advanced technology or future settings.
        Mystery: Stories involving a crime or puzzle to be solved.
        Romance: Focuses on romantic relationships.
        Horror: Elicits fear or suspense.
        Historical Fiction: Set in a historical period.
        Dystopian: Depicts a future society that is often oppressive or undesirable.
        Comedy: Intended to be humorous.
        
        Biography/Autobiography: Accounts of a person's life.
        History: Records of past events.
        Science: Deals with the natural world and its phenomena.
        Self-Help: Offers advice or guidance on personal development.
        Business/Economics: Related to business, finance, and economics.
        Cookbook: Contains recipes for preparing food.
        Travel: Describes places and cultures.
        """;

    // create the memory for the few-shot history
    ChatMemory chatMemory =  MessageWindowChatMemory.builder().build();
    chatMemory.add("examples", messages);

    // use the fluent ChatClient interface and provision chat history
    // and finer grained control of building the request
    long start = System.currentTimeMillis();
    String response =
        ChatClient
            .builder(geminiChatModel)
            .build()
        .prompt()
        .system(systemMessage)
        .advisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build())
        .user("""
            In which category does the jungle book by Rudyard Kipling fit in best? 
            What is the name of the main character? 
            Is the main character portrayed in one of the following ways: positive, neutral, ambiguous or negative? 
            Recommend other books with similar characters. 
            Return book category, main character name, main character sentiment and book recommendations strictly in JSON format""")
        .call()
        .content();
    System.out.println("GEMINI: " + response);
    System.out.println(
        "Google GenAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}


