package models;

import java.util.List;

public class MovieDTO {
    public int id;
    public String title;
    public String poster_path;
    public String backdrop_path;
    public double vote_average;
    public String release_date;
    public String overview;
    public List<Integer> genre_ids;
    
    // Details specifics
    public int runtime;
    public String tagline;
    public double popularity;
    public List<GenreDTO> genres;
    public String original_language;

    public static class GenreDTO {
        public int id;
        public String name;
    }
}
