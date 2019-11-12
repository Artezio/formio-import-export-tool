package com.artezio.formio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.emptyList;

public class FormDeleter {

    private FormioClient formioClient = new FormioClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void delete(String apiUrl, String username, String password, String formPaths, String tags){
        String token = formioClient.getToken(apiUrl, username, password);
        List<String> formPathList = formPaths.isEmpty() ? emptyList() : Arrays.asList(formPaths.split(","));
        if (formPathList.isEmpty()) {
            formPathList = getFormPaths(apiUrl, token, tags);
        }
        formPathList.forEach(formPath -> {
            try {
                formioClient.deleteForm(apiUrl, formPath, token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private List<String> getFormPaths(String apiUrl, String token, String tags) {
        String formPathsJson = formioClient.getForms(apiUrl, token, tags, "select=path");
        try {
            JsonNode formPathArray = OBJECT_MAPPER.readTree(formPathsJson);
            return getStream(formPathArray)
                    .map(formObject -> formObject.get("path").asText())
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred during getting form paths", e);
        }
    }

    private Stream<JsonNode> getStream(JsonNode components) {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(components.iterator(), Spliterator.ORDERED), false);
    }

}
