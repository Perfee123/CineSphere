package controllers.scheduler;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Hall;
import models.HallDAO;
import models.Movie;
import models.MovieDAO;
import models.ShowDAO;
import models.Showtime;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ShowSchedulingController implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane moviesGrid;

    private ShowDAO showDAO = new ShowDAO();
    private MovieDAO movieDAO = new MovieDAO();
    
    private List<Movie> allMovies;
    private String currentFilter = "ALL";

    private String searchQuery = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshData();
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchQuery = newValue.toLowerCase();
            renderMovies();
        });
    }

    private void refreshData() {
        allMovies = showDAO.getAllActiveMoviesWithShows();
        renderMovies();
    }

    private void renderMovies() {
        moviesGrid.getChildren().clear();
        
        List<Movie> filteredMovies = allMovies.stream().filter(m -> {
            boolean hasShows = !m.getShowtimes().isEmpty();
            boolean matchesSearch = m.getTitle().toLowerCase().contains(searchQuery);
            return hasShows && matchesSearch;
        }).collect(Collectors.toList());
        
        if (filteredMovies.isEmpty()) {
            VBox emptyState = new VBox();
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(100, 0, 0, 0));
            emptyState.prefWidthProperty().bind(moviesGrid.widthProperty());
            
            Label noShows = new Label("No shows scheduled yet.");
            noShows.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8;");
            emptyState.getChildren().add(noShows);
            
            moviesGrid.getChildren().add(emptyState);
            return;
        }
        
        for (Movie movie : filteredMovies) {
            VBox card = createMovieCard(movie);
            moviesGrid.getChildren().add(card);
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
        Label durationLbl = new Label(movie.getRuntime());
        durationLbl.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

        // Schedule Pills (TimesPane)
        FlowPane timesPane = new FlowPane();
        timesPane.setHgap(8);
        timesPane.setVgap(8);
        
        if (!movie.getShowtimes().isEmpty()) {
            int maxPills = Math.min(movie.getShowtimes().size(), 4);
            for (int i = 0; i < maxPills; i++) {
                Label timePill = new Label(movie.getShowtimes().get(i).getTime());
                timePill.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 4 10;");
                timesPane.getChildren().add(timePill);
            }
        } else {
            Label noShows = new Label("No shows scheduled yet.");
            noShows.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 12px;");
            timesPane.getChildren().add(noShows);
        }
        
        Region buttonSpacer = new Region();
        VBox.setVgrow(buttonSpacer, Priority.ALWAYS);

        Button manageBtn = new Button("Edit Schedule");
        if (!movie.getShowtimes().isEmpty() && movie.getShowtimes().size() > 4) {
             manageBtn.setText("Edit Schedule (+" + (movie.getShowtimes().size() - 4) + ")");
        }
        manageBtn.getStyleClass().add("action-btn");
        manageBtn.setMaxWidth(Double.MAX_VALUE);
        manageBtn.setOnAction(e -> handleManageSchedules(movie));

        card.getChildren().addAll(topRow, titleLbl, durationLbl, timesPane, buttonSpacer, manageBtn);
        return card;
    }



    private void handleAddSchedule(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/scheduler/ScheduleMovie.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ScheduleMovieController controller = loader.getController();
            controller.setMovie(movie);
            
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) moviesGrid.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    
    private void handleManageSchedules(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/scheduler/ManageMovieSchedules.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ManageMovieSchedulesController controller = loader.getController();
            controller.setMovie(movie);
            
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) moviesGrid.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // Removed old filter logic
}
