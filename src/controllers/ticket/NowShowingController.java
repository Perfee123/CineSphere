package controllers.ticket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import models.MovieDTO;
import utils.TMDBUtils;

import java.util.List;
import java.util.stream.Collectors;

public class NowShowingController {

    @FXML private ComboBox<String> genreFilterCombo;
    @FXML private TextField searchField;
    @FXML private FlowPane moviesGrid;

    private List<MovieDTO> allMovies;

    @FXML
    public void initialize() {
        genreFilterCombo.getItems().addAll("All Genres", "Action", "Comedy", "Drama", "Science Fiction", "Horror", "Thriller");
        genreFilterCombo.getSelectionModel().selectFirst();
        
        genreFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterMovies());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMovies());
        
        // Show loading spinner
        javafx.scene.layout.VBox loaderContainer = new javafx.scene.layout.VBox(15);
        loaderContainer.setAlignment(javafx.geometry.Pos.CENTER);
        loaderContainer.setPadding(new javafx.geometry.Insets(100, 0, 0, 0));
        
        atlantafx.base.controls.RingProgressIndicator loader = new atlantafx.base.controls.RingProgressIndicator();
        loader.setProgress(-1); // Indeterminate
        
        javafx.scene.control.Label waitLbl = new javafx.scene.control.Label("Please wait, loading movies...");
        waitLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        
        loaderContainer.getChildren().addAll(loader, waitLbl);
        moviesGrid.getChildren().clear();
        moviesGrid.setAlignment(javafx.geometry.Pos.CENTER);
        moviesGrid.getChildren().add(loaderContainer);
        
        // Fetch movies asynchronously to not block UI
        new Thread(() -> {
            try {
                models.MovieDAO dao = new models.MovieDAO();
                List<models.Movie> dbMovies = dao.getActiveMovies();
                allMovies = new java.util.ArrayList<>();
                
                for (models.Movie dbm : dbMovies) {
                    MovieDTO dto;
                    if (dbm.getTmdbId() > 0) {
                        dto = TMDBUtils.getMovieDetails(dbm.getTmdbId());
                        if (dto == null) {
                            dto = new MovieDTO(); // Fallback
                            dto.id = dbm.getTmdbId();
                        }
                    } else {
                        dto = new MovieDTO();
                        dto.id = dbm.getTmdbId(); // keep -1 or 0
                    }
                    
                    // Override/Set properties based on local DB if missing
                    if (dto.title == null) dto.title = dbm.getTitle();
                    if (dto.overview == null) dto.overview = dbm.getDescription();
                    if (dto.poster_path == null) dto.poster_path = dbm.getPosterPath();
                    
                    // If it's a local movie, set a generic genre for filtering
                    if (dto.genres == null) {
                        dto.genres = new java.util.ArrayList<>();
                        MovieDTO.GenreDTO g = new MovieDTO.GenreDTO();
                        g.name = dbm.getGenre();
                        dto.genres.add(g);
                    }
                    
                    allMovies.add(dto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(this::filterMovies);
            }
        }).start();
    }

    private void filterMovies() {
        if (allMovies == null) return;
        
        String searchQuery = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String selectedGenre = genreFilterCombo.getValue();
        
        List<MovieDTO> filtered = allMovies.stream()
            .filter(m -> {
                String mTitle = m.title != null ? m.title.toLowerCase() : "";
                boolean matchesSearch = mTitle.contains(searchQuery);
                boolean matchesGenre = "All Genres".equals(selectedGenre) || hasGenre(m, selectedGenre);
                return matchesSearch && matchesGenre;
            })
            .collect(Collectors.toList());
            
        populateGrid(filtered);
    }
    
    private boolean hasGenre(MovieDTO movie, String genreName) {
        if (movie.genres != null) {
            for (MovieDTO.GenreDTO g : movie.genres) {
                if (g.name != null && g.name.equalsIgnoreCase(genreName)) {
                    return true;
                }
            }
        }
        if (movie.genre_ids != null) {
            for (int id : movie.genre_ids) {
                String name = TMDBUtils.getGenreName(id);
                if (name != null && name.equalsIgnoreCase(genreName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void populateGrid(List<MovieDTO> movies) {
        moviesGrid.getChildren().clear();
        moviesGrid.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        
        if (movies == null || movies.isEmpty()) {
            moviesGrid.getChildren().add(new Label("No movies available right now."));
            return;
        }

        for (MovieDTO movie : movies) {
            VBox card = createMovieCard(movie);
            moviesGrid.getChildren().add(card);
        }
    }

    private VBox createMovieCard(MovieDTO movie) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-grid-card");
        card.setPrefWidth(350);
        card.setMinHeight(250);
        card.setSpacing(20);
        card.setPadding(new Insets(30));

        // Top Row: Genres and Rating
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        String genresStr = "GENRE";
        if (movie.genres != null && !movie.genres.isEmpty()) {
            genresStr = movie.genres.stream().map(g -> g.name != null ? g.name : "Unknown").limit(2).collect(Collectors.joining(", ")).toUpperCase();
        } else if (movie.genre_ids != null && !movie.genre_ids.isEmpty()) {
            genresStr = movie.genre_ids.stream().map(TMDBUtils::getGenreName).limit(2).collect(Collectors.joining(", ")).toUpperCase();
        }
            
        Label genreLabel = new Label(genresStr);
        genreLabel.setStyle("-fx-text-fill: #0d6efd; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", movie.vote_average));
        ratingLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        topRow.getChildren().addAll(genreLabel, spacer, ratingLabel);

        // Title
        Label titleLabel = new Label(movie.title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #212529;");
        titleLabel.setWrapText(true);

        // Subtitle (Duration / Halls) - TMDB now_playing doesn't give runtime without details call, so hardcode or fetch details
        Label subtitleLabel = new Label("120 mins • Multiple Halls");
        subtitleLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

        // Action Button Spacer (pushes button to bottom)
        Region buttonSpacer = new Region();
        VBox.setVgrow(buttonSpacer, Priority.ALWAYS);

        // Action Button
        Button detailsBtn = new Button("View Details");
        detailsBtn.getStyleClass().add("search-btn");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        
        models.Movie localMovieObj = getLocalMovieObj(movie.id, movie.title);
        detailsBtn.setOnAction(e -> openMovieDetails(localMovieObj));

        card.getChildren().addAll(topRow, titleLabel, subtitleLabel, buttonSpacer, detailsBtn);
        return card;
    }

    private models.Movie getLocalMovieObj(int tmdbId, String title) {
        models.MovieDAO dao = new models.MovieDAO();
        for (models.Movie m : dao.getActiveMovies()) {
            if (tmdbId > 0 && m.getTmdbId() == tmdbId) return m;
            if (tmdbId == -1 && title.equals(m.getTitle())) return m;
        }
        return null;
    }

    private void openMovieDetails(models.Movie localMovie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/MovieDetails.fxml"));
            Parent root = loader.load();
            
            MovieDetailsController controller = loader.getController();
            controller.setLocalMovie(localMovie);
            
            StackPane contentArea = (StackPane) searchField.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
