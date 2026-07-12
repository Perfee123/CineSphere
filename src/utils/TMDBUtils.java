package utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import models.MovieDTO;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TMDBUtils {

    private static final String API_KEY = EnvUtils.get("TMDB_API_KEY");
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final Gson gson = new Gson();
    
    // Genre mapping cache
    private static Map<Integer, String> genreMap = new HashMap<>();

    static {
        if (API_KEY != null && !API_KEY.isEmpty()) {
            fetchGenres();
        }
    }

    private static void fetchGenres() {
        try {
            String urlStr = BASE_URL + "/genre/movie/list?api_key=" + API_KEY;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                    JsonObject response = gson.fromJson(reader, JsonObject.class);
                    Type listType = new TypeToken<List<MovieDTO.GenreDTO>>(){}.getType();
                    List<MovieDTO.GenreDTO> genres = gson.fromJson(response.get("genres"), listType);
                    for (MovieDTO.GenreDTO genre : genres) {
                        genreMap.put(genre.id, genre.name);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch genres: " + e.getMessage());
        }
    }

    public static String getGenreName(int id) {
        return genreMap.getOrDefault(id, "Unknown");
    }

    public static List<MovieDTO> getNowPlaying() {
        if (API_KEY == null) return Collections.emptyList();
        
        try {
            String urlStr = BASE_URL + "/movie/now_playing?api_key=" + API_KEY + "&language=en-US&page=1";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                    JsonObject response = gson.fromJson(reader, JsonObject.class);
                    Type listType = new TypeToken<List<MovieDTO>>(){}.getType();
                    return gson.fromJson(response.get("results"), listType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<MovieDTO> searchMovies(String query) {
        if (API_KEY == null || query == null || query.trim().isEmpty()) return Collections.emptyList();
        
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String urlStr = BASE_URL + "/search/movie?api_key=" + API_KEY + "&language=en-US&query=" + encodedQuery + "&page=1&include_adult=false";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                    JsonObject response = gson.fromJson(reader, JsonObject.class);
                    Type listType = new TypeToken<List<MovieDTO>>(){}.getType();
                    return gson.fromJson(response.get("results"), listType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static MovieDTO getMovieDetails(int movieId) {
        if (API_KEY == null) return null;
        
        try {
            String urlStr = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY + "&language=en-US";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                    return gson.fromJson(reader, MovieDTO.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getImageUrl(String path, String size) {
        if (path == null) return null;
        // size can be "w500", "original", etc.
        return "https://image.tmdb.org/t/p/" + size + path;
    }
    
    public static String downloadImageLocally(String path, String size, String prefix) {
        if (path == null || path.isEmpty()) return null;
        try {
            String urlStr = getImageUrl(path, size);
            java.net.URL url = new java.net.URL(urlStr);
            
            String fileName = prefix + "_" + path.replace("/", "");
            java.io.File dir = new java.io.File(System.getProperty("user.dir") + "/data/images");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            java.io.File outputFile = new java.io.File(dir, fileName);
            if (!outputFile.exists()) {
                try (java.io.InputStream in = url.openStream()) {
                    java.nio.file.Files.copy(in, outputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return outputFile.toURI().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
