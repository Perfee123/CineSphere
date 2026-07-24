package controllers.scheduler;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import models.Movie;
import models.MovieDAO;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PendingShowsController implements Initializable {

    @FXML private FlowPane moviesGrid;
    @FXML private javafx.scene.control.TextField searchField;
    private MovieDAO movieDAO = new MovieDAO();
    private List<Movie> pendingMovies;

    private String searchQuery = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshData();
        
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                searchQuery = newValue.toLowerCase();
                renderMovies();
            });
        }
    }

    private void refreshData() {
        pendingMovies = movieDAO.getPendingMovies();
        renderMovies();
    }

    private void renderMovies() {
        moviesGrid.getChildren().clear();
        
        if (pendingMovies == null || pendingMovies.isEmpty()) {
            VBox emptyState = new VBox();
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(100, 0, 0, 0));
            emptyState.prefWidthProperty().bind(moviesGrid.widthProperty());
            
            Label noPending = new Label("No pending shows right now.");
            noPending.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8;");
            emptyState.getChildren().add(noPending);
            
            moviesGrid.getChildren().add(emptyState);
            return;
        }
        
        for (Movie movie : pendingMovies) {
            if (searchQuery.isEmpty() || movie.getTitle().toLowerCase().contains(searchQuery)) {
                VBox card = createMovieCard(movie);
                moviesGrid.getChildren().add(card);
            }
        }
    }
    
    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-grid-card");
        card.setPrefWidth(350);
        card.setMinHeight(250);
        card.setSpacing(20);
        card.setPadding(new Insets(30));

        // Top Row: Genres and Rating
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label genreLabel = new Label(movie.getGenre().toUpperCase());
        genreLabel.setStyle("-fx-text-fill: #0d6efd; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", movie.getRating()));
        ratingLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        topRow.getChildren().addAll(genreLabel, spacer, ratingLabel);

        // Title
        Label titleLbl = new Label(movie.getTitle());
        titleLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #212529;");
        titleLbl.setWrapText(true);

        // Subtitle (Duration)
        Label durationLbl = new Label(movie.getRuntime() + " • Pending Schedule");
        durationLbl.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

        Region buttonSpacer = new Region();
        VBox.setVgrow(buttonSpacer, Priority.ALWAYS);

        Button scheduleBtn = new Button("Add Schedule");
        scheduleBtn.getStyleClass().add("search-btn");
        scheduleBtn.setMaxWidth(Double.MAX_VALUE);
        scheduleBtn.setOnAction(e -> handleAddSchedule(movie));

        card.getChildren().addAll(topRow, titleLbl, durationLbl, buttonSpacer, scheduleBtn);
        
        return card;
    }

    private void handleAddSchedule(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/scheduler/ScheduleMovie.fxml"));
            Parent root = loader.load();
            
            ScheduleMovieController controller = loader.getController();
            controller.setMovie(movie);
            
            StackPane contentArea = (StackPane) moviesGrid.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
