package com.learning.jdk21;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JDK 18 (JEP 408): a simple, static-file HTTP server built into the JDK --
 * both as a `jwebserver` command-line tool and as the SimpleFileServer API
 * used here, for quickly serving a directory (e.g. for local testing) without
 * pulling in a full web framework.
 */
public class SimpleWebServerDemo {

    public static void main(String[] args) throws IOException, InterruptedException {
        Path root = Files.createTempDirectory("jdk21-simple-web-server-demo");
        try {
            Files.writeString(root.resolve("index.html"), "<html><body>Hello from SimpleFileServer!</body></html>");
            Files.createDirectory(root.resolve("data"));
            Files.writeString(root.resolve("data/notes.txt"), "plain text file served statically");

            HttpServer server = SimpleFileServer.createFileServer(
                    new InetSocketAddress("localhost", 0),
                    root,
                    SimpleFileServer.OutputLevel.NONE); // quiet -- we'll print our own status lines
            server.start();
            try {
                int port = server.getAddress().getPort();
                System.out.println("SimpleFileServer serving " + root + " on port " + port);

                fetchIndexPage(port);
                fetchNestedFile(port);
                fetchMissingFileGets404(port);
            } finally {
                server.stop(0);
            }
        } finally {
            deleteRecursively(root);
        }
    }

    private static void fetchIndexPage(int port) throws IOException, InterruptedException {
        String body = get(port, "/index.html");
        System.out.println("GET /index.html -> " + body);
    }

    private static void fetchNestedFile(int port) throws IOException, InterruptedException {
        String body = get(port, "/data/notes.txt");
        System.out.println("GET /data/notes.txt -> " + body);
    }

    private static void fetchMissingFileGets404(int port) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/missing.html"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("GET /missing.html -> status " + response.statusCode() + " (file not found)");
    }

    private static String get(int port, String path) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static void deleteRecursively(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // best-effort cleanup of a temp directory
                        }
                    });
        }
    }
}
