package gemini.workshop;

import com.google.genai.Client;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.types.Document;
import com.google.genai.types.File;
import com.google.genai.types.FileSearchStore;
import com.google.genai.types.ImportFileOperation;
import com.google.genai.types.ListFileSearchStoresConfig;
import com.google.genai.types.UploadFileConfig;
import com.google.genai.types.UploadToFileSearchStoreOperation;

/**
 * An example of how to use the FileSearchStores module to upload, retrieve, and delete file search
 * stores.
 */
public final class FileSearchStoreExample {
  public static void main(String[] args) {
    String filePath = args.length > 0 ? args[0] : "pom.xml";

    String useVertexAiEnv = System.getenv("USE_VERTEX_AI");
    boolean useVertexAi = useVertexAiEnv != null ? Boolean.parseBoolean(useVertexAiEnv) : true;
    
    if (useVertexAi) {
      System.out.println("FileSearchStore is not currently supported on Vertex AI. Please use Gemini Developer API.");
      return;
    }

    Client client = Client.builder()
        .apiKey(System.getenv("GOOGLE_API_KEY"))
        .build();

    System.out.println("Using Gemini Developer API");

    try {
      FileSearchStore fileSearchStore = client.fileSearchStores.create(null);
      System.out.println("Created file store: " + fileSearchStore.name().get());

      FileSearchStore retrievedFileStore = client.fileSearchStores.get(fileSearchStore.name().get(), null);
      System.out.println("Retrieved file store: " + retrievedFileStore.name().get());

      System.out.println("List file stores: ");
      client.fileSearchStores.list(ListFileSearchStoresConfig.builder().pageSize(10).build())
          .forEach(f -> System.out.println("  File store name: " + f.name().get()));

      File file = client.files.upload(filePath, UploadFileConfig.builder().mimeType("text/plain").build());
      System.out.println("Uploaded file: " + file.name().get());

      ImportFileOperation importOperation = client.fileSearchStores.importFile(
          fileSearchStore.name().get(), file.name().get(), null);
      System.out.println("Import file operation: " + importOperation.name().get());

      importOperation = waitForOperation(client, importOperation);
      System.out.println("Import operation completed.");

      UploadToFileSearchStoreOperation uploadOperation = client.fileSearchStores.uploadToFileSearchStore(
          fileSearchStore.name().get(), filePath, null);
      System.out.println("Upload to file search store operation: " + uploadOperation.name().get());

      uploadOperation = waitForOperation(client, uploadOperation);
      String documentName = uploadOperation.response().get().documentName().get();
      System.out.println("Uploaded document: " + documentName);

      Document retrievedDocument = client.fileSearchStores.documents.get(documentName, null);
      System.out.println("Retrieved document: " + retrievedDocument.name().get());

      System.out.println("List documents: ");
      client.fileSearchStores.documents.list(fileSearchStore.name().get(), null)
          .forEach(d -> System.out.println("  Document name: " + d.name().get()));

      client.fileSearchStores.documents.delete(documentName, null);
      System.out.println("Deleted document: " + documentName);

      client.fileSearchStores.delete(fileSearchStore.name().get(), null);
      System.out.println("Deleted file store: " + fileSearchStore.name().get());

    } catch (GenAiIOException | InterruptedException e) {
      System.out.println("An error occurred: " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends com.google.genai.types.Operation> T waitForOperation(Client client, T operation) throws InterruptedException {
    while (!Boolean.TRUE.equals(operation.done().orElse(false))) {
      Thread.sleep(2000);
      operation = (T) client.operations.get(operation, null);
      System.out.println("Waiting for operation...");
    }
    return operation;
  }
}
