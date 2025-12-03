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
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;

public class RAGExample {
  public static void main(String[] args) {
    boolean useVertexAi = Boolean.parseBoolean(System.getenv("USE_VERTEX_AI"));
    Client client;
    GoogleGenAiEmbeddingConnectionDetails connectionDetails;

    if (useVertexAi) {
      client = Client.builder()
          .project(System.getenv("GOOGLE_CLOUD_PROJECT"))
          .location(System.getenv("GOOGLE_CLOUD_LOCATION"))
          .vertexAI(true)
          .build();
      connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
          .projectId(System.getenv("GOOGLE_CLOUD_PROJECT"))
          .location(System.getenv("GOOGLE_CLOUD_LOCATION"))
          .build();
    } else {
      client = Client.builder()
          .apiKey(System.getenv("GOOGLE_API_KEY"))
          .build();
      connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
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

    // read Text in txt format
    TextReader textReader = new TextReader("classpath:/the-jungle-book.txt");
    String bookText = textReader.get().getFirst().getText();
    System.out.printf("Read book %s with length %d, CharSet %s\nExcerpt: %s ...\n\n\n",
        textReader.getCustomMetadata().get(TextReader.SOURCE_METADATA),
        bookText.length(),
        textReader.getCustomMetadata().get(TextReader.CHARSET_METADATA),
        bookText.substring(0, 200));

    //---------------------------
    //Test splitting into chunks

    // override the default chunking values as per your use case:
    // The target size of each text chunk in tokens
    // 	private int defaultChunkSize = 800;
    // The minimum size of each text chunk in characters
    // 	private int minChunkSizeChars = 350;
    // Discard chunks shorter than this
    // 	private int minChunkLengthToEmbed = 5;
    // The maximum number of chunks to generate from a text
    // 	private int maxNumChunks = 10000;
    TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(5000, 100, 5, 100000, true);
    List<Document> chunks = tokenTextSplitter.apply(textReader.get());

    System.out.println("Splitting document: " + textReader.getCustomMetadata().get(TextReader.SOURCE_METADATA));
    System.out.println("Chunks size: " + chunks.size());
    for(Document chunk : chunks)
      System.out.printf("Read text document %s ... with length %d\n",
          chunk.getText().substring(0, 25),
          chunk.getText().length());

    // Default embedding model: text-embedding-004
    GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
        .model("text-embedding-004")
        .build();

    EmbeddingModel embeddingModel = new GoogleGenAiTextEmbeddingModel(connectionDetails, options);

    // create a simple (in memory) vector store, good for education purposes
    // for production usage, here's the available list of VectorStore implementations
    // https://docs.spring.io/spring-ai/reference/api/vectordbs.html
    VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    vectorStore.add(chunks);


    // perform a similarity search in the Vector database
    String keywords = "friendship, adventure, coming of age";
    String message = String.format("Find the paragraphs mentioning keywords in the following list: {%s} in the book.",
                keywords);

    List<Document> similarDocuments = vectorStore.similaritySearch(
        SearchRequest.builder().query(message).topK(5).build());
    String content = similarDocuments.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
    System.out.println("SearchRequest in vector store with the query string: " + message);
    System.out.println("Vector search has found " + similarDocuments.size() + " documents");

    // create system message template
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
              You are a helpful assistant, conversing with a user about the subjects contained in a set of documents.
              Use the information from the DOCUMENTS section to provide accurate answers. If unsure or if the answer
              isn't found in the DOCUMENTS section, simply state that you don't know the answer and do not mention
              the DOCUMENTS section.
              
              DOCUMENTS:
              {documents}
              """);
    Message systemMessage = systemPromptTemplate.createMessage(
        Map.of("documents", content));

    PromptTemplate userPromptTemplate = new PromptTemplate("""
        Provide an analysis of the book {book} by {author}
        with the skills of a literary critic.
        What factor do the following {keywords} play in the narrative of the book.
        """
    );
    Message userMessage =  userPromptTemplate.createMessage(Map.of(
        "book", "The Jungle Book",
        "author", "Rudyard Kipling",
        "keywords", keywords));

    // call Gemini including the findings of the vector store
    long start = System.currentTimeMillis();
    System.out.println("GEMINI: " + geminiChatModel
        .call(new Prompt(List.of(userMessage, systemMessage)))
        .getResult().getOutput().getText());
    System.out.println(
        "Google GenAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
  }
}
