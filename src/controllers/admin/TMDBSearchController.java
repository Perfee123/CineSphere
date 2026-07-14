package controllers.admin;

import controllers.MainLayoutController;
import controllers.ticket.MovieDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.MovieDTO;
import utils.TMDBUtils;

import java.io.IOException;
import java.util.List;

public class TMDBSearchController {

    @FXML private TextField searchField;
    @FXML private FlowPane resultsContainer;
    @FXML private Label statusLabel;
    
    private static String lastQuery = "";
    private static List<MovieDTO> lastResults = null;

    @FXML
    public void initialize() {
        if (!lastQuery.isEmpty()) {
            searchField.setText(lastQuery);
            if (lastResults != null) {
                statusLabel.setText("Restored " + lastResults.size() + " result(s).");
                for (MovieDTO movie : lastResults) {
                    resultsContainer.getChildren().add(createResultCard(movie));
                }
            }
        } else {
            statusLabel.setText("Search for a movie to see results here.");
        }
    }

    @FXML
    public void handleBack() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPageDirectly("/views/admin/MovieManagement.fxml");
        }
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        statusLabel.setText("Searching...");
        resultsContainer.getChildren().clear();

        new Thread(() -> {
            List<MovieDTO> results = TMDBUtils.searchMovies(query);
            
            Platform.runLater(() -> {
                lastQuery = query;
                lastResults = results;
                if (results.isEmpty()) {
                    statusLabel.setText("No results found for '" + query + "'.");
                } else {
                    statusLabel.setText("Found " + results.size() + " result(s) for '" + query + "'.");
                    for (MovieDTO movie : results) {
                        resultsContainer.getChildren().add(createResultCard(movie));
                    }
                }
            });
        }).start();
    }

    private VBox createResultCard(MovieDTO movie) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-background-radius: 15px; -fx-pref-width: 170px; -fx-pref-height: 310px;");
        card.setCursor(Cursor.HAND);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f1f3f5; -fx-border-color: transparent; -fx-background-radius: 15px; -fx-pref-width: 170px; -fx-pref-height: 310px;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-background-radius: 15px; -fx-pref-width: 170px; -fx-pref-height: 310px;"));

        card.setOnMouseClicked(e -> openMovieDetailsAddMode(movie));

        // Poster
        ImageView poster = new ImageView();
        poster.setFitWidth(150);
        poster.setFitHeight(225);
        poster.setPreserveRatio(false); // ensures the rectangle clip fits exactly
        
        // Clip to round image corners
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(150, 225);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        poster.setClip(clip);

        if (movie.poster_path != null && !movie.poster_path.isEmpty()) {
            String posterUrl = TMDBUtils.getImageUrl(movie.poster_path, "w185");
            poster.setImage(new Image(posterUrl, true));
        } else {
            poster.setStyle("-fx-border-color: #ccc;");
        }

        // Info
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER);
        
        Label titleLbl = new Label(movie.title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111111;");
        titleLbl.setWrapText(true);
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        titleLbl.setMinHeight(40);
        titleLbl.setMaxHeight(40);
        
        Label detailsLbl = new Label("⭐ " + movie.vote_average + "  ·  " + (movie.release_date != null && movie.release_date.length() >= 4 ? movie.release_date.substring(0, 4) : ""));
        detailsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #777777;");
        
        infoBox.getChildren().addAll(titleLbl, detailsLbl);
        
        card.getChildren().addAll(poster, infoBox);
        return card;
    }

    private void openMovieDetailsAddMode(MovieDTO dto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/MovieDetails.fxml"));
            Parent root = loader.load();
            
            MovieDetailsController controller = loader.getController();
            
            // We need an ADD_NEW mode in MovieDetailsController
            // We will set this via a new method we'll add to MovieDetailsController
            controller.setAddNewMode(true);
            
            // To reuse MovieDetailsController, we create a temporary Movie model with tmdbId
            models.Movie tempMovie = new models.Movie("", dto.title, "", "", "", null);
            tempMovie.setTmdbId(dto.id);
            controller.setLocalMovie(tempMovie);
            
            StackPane contentArea = (StackPane) resultsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
