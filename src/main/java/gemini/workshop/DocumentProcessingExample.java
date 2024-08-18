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

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ClassPathResource;

public class DocumentProcessingExample {
  private static final int CHUNK_SIZE = 10000;  // Number of words in each window
  private static final int OVERLAP_SIZE = 2000;

  public static void main(String[] args) {
    // read Text in txt format
    TextReader textReader = new TextReader("classpath:/the-jungle-book.txt");
    String bookText = textReader.get().getFirst().getContent();
    System.out.printf("Read book %s with length %d, CharSet %s\nExcerpt: %s ...\n\n\n",
        textReader.getCustomMetadata().get(TextReader.SOURCE_METADATA),
        bookText.length(),
        textReader.getCustomMetadata().get(TextReader.CHARSET_METADATA),
        bookText.substring(0, 200));

    // read JSON documents
    //    NOTE: it reads a LIST of JSON documents
    //    To use this reader, provide a list of documents
    //    in this example: [ {..} ]
    ClassPathResource jsonUri = new ClassPathResource("book-genres.json");
    System.out.println("Reading JSON document: " + jsonUri.getFilename());
    JsonReader jsonReader = new JsonReader(jsonUri);
    List<Document> documents = jsonReader.read();
    for(Document document : documents)
      System.out.printf("Read JSON document %s with length %d\n",
          document.getContent(),
          document.getContent().length());


    // problem with PDF readers
    // commented out
    // PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/sample1.pdf",
    //     PdfDocumentReaderConfig.builder()
    //         .withPageTopMargin(0)
    //         .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
    //             .withNumberOfTopTextLinesToDelete(0)
    //             .build())
    //         .withPagesPerDocument(1)
    //         .build());
    // pdfReader.read();

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
          chunk.getContent().substring(0, 25),
          chunk.getContent().length());

  }
}
