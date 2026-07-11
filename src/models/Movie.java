package models;

import java.util.List;

public class Movie {
    private String id;
    private String title;
    private String genre;
    private String runtime;
    private String description;
    private List<Showtime> showtimes;
    
    private int tmdbId = -1;
    private String posterPath;
    private String bannerPath;

    public Movie(String id, String title, String genre, String runtime, String description, List<Showtime> showtimes) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.runtime = runtime;
        this.description = description;
        this.showtimes = showtimes;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getRuntime() { return runtime; }
    public String getDescription() { return description; }
    public List<Showtime> getShowtimes() { return showtimes; }

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    private String showingFrom;
    private String showingUntil;
    private double adultPrice;
    private double kidsPrice;

    public String getShowingFrom() { return showingFrom; }
    public void setShowingFrom(String showingFrom) { this.showingFrom = showingFrom; }

    public String getShowingUntil() { return showingUntil; }
    public void setShowingUntil(String showingUntil) { this.showingUntil = showingUntil; }

    public double getAdultPrice() { return adultPrice; }
    public void setAdultPrice(double adultPrice) { this.adultPrice = adultPrice; }

    public double getKidsPrice() { return kidsPrice; }
    public void setKidsPrice(double kidsPrice) { this.kidsPrice = kidsPrice; }
}
