package com.example.ui;

import com.example.Book;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
        public final boolean found;
        public final Book completedBook;
        public final String message;

        public ValidationResult(boolean found, Book completedBook, String message) {
            this.found = found;
            this.completedBook = completedBook;
            this.message = message;
        }
    }

    // ── HTTP client ───────────────────────────────────────────────────────────

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    private static final String SEARCH_BASE  = "https://openlibrary.org/search.json";
    private static final String EDITION_BASE = "https://openlibrary.org/books/";

    // ── Public API ────────────────────────────────────────────────────────────

    public static ValidationResult validate(Book book) {
        String isbn   = nullSafe(book.getIsbn());
        String title  = nullSafe(book.getTitle());
        String author = nullSafe(book.getAuthor());

        if (isbn.isEmpty() && title.isEmpty()) {
            return new ValidationResult(false, book,
                    "Need at least a title or ISBN to validate.");
        }

        try {
            String json;
            String doc;

            if (!isbn.isEmpty()) {
                // ISBN is unambiguous — one result is enough
                json = fetch(SEARCH_BASE + "?isbn=" + encode(isbn) + "&limit=1");
                if (extractInt(json, "numFound") == 0) {
                    return new ValidationResult(false, book, "Not found in Open Library.");
                }
                doc = firstDoc(json);
            } else {
                // Title (+ optional author): fetch several candidates and pick the
                // one whose title best matches what the user typed
                String url = SEARCH_BASE + "?title=" + encode(title)
                        + (!author.isEmpty() ? "&author=" + encode(author) : "")
                        + "&sort=editions&limit=10";
                json = fetch(url);

                if (extractInt(json, "numFound") == 0) {
                    return new ValidationResult(false, book, "Not found in Open Library.");
                }

                doc = bestMatchingDoc(json, title);
                if (doc == null) {
                    return new ValidationResult(false, book,
                            "No close title match found in Open Library.");
                }
            }

            // ── Extract fields from the chosen doc ────────────────────────────

            String apiTitle  = extractString(doc, "title");
            int    apiYear   = extractInt(doc, "first_publish_year");

            // Pick the last author_name entry — Open Library lists adapters/editors
            // first and the original author last
            String apiAuthor = author.isEmpty()
                    ? extractLastArrayString(doc, "author_name")
                    : author;

            // Try to get ISBN from the edition endpoint first, then fall back to
            // the isbn array in the search doc (often absent for older works)
            String apiIsbn = isbn;
            if (apiIsbn.isEmpty()) {
                String editionKey = extractString(doc, "cover_edition_key");
                if (!editionKey.isEmpty()) {
                    apiIsbn = fetchIsbnFromEdition(editionKey);
                }
                if (apiIsbn.isEmpty()) {
                    apiIsbn = extractFirstArrayString(doc, "isbn");
                }
            }

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
            return new ValidationResult(false, book, "Network error: " + e.getMessage());
        }
    }

    // ── Best-match doc selection ──────────────────────────────────────────────

    /**
     * Parses all docs from {@code json} and returns the one whose title most
     * closely matches {@code searchTitle}, using a simple normalised similarity
     * score. Returns {@code null} if no doc scores above the minimum threshold.
     */
    private static String bestMatchingDoc(String json, String searchTitle) {
        List<String> docs = allDocs(json);
        if (docs.isEmpty()) return null;

        String normSearch = normalise(searchTitle);
        String bestDoc    = null;
        double bestScore  = -1;

        for (String doc : docs) {
            String docTitle = extractString(doc, "title");
            double score    = similarity(normSearch, normalise(docTitle));
            if (score > bestScore) {
                bestScore = score;
                bestDoc   = doc;
            }
        }

        // Require at least a 50% similarity to avoid wildly wrong matches
        return bestScore >= 0.5 ? bestDoc : null;
    }

    /**
     * Strips punctuation, lowercases, and trims so that "1984" and "1984 (adaptation)"
     * can be compared on their common token rather than exact characters.
     */
    private static String normalise(String s) {
        return s.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim();
    }

    /**
     * Token-based Jaccard similarity: intersection / union of word sets.
     * Ranges from 0 (no overlap) to 1 (identical word sets).
     */
    private static double similarity(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        String[] tokensA = a.split("\\s+");
        String[] tokensB = b.split("\\s+");

        java.util.Set<String> setA = new java.util.HashSet<>(List.of(tokensA));
        java.util.Set<String> setB = new java.util.HashSet<>(List.of(tokensB));

        java.util.Set<String> intersection = new java.util.HashSet<>(setA);
        intersection.retainAll(setB);

        java.util.Set<String> union = new java.util.HashSet<>(setA);
        union.addAll(setB);

        return (double) intersection.size() / union.size();
    }

    // ── Edition API call to retrieve ISBN ─────────────────────────────────────

    private static String fetchIsbnFromEdition(String editionKey)
            throws IOException, InterruptedException {
        String json = fetch(EDITION_BASE + editionKey + ".json");
        String isbn = extractFirstArrayString(json, "isbn_13");
        if (isbn.isEmpty()) isbn = extractFirstArrayString(json, "isbn_10");
        return isbn;
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

    // ── Minimal JSON extraction ───────────────────────────────────────────────

    /** Returns the raw JSON object of the first element in "docs":[...] */
    private static String firstDoc(String json) {
        int start = json.indexOf("\"docs\":[{");
        if (start < 0) return json;
        start += "\"docs\":[".length();
        int depth = 0, i = start;
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '{') depth++;
            else if (c == '}') { if (--depth == 0) return json.substring(start, i); }
        }
        return json.substring(start);
    }

    /** Returns all doc objects from the "docs":[...] array. */
    private static List<String> allDocs(String json) {
        List<String> docs = new ArrayList<>();
        int arrayStart = json.indexOf("\"docs\":[");
        if (arrayStart < 0) return docs;
        int pos = arrayStart + "\"docs\":[".length();

        while (pos < json.length()) {
            // Skip whitespace and commas between docs
            while (pos < json.length() && json.charAt(pos) != '{' && json.charAt(pos) != ']') pos++;
            if (pos >= json.length() || json.charAt(pos) == ']') break;

            // Walk the braces to find the end of this doc object
            int start = pos;
            int depth = 0;
            while (pos < json.length()) {
                char c = json.charAt(pos++);
                if (c == '{') depth++;
                else if (c == '}') { if (--depth == 0) break; }
            }
            docs.add(json.substring(start, pos));
        }
        return docs;
    }

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

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int s = idx + search.length();
        int e = json.indexOf('"', s);
        return (e > s) ? json.substring(s, e) : "";
    }

    private static String extractFirstArrayString(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int pos = idx + search.length();
        // Skip any whitespace between : and [
        while (pos < json.length() && json.charAt(pos) == ' ') pos++;
        if (pos >= json.length() || json.charAt(pos) != '[') return "";
        pos++; // skip [
        // Skip any whitespace between [ and "
        while (pos < json.length() && json.charAt(pos) == ' ') pos++;
        if (pos >= json.length() || json.charAt(pos) != '"') return "";
        pos++; // skip opening "
        int end = json.indexOf('"', pos);
        return (end > pos) ? json.substring(pos, end) : "";
    }

    /**
     * Returns the LAST element of a JSON string array.
     * Used for author_name so adapters/editors listed first are skipped.
     */
    private static String extractLastArrayString(String json, String key) {
        String search = "\"" + key + "\":[";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int arrStart = idx + search.length();
        int arrEnd = json.indexOf(']', arrStart);
        if (arrEnd < 0) return "";
        String array = json.substring(arrStart, arrEnd);
        String last = "";
        int pos = 0;
        while (pos < array.length()) {
            int open  = array.indexOf('"', pos);
            if (open < 0) break;
            int close = array.indexOf('"', open + 1);
            if (close < 0) break;
            last = array.substring(open + 1, close);
            pos = close + 1;
        }
        return last;
    }

    // ── Misc helpers ──────────────────────────────────────────────────────────

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String nullSafe(String s) {
        return (s == null) ? "" : s.trim();
    }
}