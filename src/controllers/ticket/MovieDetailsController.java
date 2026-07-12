package controllers.ticket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import models.Movie;
import models.MovieDTO;
import utils.TMDBUtils;
import controllers.admin.EditMoviePricingDialogController;
import models.MovieDAO;

public class MovieDetailsController {

    @FXML
    private StackPane heroBanner;
    @FXML
    private ImageView posterImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label taglineLabel;
    @FXML
    private HBox genresBox;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label yearLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label popularityLabel;
    @FXML
    private Label overviewLabel;
    
    @FXML
    private javafx.scene.layout.VBox loadingOverlay;
    
    @FXML
    private javafx.scene.layout.VBox contentContainer;

    private boolean isAdminMode = false;
    private boolean isAddNewMode = false;
    private Movie localMovie;
    private int currentMovieId;
    private MovieDTO currentFetchedDto;
    private MovieDAO movieDAO = new MovieDAO();

    @FXML
    private Button actionButton; // We will replace the "Book Tickets" button with a dynamic one in FXML or just toggle visibility if we add both.

    @FXML
    public void initialize() {
        // Initialization if needed
    }

    public void setAdminMode(boolean admin) {
        this.isAdminMode = admin;
        this.isAddNewMode = false;
    }

    public void setAddNewMode(boolean addNew) {
        this.isAddNewMode = addNew;
        this.isAdminMode = false;
    }

    public void setLocalMovie(Movie movie) {
        this.localMovie = movie;
        
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            contentContainer.setVisible(false);
        }
        
        if (isAddNewMode && movie.getTmdbId() > 0) {
            this.currentMovieId = movie.getTmdbId();
            new Thread(() -> {
                try {
                    MovieDTO dto = TMDBUtils.getMovieDetails(currentMovieId);
                    this.currentFetchedDto = dto;
                    Platform.runLater(() -> {
                        try {
                            populateDetails(dto);
                            updateActionButtons();
                        } finally {
                            hideLoader();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(this::hideLoader);
                }
            }).start();
        } else {
            try {
                // Populate from local DB (already licensed or manually added)
                titleLabel.setText(movie.getTitle());
                overviewLabel.setText(movie.getDescription());
                durationLabel.setText("⏱ " + movie.getRuntime());
                
                // Local DB has single genre string, put in a label like TMDB does
                genresBox.getChildren().clear();
                if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
                    Label gLbl = new Label(movie.getGenre());
                    gLbl.setStyle("-fx-border-color: #0d6efd; -fx-text-fill: #0d6efd; -fx-border-radius: 15px; -fx-padding: 5 15;");
                    genresBox.getChildren().add(gLbl);
                }
                
                ratingLabel.setText(movie.getRating() > 0 ? "⭐ " + String.format("%.1f", movie.getRating()) : "⭐ N/A");
                yearLabel.setText(movie.getReleaseDate() != null ? movie.getReleaseDate() : "Unknown Year");
                taglineLabel.setText(movie.getTagline() != null && !movie.getTagline().isEmpty() ? "\"" + movie.getTagline() + "\"" : "");
                languageLabel.setText("🌐 EN"); // Default
                popularityLabel.setText("🔥 Popularity: " + String.format("%.1f", movie.getPopularity()));
                
                if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                    String posterUrl = (movie.getPosterPath().startsWith("http") || movie.getPosterPath().startsWith("file:")) ? movie.getPosterPath() : TMDBUtils.getImageUrl(movie.getPosterPath(), "w500");
                    posterImage.setImage(new Image(posterUrl, true));
                }
                if (movie.getBannerPath() != null && !movie.getBannerPath().isEmpty()) {
                    String bannerUrl = (movie.getBannerPath().startsWith("http") || movie.getBannerPath().startsWith("file:")) ? movie.getBannerPath() : TMDBUtils.getImageUrl(movie.getBannerPath(), "original");
                    heroBanner.setStyle("-fx-background-image: url('" + bannerUrl + "'); -fx-background-size: cover; -fx-background-position: center;");
                }
                updateActionButtons();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in setLocalMovie else block: " + e.getMessage());
            } finally {
                hideLoader();
            }
        }
    }

    private void hideLoader() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
            contentContainer.setVisible(true);
        }
    }

    public void setMovieId(int tmdbId) {
        this.currentMovieId = tmdbId;
        
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            contentContainer.setVisible(false);
        }
        
        new Thread(() -> {
            try {
                MovieDTO dto = TMDBUtils.getMovieDetails(tmdbId);
                this.currentFetchedDto = dto;
                Platform.runLater(() -> {
                    try {
                        populateDetails(dto);
                        updateActionButtons();
                    } finally {
                        hideLoader();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(this::hideLoader);
            }
        }).start();
    }

    private void updateActionButtons() {
        if (actionButton != null) {
            actionButton.getStyleClass().clear();
            actionButton.getStyleClass().add("primary-action-btn");
            
            // Clear any inline styles from previous states
            actionButton.setStyle("");
            actionButton.setDisable(false);

            if (isAddNewMode) {
                if (currentFetchedDto != null && movieDAO.isMovieExistsByTmdbId(currentFetchedDto.id)) {
                    actionButton.setText("Already Added");
                    actionButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                    actionButton.setDisable(true);
                } else {
                    actionButton.setText("+ Add to Theater");
                    actionButton.setOnAction(e -> handleAddToTheater());
                }
            } else if (isAdminMode) {
                actionButton.setText("Edit Dates & Pricing");
                actionButton.setOnAction(e -> handleEditPricing());
            } else {
                actionButton.setText("+ Book Tickets");
                actionButton.setOnAction(e -> handleBookTickets());
            }
        }
    }

    private void handleAddToTheater() {
        if (currentFetchedDto == null) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Movie details not fully loaded yet.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditMoviePricingDialog.fxml"));
            Parent root = loader.load();
            EditMoviePricingDialogController controller = loader.getController();
            
            // Pass the DTO instead of saving it first
            controller.initDataForNewTMDB(currentFetchedDto);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Edit Dates & Pricing");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Only navigate back if the user successfully saved
            if (controller.saveSuccessful) {
                if (controllers.MainLayoutController.getInstance() != null) {
                    controllers.MainLayoutController.getInstance().loadPageDirectly("/views/admin/MovieManagement.fxml");
                }
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    private void populateDetails(MovieDTO movie) {
        if (movie == null) {
            titleLabel.setText("Error loading details");
            return;
        }

        titleLabel.setText(movie.title);
        taglineLabel.setText(movie.tagline != null && !movie.tagline.isEmpty() ? "\"" + movie.tagline + "\"" : "");
        ratingLabel.setText(String.format("⭐ %.1f", movie.vote_average));

        if (movie.release_date != null && movie.release_date.length() >= 4) {
            yearLabel.setText(movie.release_date.substring(0, 4));
        }

        languageLabel.setText(movie.original_language != null ? movie.original_language.toUpperCase() : "EN");
        durationLabel.setText("⏱ " + movie.runtime + " mins");
        popularityLabel.setText("Popularity: " + movie.popularity + " • Released");
        overviewLabel.setText(movie.overview);

        // Genres
        genresBox.getChildren().clear();
        if (movie.genres != null) {
            for (MovieDTO.GenreDTO genre : movie.genres) {
                Label gLabel = new Label(genre.name);
                gLabel.getStyleClass().add("genre-badge");
                genresBox.getChildren().add(gLabel);
            }
        }

        // Images
        if (movie.backdrop_path != null) {
            String imageUrl = movie.backdrop_path.startsWith("http") ? movie.backdrop_path : TMDBUtils.getImageUrl(movie.backdrop_path, "w1280");
            heroBanner.setStyle("-fx-background-image: url('" + imageUrl + "'); "
                    + "-fx-background-size: cover; "
                    + "-fx-background-position: center 25%;");
        }

        if (movie.poster_path != null) {
            String posterUrl = movie.poster_path.startsWith("http") ? movie.poster_path : TMDBUtils.getImageUrl(movie.poster_path, "w500");
            Image poster = new Image(posterUrl, true);
            posterImage.setImage(poster);
        }
    }

    public void handleEditPricing() {
        if (localMovie != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditMoviePricingDialog.fxml"));
                Parent root = loader.load();

                EditMoviePricingDialogController controller = loader.getController();
                controller.initData(localMovie);

                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Edit Dates & Pricing");
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.setScene(new javafx.scene.Scene(root));
                stage.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleBookTickets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingTicket.fxml"));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) titleLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBack() {
        try {
            String viewPath = "/views/ticket/NowShowing.fxml";
            if (isAddNewMode) {
                viewPath = "/views/admin/TMDBSearch.fxml";
            } else if (isAdminMode) {
                viewPath = "/views/admin/MovieManagement.fxml";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) titleLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
