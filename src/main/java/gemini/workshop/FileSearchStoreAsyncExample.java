package gemini.workshop;

import com.google.genai.Client;
import com.google.genai.types.File;
import com.google.genai.types.FileSearchStore;
import com.google.genai.types.ListFileSearchStoresConfig;
import com.google.genai.types.Operation;
import com.google.genai.types.UploadFileConfig;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An example of how to use the FileSearchStores module to upload, retrieve, and delete file search
 * stores asynchronously.
 */
public final class FileSearchStoreAsyncExample {
  private static <T extends Operation> CompletableFuture<T> awaitOperationComplete(
      Client client, T operation) {
    if (Boolean.TRUE.equals(operation.done().orElse(false))) {
      System.out.println("Operation " + operation.name().get() + " completed.");
      return CompletableFuture.completedFuture(operation);
    }
    System.out.println("Waiting for operation to complete...");
    return CompletableFuture.supplyAsync(
            () -> operation, CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS))
        .thenApply(
            op -> {
              try {
                return client.async.operations.get(op, null).get();
              } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to get operation status", e);
              }
            })
        .thenCompose(newOp -> awaitOperationComplete(client, (T) newOp));
  }

  public static void main(String[] args) throws Exception {
    String filePath = args.length > 0 ? args[0] : "pom.xml";
    
    String useVertexAiEnv = System.getenv("USE_VERTEX_AI");
    boolean useVertexAi = useVertexAiEnv != null ? Boolean.parseBoolean(useVertexAiEnv) : true;
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
    
    if (useVertexAi) {
        System.out.println("Warning: FileSearchStore might not be fully supported on Vertex AI yet via this SDK method.");
    } else {
        System.out.println("Using Gemini Developer API");
    }

    try (client) {
      // Create store
      CompletableFuture<Void> finalFuture =
          client
              .async
              .fileSearchStores
              .create(null)
              .thenCompose(
                  store -> {
                    System.out.println("Created file store: " + store.name().get());
                    // Get store
                    return client
                        .async
                        .fileSearchStores
                        .get(store.name().get(), null)
                        .thenAccept(
                            retrievedStore ->
                                System.out.println(
                                    "Retrieved file store: " + retrievedStore.name().get() + ")"))
                        // List stores.
                        .thenCompose(
                            v ->
                                client.async.fileSearchStores.list(
                                    ListFileSearchStoresConfig.builder().pageSize(10).build()))
                        .thenCompose(
                            pager -> {
                              System.out.println("List file stores: ");
                              return pager.forEach(
                                  item -> System.out.println("  File store name: " + item.name().get()));
                            })
                        .thenApply(v -> store);
                  })
              .thenCompose(
                  store -> {
                    // Upload File
                    return client
                        .async
                        .files
                        .upload(
                            filePath, UploadFileConfig.builder().mimeType("text/plain").build())
                        .thenApply(
                            file -> {
                              System.out.println("Uploaded file: " + file.name().get());
                              return new Object[] {store, file};
                            });
                  })
              .thenCompose(
                  objects -> {
                    FileSearchStore store = (FileSearchStore) objects[0];
                    File file = (File) objects[1];
                    // Import File
                    return client
                        .async
                        .fileSearchStores
                        .importFile(store.name().get(), file.name().get(), null)
                        .thenCompose(operation -> awaitOperationComplete(client, operation))
                        .thenApply(
                            completedOp -> {
                              System.out.println("Import File: LRO Completed.");
                              return store;
                            });
                  })
              // Direct upload file to the store
              .thenCompose(
                  store -> {
                    return client
                        .async
                        .fileSearchStores
                        .uploadToFileSearchStore(store.name().get(), filePath, null)
                        .thenCompose(operation -> awaitOperationComplete(client, operation))
                        .thenApply(
                            completedOp -> {
                              String docName = completedOp.response().get().documentName().get();
                              System.out.println("Direct Upload: Completed document " + docName);
                              return new Object[] {store, docName};
                            });
                  })
              .thenCompose(
                  objects -> {
                    FileSearchStore store = (FileSearchStore) objects[0];
                    String docName = (String) objects[1];
                    return client
                        .async
                        .fileSearchStores
                        .documents
                        .get(docName, null)
                        .thenAccept(
                            doc ->
                                System.out.println(
                                    "Get Document: Success (" + doc.name().get() + ")"))
                        // List documents
                        .thenCompose(
                            v ->
                                client.async.fileSearchStores.documents.list(
                                    store.name().get(), null))
                        .thenCompose(
                            pager -> {
                              System.out.println("List all document names: ");
                              return pager.forEach(
                                  item ->
                                      System.out.println("  document name: " + item.name().get()));
                            })
                        // Delete document
                        .thenCompose(
                            v -> client.async.fileSearchStores.documents.delete(docName, null))
                        .thenRun(() -> System.out.println("Delete Document: Success."))
                        // Delete store
                        .thenCompose(
                            v -> client.async.fileSearchStores.delete(store.name().get(), null))
                        .thenAccept(v -> System.out.println("Delete Store: Success."));
                  });

      finalFuture.get();
    }
    System.out.println("Async execution for file search stores completed successfully.");
  }
}
