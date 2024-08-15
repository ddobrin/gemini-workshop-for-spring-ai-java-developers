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
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class SummarizationExample {
  public static void main(String[] args) {

    VertexAI vertexAI = new VertexAI.Builder()
        .setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
        .setProjectId(System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"))
        .setTransport(Transport.REST)
        .build();

    var geminiChatModel = new VertexAiGeminiChatModel(vertexAI,
        VertexAiGeminiChatOptions.builder()
            .withModel(System.getenv("VERTEX_AI_GEMINI_MODEL"))
            .withTemperature(0.2f)
            .build());

    try{
      summarizationStuffing(geminiChatModel);
    }catch(IOException e){
      System.out.println("Could not read file");
    }

  }

  private static void summarizationStuffing(VertexAiGeminiChatModel geminiChatModel) throws IOException {
    // read book
    TextReader textReader = new TextReader("classpath:/The-Wasteland-TSEliot-public.txt");
    String bookTest = textReader.get().getFirst().getContent();

    // create system message template
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
        You are a helpful AI assistant.
        You are an AI assistant that helps people summarize information.
        Your name is {name}
        You should reply to the user's request with your name and also in the style of a {voice}.
        """
    );
    Message systemMessage = systemPromptTemplate.createMessage(
        Map.of("name", "Gemini", "voice", "literary critic"));

    // create user mesage template
    PromptTemplate userPromptTemplate = new PromptTemplate("""
        "Please provide a concise summary covering the key points of the following text.
                          TEXT: {content}
                          SUMMARY:
        """, Map.of("content", bookTest));
    Message userMessage = userPromptTemplate.createMessage();

    // summarize document by stuffing the prompt with the content of the document
    long start = System.currentTimeMillis();
    ChatResponse response = geminiChatModel.call(new Prompt(List.of(userMessage, systemMessage),
        VertexAiGeminiChatOptions.builder()
            .withTemperature(0.2f)
            .build()));

    System.out.println("Gemini response - Stuffing Pattern: \n" + response.getResult().getOutput().getContent());
    System.out.print("Summarization (stuffing test) took " + (System.currentTimeMillis() - start) + " milliseconds");
  }
}
