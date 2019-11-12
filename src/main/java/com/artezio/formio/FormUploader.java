package com.artezio.formio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FormUploader {
    private FormioClient formioClient = new FormioClient();
    private ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void upload(String apiUrl, String username, String password, String sourceDirectory) throws IOException {
        String token = formioClient.getToken(apiUrl, username, password);
        Map<String, String> formByFormIds = collectFormsInDirectoryTree(sourceDirectory);
        formByFormIds.keySet().forEach(key -> {
            try {
                formioClient.deleteForm(apiUrl, key, token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        uploadForms(apiUrl, new ArrayList<>(formByFormIds.keySet()), formByFormIds, token);
    }

    private Map<String, String> collectFormsInDirectoryTree(String sourceDirectory) throws IOException {
        Map<String, String> formByIds = new HashMap<>();
        Files.walkFileTree(Paths.get(sourceDirectory), new MapAppendingFileVisitor(formByIds, Paths.get(sourceDirectory)));
        return formByIds;
    }

    private void setRoleAccess(ObjectNode form, String roleId) throws IOException {
        String roleReadAccess = String.format(
                "[{ " +
                        "\"roles\": [\"%s\"]," +
                        "\"type\": \"read_all\"" +
                        " }]",
                roleId);
        String submissionAccess = String.format(
                "[" +
                        "{ " +
                        "\"roles\": [\"%1$s\"]," +
                        "\"type\": \"create_own\"" +
                        "}," +
                        "{ " +
                        "\"roles\": [\"%1$s\"]," +
                        "\"type\": \"read_own\"" +
                        "}," +
                        "{ " +
                        "\"roles\": [\"%1$s\"]," +
                        "\"type\": \"update_own\"" +
                        "}," +
                        "{ " +
                        "\"roles\": [\"%1$s\"]," +
                        "\"type\": \"delete_own\"" +
                        "}" +
                        "]",
                roleId);
        form.set("access", OBJECT_MAPPER.readTree(roleReadAccess));
        form.set("submissionAccess", OBJECT_MAPPER.readTree(submissionAccess));
    }

    class MapAppendingFileVisitor extends SimpleFileVisitor<Path> {
        private Map<String, String> fileContentsByIds;
        private Path rootDirectory;

        MapAppendingFileVisitor(Map<String, String> fileContentsByIds, Path rootDirectory) {
            this.fileContentsByIds = fileContentsByIds;
            this.rootDirectory = rootDirectory;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
            if (attr.isRegularFile()) {
                byte[] bytes = Files.readAllBytes(file);
                String relativePath = rootDirectory.relativize(file).toString();
                fileContentsByIds.put(relativePath.replaceAll("\\\\", "/"), new String(bytes, Charset.forName("UTF-8")));
            }
            return CONTINUE;
        }
    }

    public void uploadForms(String apiUrl, List<String> formIds, Map<String, String> formDefinitionsByIds, String token) {
        formIds.forEach(formId -> {
            try {
                JsonNode form = OBJECT_MAPPER.readTree(uploadNestedForms(apiUrl, formDefinitionsByIds.get(formId), formDefinitionsByIds, token));
                String adminRoleId = formioClient.getRoleId(apiUrl, "Administrator", token);
                setRoleAccess((ObjectNode)form, adminRoleId);
                try {
                    formioClient.uploadForm(apiUrl, new JSONObject(form.toString()), token);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Could not upload form %s", formId), e);
            }
        });
    }

    protected String uploadNestedForms(String apiUrl, String formDefinition, Map<String, String> formDefinitionsByIds, String token) {
        try {
            return uploadNestedForms(apiUrl, OBJECT_MAPPER.readTree(formDefinition), formDefinitionsByIds, token).toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read form.", e);
        }
    }

    protected JsonNode uploadNestedForms(String apiUrl, JsonNode definition, Map<String, String> formDefinitionsByIds, String token) throws IOException {
        if (isNestedForm(definition) && !definition.get("path").asText().isEmpty()) {
            return uploadNestedForm(apiUrl, definition, formDefinitionsByIds, token);
        }
        if (definition.isArray()) {
            return uploadNestedForms(apiUrl, (ArrayNode)definition, formDefinitionsByIds, token);
        }
        if (definition.isObject()) {
            return uploadNestedForms(apiUrl, (ObjectNode) definition, formDefinitionsByIds, token);
        }
        return definition;
    }

    protected JsonNode uploadNestedForms(String apiUrl, ObjectNode node, Map<String, String> formDefinitionsByIds, String token) throws IOException {
        node = node.deepCopy();
        List<String> fieldNames = new ArrayList<>();
        node.fieldNames().forEachRemaining(fieldNames::add);
        for (String fieldName : fieldNames) {
            JsonNode nodeWithReplacedIds = uploadNestedForms(apiUrl, node.get(fieldName), formDefinitionsByIds, token);
            node.set(fieldName, nodeWithReplacedIds);
        }
        return node;
    }

    protected JsonNode uploadNestedForms(String apiUrl, ArrayNode node, Map<String, String> formDefinitionsByIds, String token) throws IOException {
        node = node.deepCopy();
        for (int i = 0; i < node.size(); i++) {
            JsonNode nodeWithReplacedIds = uploadNestedForms(apiUrl, node.get(i), formDefinitionsByIds, token);
            node.set(i, nodeWithReplacedIds);
        }
        return node;
    }

    protected JsonNode uploadNestedForm(String apiUrl, JsonNode referenceDefinition, Map<String, String> formDefinitionsByIds, String token) throws IOException {
        String formPath = referenceDefinition.get("path").asText().substring(1);
        JsonNode form = OBJECT_MAPPER.readTree(uploadNestedForms(apiUrl, formDefinitionsByIds.get(formPath), formDefinitionsByIds, token));
        String adminRoleId = formioClient.getRoleId(apiUrl, "Administrator", token);
        setRoleAccess((ObjectNode)form, adminRoleId);
        try {
            formioClient.uploadForm(apiUrl, new JSONObject(form.toString()), token);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return setNestedFormFields(apiUrl, formDefinitionsByIds, referenceDefinition, token);
    }

    protected JsonNode setNestedFormFields(String apiUrl, Map<String, String> formDefinitionByIds, JsonNode referenceDefinition, String token) throws IOException {
        ObjectNode modifiedNode = referenceDefinition.deepCopy();
        String formId = formioClient.getFormId(apiUrl, referenceDefinition.get("path").asText(), token);
        System.out.format("Form '%s' new id is '%s'%n", referenceDefinition.get("path").asText(), formId);
        modifiedNode.put("form", formId);
        modifiedNode.put("reference", false);
        modifiedNode.put("path", "");
        return uploadNestedForms(apiUrl, modifiedNode, formDefinitionByIds, token);
    }

    protected boolean isNestedForm(JsonNode node) {
        return node.isContainerNode()
                && node.has("form")
                && node.has("type")
                && node.get("type").asText().equals("form");
    }

}
