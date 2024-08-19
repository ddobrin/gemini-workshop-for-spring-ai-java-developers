/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gemini.workshop;

import com.github.dockerjava.api.model.Image;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;

// Important:
// Validate that Docker is installed on your machine and running
public class LocalTestingWithOllamaContainers {

    private static final String TC_OLLAMA_GEMMA_2_B = "tc-ollama-gemma2:2b";
    public static final String OLLAMA_VERSION = "0.3.1";
    public static final String MODEL = "gemma2:2b";

    // Creating an Ollama container with Gemma2:2B if it doesn't exist locally
    private static OllamaContainer createGemmaOllamaContainer() throws IOException, InterruptedException {

        // Check if the custom Gemma Ollama image exists; create otherwise
        List<Image> listImagesCmd = DockerClientFactory.lazyClient()
                .listImagesCmd()
                .withImageNameFilter(TC_OLLAMA_GEMMA_2_B)
                .exec();

        if (listImagesCmd.isEmpty()) {
            System.out.println("Creating a new Ollama container with Gemma2:2B image...");
            OllamaContainer ollama = new OllamaContainer("ollama/ollama:" + OLLAMA_VERSION);
            ollama.start();
            ollama.execInContainer("ollama", "pull", MODEL);
            ollama.commitToImage(TC_OLLAMA_GEMMA_2_B);
            return ollama;
        } else {
            System.out.println("Using existing Ollama container with Gemma2:2B image...");

            // Substitute the default Ollama image with the Gemma variant of your choice
            return new OllamaContainer(
                    DockerImageName.parse(TC_OLLAMA_GEMMA_2_B)
                            .asCompatibleSubstituteFor("ollama/ollama"));
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        OllamaContainer ollama = createGemmaOllamaContainer();
        ollama.start();

        // URL of the running Ollama container
        String baseURL = String.format("http://%s:%d", ollama.getHost(), ollama.getFirstMappedPort());

        // Create a new Ollama instance at the respective base URL
        var ollamaApi = new OllamaApi(baseURL);

        var chatModel = new OllamaChatModel(ollamaApi,
                OllamaOptions.create()
                        .withModel(MODEL)
                        .withTemperature(0.2f));

        long start = System.currentTimeMillis();
        ChatResponse response = chatModel.call(
                new Prompt("Please provide 5 reasons why Gemma2 is a great model for use in local environments"));
        System.out.println("Response: " + response.getResult().getOutput().getContent());
        System.out.print("Ollama call with Gemma2 took: " + (System.currentTimeMillis() - start) + " milliseconds");
    }
}
