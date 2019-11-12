package com.artezio.formio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FormioClient {
    public String getToken(String apiUrl, String username, String password) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl + "/user/login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            String data = String.format("{\"data\":{\"email\":\"%s\", \"password\": \"%s\"}}", username, password);
            request.write(data.getBytes());
            request.close();
            connection.getInputStream().close();
            return connection.getHeaderField("x-jwt-token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getForms(String apiUrl, String token, String tags, String... queryParams) {
        HttpURLConnection connection = null;

        try {
            String params = String.join("&", queryParams);
            String targetUrl = apiUrl + "/form?type=form&limit=10000000&skip=0&" + params;
            if (tags != null && !tags.isEmpty()) {
                targetUrl += "&tags__in=" + tags;
            }
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-jwt-token", token);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            return readResponse(connection);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getForm(String apiUrl, String formId, String token, String... queryParams) {
        HttpURLConnection connection = null;

        try {
            String params = String.join("&", queryParams);
            String targetUrl = apiUrl + "/form/" + formId + "?" + params;
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-jwt-token", token);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            return readResponse(connection);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getFormId(String apiUrl, String formPath, String token) {
        HttpURLConnection connection = null;
        try {
            String targetUrl = apiUrl + (formPath.startsWith("/") ? "" : "/") + formPath;
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-jwt-token", token);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            JSONObject formIdsJson = new JSONObject(readResponse(connection));
            return formIdsJson.getString("_id");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public List<String> getFormIds(String apiUrl, String token, String tags) {
        HttpURLConnection connection = null;
        try {
            String targetUrl = apiUrl + "/form?type=form&limit=10000000&skip=0&select=_id";
            if (tags != null && !tags.isEmpty()) {
                targetUrl += "&tags__in=" + tags;
            }
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-jwt-token", token);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            JSONArray formIdsJson = new JSONArray(readResponse(connection));
            List<String> formIds = new ArrayList<>();
            for (int index = 0; index < formIdsJson.length(); index++) {
                formIds.add(formIdsJson.getJSONObject(index).getString("_id"));
            }
            return formIds;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void uploadForms(String apiUrl, JSONArray forms, String token) throws Exception {
        for (int index = 0; index < forms.length(); index++) {
            JSONObject form = forms.getJSONObject(index);
            deleteForm(apiUrl, form.getString("path"), token);
            uploadForm(apiUrl, form, token);
        }
    }

    protected void uploadForm(String apiUrl, JSONObject form, String token) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl + "/form");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-jwt-token", token);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            request.write(form.toString().getBytes(Charset.forName("UTF-8")));
            request.close();
            connection.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getRoleId(String apiUrl, String roleName, String token) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format("%s/role?title=%s", apiUrl, roleName));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-jwt-token", token);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            JSONObject response = new JSONArray(readResponse(connection)).getJSONObject(0);
            return response.getString("_id");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(URLConnection connection) throws IOException {
        InputStream responseStream = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(responseStream, Charset.forName("UTF-8")));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            body.append(line);
            body.append('\r');
        }
        rd.close();
        return body.toString();
    }

    public void deleteForm(String apiUrl, String formPath, String token) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(apiUrl + "/" + formPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("x-jwt-token", token);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.getInputStream().close();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
