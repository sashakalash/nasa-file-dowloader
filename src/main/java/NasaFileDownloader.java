import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NasaFileDownloader {
    private CloseableHttpClient httpClient;
    private final String URL = "https://api.nasa.gov/planetary/apod?api_key=8g6sC4oIp3tgVd4kjUyzlNjoD2BegvXYLENam8zF";
    private ObjectMapper mapper = new ObjectMapper();
    private static NasaResponse response;
    private final String REG_EXP = "[^/\\\\&\\?]+\\.\\w{3,4}(?=([\\?&].*$|$))";


    public static void main(String[] args) throws IOException {
        NasaFileDownloader nasaFileDownloader = new NasaFileDownloader();
        nasaFileDownloader.getFileFromNasa();
    }

    private void getFileFromNasa() throws IOException {
        createHttpClient();
        response = getNASAResponse();
        HttpEntity fileBody = getNASAFile(response.getUrl());
        recordDataToFile(getFileNameFromBody(response.getUrl()), fileBody);
    }

    private void createHttpClient() {
        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();
    }

    private NasaResponse getNASAResponse() throws IOException {
        HttpGet request = new HttpGet(URL);
        return mapper.readValue(
                httpClient.execute(request).getEntity().getContent(),
                new TypeReference<>() {
                }
        );
    }

    private HttpEntity getNASAFile(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity();
    }

    private void recordDataToFile(String filebody, HttpEntity body) {
        try (BufferedInputStream bis = new BufferedInputStream(body.getContent());
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filebody)))) {
            int inByte;
            while ((inByte = bis.read()) != -1) {
                bos.write(inByte);
            }
            bis.close();
            bos.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String getFileNameFromBody(String fullPath) {
        Pattern pattern = Pattern.compile(REG_EXP);
        Matcher matcher = pattern.matcher(fullPath);
        String result = "";
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            result = fullPath.substring(start, end);
        }
        return result;
    }
}