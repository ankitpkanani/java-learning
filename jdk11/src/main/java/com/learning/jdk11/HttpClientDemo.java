package com.learning.jdk11;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * JDK 11 (JEP 321): java.net.http.HttpClient -- a modern, fluent HTTP client
 * built into the JDK, replacing the low-level HttpURLConnection and third-party
 * libraries for basic use cases. Supports HTTP/1.1 and HTTP/2, sync and async
 * calls, and a builder-style API throughout.
 *
 * This demo spins up a tiny local HTTP server (com.sun.net.httpserver.HttpServer,
 * available since JDK 6) so the whole thing runs offline against localhost --
 * no external network access required.
 */
public class HttpClientDemo {

    public static void main(String[] args) throws Exception {
        HttpServer server = startLocalServer();
        try {
            URI baseUri = URI.create("http://localhost:" + server.getAddress().getPort());
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            syncGet(client, baseUri);
            customHeaders(client, baseUri);
            postWithBody(client, baseUri);
            asyncGet(client, baseUri);
            requestTimeout(client, baseUri);
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer startLocalServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        server.createContext("/hello", exchange -> {
            byte[] body = "Hello from the local HTTP server!".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });

        server.createContext("/echo", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, requestBody.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(requestBody);
            }
        });

        server.createContext("/slow", exchange -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500); // deliberately slower than the client's request timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "eventually...".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });

        server.start();
        System.out.println("local HttpServer listening on port " + server.getAddress().getPort());
        return server;
    }

    private static void syncGet(HttpClient client, URI baseUri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/hello"))
                .GET()
                .build();

        // send() blocks the calling thread until the response arrives.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("sync GET status: " + response.statusCode());
        System.out.println("sync GET body: " + response.body());
    }

    private static void customHeaders(HttpClient client, URI baseUri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/hello"))
                .header("X-Demo-Header", "jdk11")
                .header("Accept", "text/plain")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("response headers: " + response.headers().map());
    }

    private static void postWithBody(HttpClient client, URI baseUri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/echo"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("payload sent from HttpClientDemo"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST echoed back: " + response.body());
    }

    private static void asyncGet(HttpClient client, URI baseUri) {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/hello")).GET().build();

        // sendAsync() returns a CompletableFuture immediately; the request runs on a background thread.
        CompletableFuture<String> bodyFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);

        System.out.println("sendAsync() returned immediately, main thread keeps going...");
        String body = bodyFuture.join(); // block only where we actually need the result
        System.out.println("async GET body (after join()): " + body);
    }

    private static void requestTimeout(HttpClient client, URI baseUri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/slow"))
                .timeout(Duration.ofMillis(100)) // shorter than the /slow endpoint's 500ms delay
                .GET()
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("did not time out (unexpected)");
        } catch (HttpTimeoutException e) {
            System.out.println("request.timeout(100ms) against a 500ms-slow endpoint: "
                    + "caught HttpTimeoutException as expected");
        }
    }
}
