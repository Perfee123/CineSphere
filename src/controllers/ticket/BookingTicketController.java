package controllers.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Movie;
import models.Showtime;

import java.util.Arrays;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class BookingTicketController {

    @FXML private TextField searchField;
    @FXML private ListView<Movie> movieListView;
    @FXML private VBox emptyStatePane;
    @FXML private BorderPane detailsPane;
    
    @FXML private Label movieTitleLabel;
    @FXML private Label movieMetaLabel;
    @FXML private Label movieDescLabel;
    @FXML private FlowPane timeSlotsPane;
    @FXML private Button selectSeatsBtn;

    private Showtime selectedShowtime = null;

    @FXML
    public void initialize() {
        // Load live movies from DB
        models.ShowDAO dao = new models.ShowDAO();
        ObservableList<Movie> movies = FXCollections.observableArrayList(dao.getActiveMoviesWithShowtimes());

        javafx.collections.transformation.FilteredList<Movie> filteredData = new javafx.collections.transformation.FilteredList<>(movies, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(movie -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();
                    return movie.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                           movie.getGenre().toLowerCase().contains(lowerCaseFilter);
                });
            });
        }
        movieListView.setItems(filteredData);

        // Custom ListCell for Movies
        movieListView.setCellFactory(lv -> new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);
                if (empty || movie == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                } else {
                    // Calculate total available seats for the movie
                    int availableSeats = movie.getShowtimes().stream().mapToInt(Showtime::getAvailableSeats).sum();

                    HBox box = new HBox(15);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    box.setPadding(new Insets(20, 20, 20, 20));
                    
                    // A sleek icon placeholder for the movie
                    Label iconLabel = new Label("🎬");
                    iconLabel.setStyle("-fx-font-size: 24px;");
                    
                    VBox textContainer = new VBox(5);
                    Label titleLabel = new Label(movie.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + (availableSeats > 0 ? "#212529;" : "#dc3545;"));
                    
                    String seatsText = availableSeats > 0 ? availableSeats + " Seats Left" : "Sold Out";
                    Label genreLabel = new Label(movie.getGenre() + " • " + seatsText);
                    genreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (availableSeats > 0 ? "#adb5bd;" : "#dc3545; -fx-font-weight: bold;"));
                    textContainer.getChildren().addAll(titleLabel, genreLabel);
                    
                    box.getChildren().addAll(iconLabel, textContainer);
                    
                    // We wrap the content inside an invisible background pane to simulate margins
                    StackPane wrapper = new StackPane(box);
                    wrapper.setPadding(new Insets(0, 0, 15, 0)); // bottom margin
                    
                    if (availableSeats == 0) {
                        box.getStyleClass().setAll("movie-list-item-sold-out");
                        box.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #ffe3e3; -fx-border-radius: 8; -fx-background-radius: 8;");
                    } else if (isSelected()) {
                        box.getStyleClass().setAll("movie-list-item-selected");
                    } else {
                        box.getStyleClass().setAll("movie-list-item");
                    }
                    setDisable(false); // Don't disable so we keep colors
                    
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    setGraphic(wrapper);
                }
            }
        });

        // Handle selection cleanly
        movieListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int available = newVal.getShowtimes().stream().mapToInt(Showtime::getAvailableSeats).sum();
                if (available > 0) {
                    showMovieDetails(newVal);
                } else {
                    javafx.application.Platform.runLater(() -> movieListView.getSelectionModel().clearSelection());
                }
            }
        });
    }

    private void showMovieDetails(Movie movie) {
        emptyStatePane.setVisible(false);
        emptyStatePane.setManaged(false);
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);

        movieTitleLabel.setText(movie.getTitle());
        movieMetaLabel.setText(movie.getGenre() + " • " + movie.getRuntime());
        movieDescLabel.setText(movie.getDescription());

        // Reset slot selection
        selectedShowtime = null;
        updateSelectSeatsButton();
        timeSlotsPane.getChildren().clear();

        // Populate time slots
        for (Showtime slot : movie.getShowtimes()) {
            Button slotBtn = new Button(slot.getTime() + " - " + slot.getHall());
            slotBtn.getStyleClass().add("time-slot-btn");
            slotBtn.setOnAction(e -> {
                selectedShowtime = slot;
                // Update styles to reflect active
                for (javafx.scene.Node node : timeSlotsPane.getChildren()) {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("time-slot-btn-active");
                        if (!node.getStyleClass().contains("time-slot-btn")) {
                            node.getStyleClass().add("time-slot-btn");
                        }
                    }
                }
                slotBtn.getStyleClass().remove("time-slot-btn");
                slotBtn.getStyleClass().add("time-slot-btn-active");
                updateSelectSeatsButton();
            });
            timeSlotsPane.getChildren().add(slotBtn);
        }
    }

    private void updateSelectSeatsButton() {
        if (selectedShowtime != null) {
            selectSeatsBtn.setDisable(false);
            selectSeatsBtn.getStyleClass().remove("primary-action-btn-disabled");
            if (!selectSeatsBtn.getStyleClass().contains("primary-action-btn")) {
                selectSeatsBtn.getStyleClass().add("primary-action-btn");
            }
        } else {
            selectSeatsBtn.setDisable(true);
            selectSeatsBtn.getStyleClass().remove("primary-action-btn");
            if (!selectSeatsBtn.getStyleClass().contains("primary-action-btn-disabled")) {
                selectSeatsBtn.getStyleClass().add("primary-action-btn-disabled");
            }
        }
    }

    @FXML
    public void handleSelectSeats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/SeatSelection.fxml"));
            Parent root = loader.load();
            
            SeatSelectionController controller = loader.getController();
            controller.setBookingData(selectedShowtime.getId(), movieTitleLabel.getText(), selectedShowtime.getTime() + " - " + selectedShowtime.getHall());
            
            StackPane contentArea = (StackPane) selectSeatsBtn.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
