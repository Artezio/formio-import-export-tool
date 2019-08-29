package com.artezio.formio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FormDownloader {

    private FormioClient formioClient = new FormioClient();

    public void downloadAllForms(String apiUrl, String username, String password, String destinationDirectory, String tags) {
        String token = formioClient.getToken(apiUrl, username, password);
        String formsJson = formioClient.getForms(apiUrl, token, tags);
        Map<String, String> formPathsById = getFormPathsById(formsJson);
        formioClient.getFormIds(apiUrl, token, tags).stream()
                .map(formId -> formioClient.getForm(apiUrl, formId, token))
                .forEach(formDefinition ->
                        createDirectoryAndWriteFormAsFile(formDefinition, destinationDirectory, formPathsById));
    }

    protected void createDirectoryAndWriteFormAsFile(String formDefinition, String targetDirectory, Map<String, String> formPathsById) {
        JSONObject form = setChildFormsPaths(new JSONObject(formDefinition), formPathsById);
        String formPath = form.getString("path");
        String[] pathParts = formPath.split("/");
        String path = Stream.of(pathParts)
                .limit(pathParts.length > 0 ? pathParts.length - 1 : 0)
                .collect(Collectors.joining("/"));
        try {
            System.out.format("Creating directory '%s/%s' for form with path '%s'%n", targetDirectory, path, formPath);
            Files.createDirectories(Paths.get(targetDirectory + "/" + path));
            Files.write(Paths.get(targetDirectory + "/" + formPath), form.toString().getBytes(Charset.forName("UTF-8")));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private JSONObject setChildFormsPaths(JSONObject definition, Map<String, String> formPathsById) {
        if (isChildForm(definition)) {
            setChildFormPath(definition, formPathsById);
        }
        List<String> nestedFieldNames = new ArrayList<>(definition.keySet());
        for (String fieldName: nestedFieldNames) {
            Object field = definition.get(fieldName);
            if (field instanceof JSONArray) {
                JSONArray modifiedArray = setChildFormsPaths((JSONArray)field, formPathsById);
                definition.put(fieldName, modifiedArray);
            }
            if (field instanceof JSONObject) {
                JSONObject modifiedObject = setChildFormsPaths((JSONObject)field, formPathsById);
                definition.put(fieldName, modifiedObject);
            }
        }
        return definition;
    }

    private JSONArray setChildFormsPaths(JSONArray definition, Map<String, String> formPathsById) {
        for (int index = 0; index < definition.length(); index++) {
            Object jsonObject = definition.get(index);
            if (jsonObject instanceof JSONObject) {
                JSONObject modifiedObject = setChildFormsPaths((JSONObject)jsonObject, formPathsById);
                definition.put(index, modifiedObject);
            }
            if (jsonObject instanceof JSONArray) {
                JSONArray modifiedArray = setChildFormsPaths((JSONArray)jsonObject, formPathsById);
                definition.put(index, modifiedArray);
            }
        }
        return definition;
    }

    private boolean isChildForm(JSONObject definition) {
        return definition.has("form")
                && definition.has("type")
                && definition.getString("type").equals("form");
    }

    private JSONObject setChildFormPath(JSONObject definition, Map<String, String> formPathsById) {
        definition.put("path", "/" + formPathsById.get(definition.getString("form")));
        return definition;
    }

    private Map<String, String> getFormPathsById(String formsJson) {
        JSONArray formArray = new JSONArray(formsJson);
        return getStream(formArray)
                .map(obj -> (JSONObject) obj)
                .collect(Collectors.toMap(form -> form.getString("_id"), form -> form.getString("path")));
    }

    private Stream<Object> getStream(JSONArray components) {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(components.iterator(), Spliterator.ORDERED), false);
    }

}
