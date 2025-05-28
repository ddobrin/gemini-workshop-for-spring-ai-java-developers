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

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.core.convert.support.DefaultConversionService;

public class StructuredOutputExample {

	public static void mapOutputConverter(VertexAiGeminiChatModel chatClient) {
		MapOutputConverter mapOutputConverter = new MapOutputConverter();

		String format = mapOutputConverter.getFormat();
		String template = """
				You are a helpful AI assistant.
        You are an AI assistant that helps people find information.
        You should reply to the user's request in the style of a literary professor.
				Provide me a quote from a random book, including only {subject}
				{format}
				""";
		PromptTemplate promptTemplate = PromptTemplate.builder()
				.template(template)
				.variables(Map.of("subject", "book, author and quote", "format", format))
				.build();
		Prompt prompt = new Prompt(promptTemplate.createMessage());

		long start = System.currentTimeMillis();
		Generation generation = chatClient.call(prompt).getResult();

		Map<String, Object> result = mapOutputConverter.convert(generation.getOutput().getText());

		System.out.println("Prompt for MapConverter test:" + prompt.getContents());
		System.out.println("Format this response to a map: " + generation.getOutput().getText());
		System.out.println("Formatted response: " + result);
		System.out.println(
				"VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
	}

	public static void listOutputConverter(VertexAiGeminiChatModel chatClient) {
		ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());

		String format = listOutputConverter.getFormat();
		String template = """
				You are a helpful AI assistant.
        You are an AI assistant that helps people find information.
        You should reply to the user's request in the style of a literary professor.
				List the top ten {subject}
				{format}
				""";
		PromptTemplate promptTemplate = PromptTemplate.builder()
				.template(template)
				.variables(Map.of("subject", "important fiction books I should read in my lifetime, with book and author", "format", format))
				.build();
		Prompt prompt = new Prompt(promptTemplate.createMessage());

		long start = System.currentTimeMillis();
		Generation generation = chatClient.call(prompt).getResult();

		List<String> list = listOutputConverter.convert(generation.getOutput().getText());

		System.out.println("Prompt for ListConverter test:" + prompt.getContents());
		System.out.println("Format this response to a List: " + generation.getOutput().getText());
		System.out.println("Formatted response: " + list);
		System.out.println(
				"VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
	}

	public static void beanOutputConverter(VertexAiGeminiChatModel chatClient) {

		record BooksAuthor(String writer, List<String> books) {}

		BeanOutputConverter<BooksAuthor> beanOutputConverter = new BeanOutputConverter<>(BooksAuthor.class);

		String format = beanOutputConverter.getFormat();
		String writer = "Gabriel Garcia Marquez";

		String template = """
				Generate the bibliography of books written by the writer {writer}.
				{format}
				""";

		Prompt prompt = new Prompt(PromptTemplate.builder().template(template)
				.variables(Map.of("writer", writer, "format", format))
				.build()
				.createMessage());

		long start = System.currentTimeMillis();
		Generation generation = chatClient.call(prompt)
				.getResult();

		System.out.println("Prompt for BeanConverter test:" + prompt.getContents());
		String content = generation.getOutput().getText();
		System.out.println("Format this response to a Bean: " + content);

		BooksAuthor writerBooks = beanOutputConverter.convert(content);

		System.out.println("Formatted response: " + writerBooks);
		System.out.println(
				"VertexAI Gemini call took " + (System.currentTimeMillis() - start) + " ms");
	}

	public static void main(String[] args) {
			VertexAI vertexAI = new VertexAI.Builder().setLocation(System.getenv("VERTEX_AI_GEMINI_LOCATION"))
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

		// convert response to a map
		mapOutputConverter(geminiChatModel);

		// convert response to a list
		listOutputConverter(geminiChatModel);

		// convert response to a bean
		beanOutputConverter(geminiChatModel);

	}
}
