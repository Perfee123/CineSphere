package controllers.admin;

import controllers.MainLayoutController;
import controllers.ticket.MovieDetailsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import models.Movie;
import models.MovieDAO;
import utils.TMDBUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class MovieManagementController {

    @FXML private VBox moviesListContainer;
    private MovieDAO movieDAO = new MovieDAO();

    @FXML
    public void initialize() {
        loadMovies();
    }

    private void loadMovies() {
        moviesListContainer.getChildren().clear();
        
        atlantafx.base.controls.RingProgressIndicator loader = new atlantafx.base.controls.RingProgressIndicator();
        loader.setProgress(-1); // Indeterminate
        
        javafx.scene.control.Label waitLbl = new javafx.scene.control.Label("Please wait, loading movies...");
        waitLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        
        VBox loaderContainer = new VBox(15);
        loaderContainer.getChildren().addAll(loader, waitLbl);
        loaderContainer.setAlignment(Pos.CENTER);
        loaderContainer.setPadding(new Insets(100, 0, 0, 0));
        moviesListContainer.getChildren().add(loaderContainer);
        
        new Thread(() -> {
            try {
                List<Movie> movies = movieDAO.getActiveMovies();
                
                javafx.application.Platform.runLater(() -> {
                    moviesListContainer.getChildren().clear();
                    
                    if (movies == null || movies.isEmpty()) {
                        javafx.scene.control.Label noMoviesLabel = new javafx.scene.control.Label("No movies licensed yet.");
                        noMoviesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #6c757d; -fx-padding: 50;");
                        moviesListContainer.setAlignment(Pos.CENTER);
                        moviesListContainer.getChildren().add(noMoviesLabel);
                    } else {
                        moviesListContainer.setAlignment(Pos.TOP_LEFT);
                        for (Movie movie : movies) {
                            HBox row = createMovieRow(movie);
                            moviesListContainer.getChildren().add(row);
                        }
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    moviesListContainer.getChildren().clear();
                    javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Error loading movies. Please try again.");
                    errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #dc3545; -fx-padding: 50;");
                    moviesListContainer.setAlignment(Pos.CENTER);
                    moviesListContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private HBox createMovieRow(Movie movie) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 0);");

        // Click on row to edit details
        row.setOnMouseClicked(e -> {
            openMovieDetailsAdmin(movie);
        });
        row.setCursor(Cursor.HAND);

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 2);"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 0);"));

        // Poster
        ImageView poster = new ImageView();
        poster.setFitWidth(50);
        poster.setFitHeight(75);
        poster.setPreserveRatio(true);
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            String posterUrl = (movie.getPosterPath().startsWith("http") || movie.getPosterPath().startsWith("file:")) ? movie.getPosterPath() : TMDBUtils.getImageUrl(movie.getPosterPath(), "w500");
            poster.setImage(new Image(posterUrl, true));
        } else if (movie.getTmdbId() > 0) {
            // Fetch poster asynchronously if not in local DB but has TMDB ID
            new Thread(() -> {
                models.MovieDTO dto = TMDBUtils.getMovieDetails(movie.getTmdbId());
                if (dto != null && dto.poster_path != null && !dto.poster_path.isEmpty()) {
                    String posterUrl = (dto.poster_path.startsWith("http") || dto.poster_path.startsWith("file:")) ? dto.poster_path : TMDBUtils.getImageUrl(dto.poster_path, "w500");
                    javafx.application.Platform.runLater(() -> poster.setImage(new Image(posterUrl, true)));
                    
                    // Optional: update the local database so we don't have to fetch next time
                    movie.setPosterPath(dto.poster_path);
                    movie.setBannerPath(dto.backdrop_path);
                    // We'd need an update query for this, but for now just show it in UI
                }
            }).start();
        } else {
            // Placeholder logic
            poster.setStyle("-fx-border-color: #ccc;");
        }
        
        // Info
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(movie.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111111;");
        titleBox.getChildren().add(titleLbl);
        
        if (movie.getTmdbId() > 0) {
            Label tmdbBadge = new Label("TMDB");
            tmdbBadge.setStyle("-fx-background-color: #032541; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 4px;");
            titleBox.getChildren().add(tmdbBadge);
        }

        Label dateLbl = new Label("Showing: " + movie.getShowingFrom() + " - " + movie.getShowingUntil());
        dateLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");
        
        infoBox.getChildren().addAll(titleBox, dateLbl);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Days Left
        Label daysLeftLbl = new Label();
        daysLeftLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        if (movie.getShowingUntil() != null) {
            try {
                LocalDate until = LocalDate.parse(movie.getShowingUntil());
                long days = ChronoUnit.DAYS.between(LocalDate.now(), until);
                if (days < 0) {
                    daysLeftLbl.setText("Expired");
                    daysLeftLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
                } else {
                    daysLeftLbl.setText(days + " days left");
                    daysLeftLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
                }
            } catch (Exception e) {
                daysLeftLbl.setText("N/A");
            }
        }
        
        // Delete Button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #ffeef0; -fx-text-fill: #dc3545; -fx-font-size: 14px; -fx-background-radius: 50%; -fx-cursor: hand; -fx-min-width: 35px; -fx-min-height: 35px; -fx-padding: 0;");
        deleteBtn.setOnAction(e -> {
            e.consume(); // Prevent row click
            handleDelete(movie);
        });

        row.getChildren().addAll(poster, infoBox, spacer, daysLeftLbl, deleteBtn);
        return row;
    }

    private void handleDelete(Movie movie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Movie: " + movie.getTitle());
        alert.setContentText("Are you sure you want to permanently delete this movie and all its scheduled shows?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (movieDAO.deleteMovie(movie.getId())) {
                loadMovies();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete movie.");
                err.showAndWait();
            }
        }
    }

    private void openMovieDetailsAdmin(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/MovieDetails.fxml"));
            Parent root = loader.load();
            
            MovieDetailsController controller = loader.getController();
            controller.setAdminMode(true);
            controller.setLocalMovie(movie);
            
            StackPane contentArea = (StackPane) moviesListContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onAddNewMovie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/AddNewMovieTypeDialog.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Add New Movie");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
