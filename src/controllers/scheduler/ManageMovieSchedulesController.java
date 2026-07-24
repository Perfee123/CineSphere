package controllers.scheduler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Movie;
import models.ShowDAO;
import models.Showtime;
import models.MovieDAO;

import java.io.IOException;
import java.util.Optional;

public class ManageMovieSchedulesController {

    @FXML private Label movieTitleLabel;
    @FXML private FlowPane schedulesGrid;

    private Movie movie;
    private ShowDAO showDAO = new ShowDAO();
    private MovieDAO movieDAO = new MovieDAO();

    public void setMovie(Movie movie) {
        this.movie = movie;
        movieTitleLabel.setText(movie.getTitle());
        loadData();
    }

    private void loadData() {
        schedulesGrid.getChildren().clear();
        
        if (movie.getShowtimes() == null || movie.getShowtimes().isEmpty()) {
            VBox emptyState = new VBox();
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(100, 0, 0, 0));
            emptyState.prefWidthProperty().bind(schedulesGrid.widthProperty());
            
            Label noShows = new Label("No active schedules for this movie.");
            noShows.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8;");
            emptyState.getChildren().add(noShows);
            
            schedulesGrid.getChildren().add(emptyState);
            return;
        }

        for (Showtime st : movie.getShowtimes()) {
            VBox card = createScheduleCard(st);
            schedulesGrid.getChildren().add(card);
        }
    }
    
    private VBox createScheduleCard(Showtime st) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-grid-card");
        card.setPrefWidth(300);
        card.setMinHeight(200);
        card.setSpacing(15);
        card.setPadding(new Insets(25));
        
        // Header (Date & Time)
        Label dateLbl = new Label(st.getRawDate());
        dateLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label timeLbl = new Label(st.getRawTime());
        timeLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0ea5e9;");
        
        HBox header = new HBox(dateLbl);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer, timeLbl);
        
        // Body (Hall & Seats)
        VBox details = new VBox(5);
        Label hallLbl = new Label("Hall: " + st.getHall());
        hallLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        
        Label seatsLbl = new Label("Seats Available: " + st.getAvailableSeats() + " / " + st.getTotalSeats());
        seatsLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        details.getChildren().addAll(hallLbl, seatsLbl);
        
        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        
        // Action Button
        Button cancelBtn = new Button("Cancel Show");
        cancelBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> handleCancelShow(st));
        
        card.getChildren().addAll(header, details, vSpacer, cancelBtn);
        return card;
    }

    private void handleCancelShow(Showtime st) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Show");
        alert.setHeaderText("Cancel show on " + st.getRawDate() + " at " + st.getRawTime() + "?");
        alert.setContentText("Are you sure you want to cancel and delete this show? This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int showId = Integer.parseInt(st.getId().replace("SH-", ""));
            if (showDAO.deleteShow(showId)) {
                // Refresh the movie's showtimes from DB
                for (Movie m : showDAO.getAllActiveMoviesWithShows()) {
                    if (m.getId().equals(movie.getId())) {
                        this.movie = m;
                        loadData();
                        return;
                    }
                }
                // If movie has no shows left, it won't be in the list, so clear shows
                this.movie.getShowtimes().clear();
                loadData();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to cancel show. It may have active bookings.");
                err.showAndWait();
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/scheduler/ShowScheduling.fxml"));
            Parent root = loader.load();
            
            StackPane contentArea = (StackPane) movieTitleLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/scheduler/ScheduleMovie.fxml"));
            Parent root = loader.load();
            
            ScheduleMovieController controller = loader.getController();
            controller.setMovie(this.movie); // Pass the current movie to ScheduleMovie
            
            StackPane contentArea = (StackPane) movieTitleLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
