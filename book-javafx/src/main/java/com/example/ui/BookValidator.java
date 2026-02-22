package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Validates and auto-completes Book records using the Open Library search API.
 * No API key required. Searches by ISBN first (most precise), then falls back
 * to title + author.
 *
 * API docs: https://openlibrary.org/developers/api
 */
public class BookValidator {

    // ── Result returned to callers ────────────────────────────────────────────

    public static class ValidationResult {
        /** True if Open Library found at least one matching record. */
        public final boolean found;
        /**
         * A Book whose blank fields have been filled in from the API response.
         * Always non-null; equals the original book if nothing was completed.
         */
        public final Book completedBook;
        /** Human-readable summary suitable for a status label. */
        public final String message;

        public ValidationResult(boolean found, Book completedBook, String message) {
            this.found = found;
            this.completedBook = completedBook;
            this.message = message;
        }
    }

    // ── HTTP client (reused across calls) ─────────────────────────────────────

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    private static final String BASE = "https://openlibrary.org/search.json";

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Validates {@code book} against Open Library.
     * <ul>
     *   <li>If the ISBN is present it is used as the primary search key.</li>
     *   <li>Otherwise title (+ author if available) is used.</li>
     *   <li>Any blank field on {@code book} that the API can fill in is completed.</li>
     * </ul>
     * This method performs a blocking HTTP request — always call it from a
     * background thread (e.g. a JavaFX {@code Task}).
     */
    public static ValidationResult validate(Book book) {
        String isbn   = nullSafe(book.getIsbn());
        String title  = nullSafe(book.getTitle());
        String author = nullSafe(book.getAuthor());

        if (isbn.isEmpty() && title.isEmpty()) {
            return new ValidationResult(false, book,
                    "Need at least a title or ISBN to validate.");
        }

        try {
            String url;
            if (!isbn.isEmpty()) {
                // Most precise lookup: by ISBN
                url = BASE + "?isbn=" + encode(isbn) + "&limit=1";
            } else {
                // Fallback: title (+ optional author)
                url = BASE + "?title=" + encode(title)
                        + (!author.isEmpty() ? "&author=" + encode(author) : "")
                        + "&limit=1";
            }

            String json = fetch(url);

            if (extractInt(json, "numFound") == 0) {
                return new ValidationResult(false, book,
                        "Not found in Open Library.");
            }

            // Pull the first result document
            String doc = firstDoc(json);

            String apiTitle  = extractString(doc, "title");
            String apiAuthor = extractFirstArrayString(doc, "author_name");
            int    apiYear   = extractInt(doc, "first_publish_year");
            String apiIsbn   = extractFirstArrayString(doc, "isbn");

            // Merge: keep user-supplied value if present, otherwise use API value
            String mergedTitle  = !title.isEmpty()  ? title  : apiTitle;
            String mergedAuthor = !author.isEmpty() ? author : apiAuthor;
            int    mergedYear   = book.getYear() != 0 ? book.getYear() : apiYear;
            String mergedIsbn   = !isbn.isEmpty()   ? isbn   : apiIsbn;

            Book completed = new Book(mergedTitle, mergedAuthor, mergedYear, mergedIsbn);
            completed.setVerified(true);

            boolean patched = !mergedTitle.equals(title)
                    || !mergedAuthor.equals(author)
                    || mergedYear != book.getYear()
                    || !mergedIsbn.equals(isbn);

            String msg = patched
                    ? "Book verified. Missing fields were filled in."
                    : "Book verified — all fields confirmed.";

            return new ValidationResult(true, completed, msg);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ValidationResult(false, book,
                    "Network error: " + e.getMessage());
        }
    }

    // ── HTTP helper ───────────────────────────────────────────────────────────

    private static String fetch(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        return CLIENT.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    // ── Minimal JSON extraction (no external dependency) ─────────────────────

    /** Returns the raw JSON object string of the first element in "docs":[...]. */
    private static String firstDoc(String json) {
        int start = json.indexOf("\"docs\":[{");
        if (start < 0) return json;
        start += "\"docs\":[".length(); // points at '{'
        int depth = 0, i = start;
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '{') depth++;
            else if (c == '}') { if (--depth == 0) return json.substring(start, i); }
        }
        return json.substring(start);
    }

    /** Extracts an integer value for {@code key} from a flat JSON snippet. */
    private static int extractInt(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0;
        int s = idx + search.length();
        while (s < json.length() && json.charAt(s) == ' ') s++;
        int e = s;
        while (e < json.length() && (Character.isDigit(json.charAt(e)) || json.charAt(e) == '-')) e++;
        try { return Integer.parseInt(json.substring(s, e)); } catch (NumberFormatException ex) { return 0; }
    }

    /** Extracts a plain string value: {@code "key":"value"}. */
    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int s = idx + search.length();
        int e = json.indexOf('"', s);
        return (e > s) ? json.substring(s, e) : "";
    }

    /** Extracts the first element of a JSON string array: {@code "key":["value",...]}. */
    private static String extractFirstArrayString(String json, String key) {
        String search = "\"" + key + "\":[\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int s = idx + search.length();
        int e = json.indexOf('"', s);
        return (e > s) ? json.substring(s, e) : "";
    }

    // ── Misc helpers ──────────────────────────────────────────────────────────

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String nullSafe(String s) {
        return (s == null) ? "" : s.trim();
    }
}