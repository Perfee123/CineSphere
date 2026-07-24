package controllers.scheduler;

import controllers.MainLayoutController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import models.Hall;
import models.HallDAO;
import models.Movie;
import models.ShowDAO;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ScheduleMovieController implements Initializable {

    @FXML private ImageView moviePoster;
    @FXML private Label movieTitle;
    @FXML private Label movieGenre;
    @FXML private Label movieDuration;
    @FXML private Label movieLicensedPeriod;

    @FXML private ComboBox<Hall> hallComboBox;
    @FXML private DatePicker dateField;
    @FXML private TextField timeField;
    @FXML private FlowPane datesGrid;
    @FXML private FlowPane timesGrid;
    @FXML private Label errorLabel;

    private Movie currentMovie;
    private HallDAO hallDAO = new HallDAO();
    private ShowDAO showDAO = new ShowDAO();
    private List<LocalDate> addedDates = new ArrayList<>();
    private List<String> addedTimes = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Hall> halls = hallDAO.getAllHalls();
        hallComboBox.setItems(FXCollections.observableArrayList(halls));
    }

    public void setMovie(Movie movie) {
        this.currentMovie = movie;
        movieTitle.setText(movie.getTitle());
        movieGenre.setText(movie.getGenre());
        movieDuration.setText(movie.getRuntime());
        movieLicensedPeriod.setText("Licensed: " + movie.getShowingFrom() + " to " + movie.getShowingUntil());

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {
                Image image = new Image(movie.getPosterPath(), true);
                moviePoster.setImage(image);
            } catch (Exception e) {
                System.out.println("Could not load image: " + movie.getPosterPath());
            }
        }
    }

    @FXML
    public void handleAddDate(ActionEvent event) {
        LocalDate selectedDate = dateField.getValue();
        if (selectedDate == null) return;
        
        try {
            LocalDate validStart = LocalDate.MIN;
            LocalDate validEnd = LocalDate.MAX;
            
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if (currentMovie.getShowingFrom() != null && !currentMovie.getShowingFrom().trim().isEmpty()) {
                    validStart = LocalDate.parse(currentMovie.getShowingFrom(), df);
                }
                if (currentMovie.getShowingUntil() != null && !currentMovie.getShowingUntil().trim().isEmpty()) {
                    validEnd = LocalDate.parse(currentMovie.getShowingUntil(), df);
                }
            } catch (Exception parseEx) {
                System.out.println("Warning: DB License dates could not be parsed. Proceeding without strict validation.");
            }
            
            if (selectedDate.isBefore(validStart) || selectedDate.isAfter(validEnd)) {
                showError("Date must be within the licensed period (" + currentMovie.getShowingFrom() + " to " + currentMovie.getShowingUntil() + ").");
                return;
            }
            
            if (addedDates.contains(selectedDate)) {
                showError("Date already added.");
                return;
            }

            hideError();
            addedDates.add(selectedDate);
            dateField.setValue(null);
            renderDates();
        } catch (Exception e) {
            e.printStackTrace();
            showError("An unexpected error occurred.");
        }
    }
    
    private void renderDates() {
        datesGrid.getChildren().clear();
        for (LocalDate d : addedDates) {
            HBox pill = new HBox(5);
            pill.setAlignment(Pos.CENTER);
            pill.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #bbf7d0; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 6 12;");
            
            Label dLbl = new Label(d.toString());
            dLbl.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
            
            Button removeBtn = new Button("×");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-padding: 0; -fx-font-size: 14px; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                addedDates.remove(d);
                renderDates();
            });
            
            pill.getChildren().addAll(dLbl, removeBtn);
            datesGrid.getChildren().add(pill);
        }
    }

    @FXML
    public void handleAddTime(ActionEvent event) {
        String timeStr = timeField.getText();
        if (timeStr == null || timeStr.trim().isEmpty()) return;

        if (!timeStr.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
            showError("Time must be in HH:mm format.");
            return;
        }
        
        // Pad single digit hour with zero for consistency
        String[] parts = timeStr.split(":");
        if (parts[0].length() == 1) {
            timeStr = "0" + timeStr;
        }
        
        if (addedTimes.contains(timeStr)) {
            showError("Time already added.");
            return;
        }

        hideError();
        addedTimes.add(timeStr);
        timeField.clear();
        renderTimes();
    }

    private void renderTimes() {
        timesGrid.getChildren().clear();
        for (String t : addedTimes) {
            HBox pill = new HBox(5);
            pill.setAlignment(Pos.CENTER);
            pill.setStyle("-fx-background-color: #dbeafe; -fx-border-color: #bfdbfe; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 6 12;");
            
            Label timeLbl = new Label(t);
            timeLbl.setStyle("-fx-text-fill: #1e3a8a; -fx-font-weight: bold;");
            
            Button removeBtn = new Button("×");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-padding: 0; -fx-font-size: 14px; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                addedTimes.remove(t);
                renderTimes();
            });
            
            pill.getChildren().addAll(timeLbl, removeBtn);
            timesGrid.getChildren().add(pill);
        }
    }

    @FXML
    public void handleGenerateSchedule(ActionEvent event) {
        Hall hall = hallComboBox.getValue();

        if (hall == null) {
            showError("Please select a hall.");
            return;
        }
        
        if (addedDates.isEmpty()) {
            showError("Please add at least one date.");
            return;
        }
        
        if (addedTimes.isEmpty()) {
            showError("Please add at least one showtime.");
            return;
        }

        // Conflict Detection
        int runtimeMins = 120; // Default
        try {
            runtimeMins = Integer.parseInt(currentMovie.getRuntime().replace(" mins", "").trim());
        } catch (Exception e) {}

        for (LocalDate d : addedDates) {
            for (String t : addedTimes) {
                LocalTime start = LocalTime.parse(t);
                LocalTime end = start.plusMinutes(runtimeMins);
                
                if (showDAO.isHallOccupied(hall.getId(), d.toString(), start.toString(), end.toString())) {
                    showError("Conflict Detected: Hall " + hall.getName() + " is already occupied on " + d.toString() + " between " + start.toString() + " and " + end.toString() + ". Please select different times or another hall.");
                    return;
                }
            }
        }

        hideError();
        int movieId = Integer.parseInt(currentMovie.getId().replace("M", ""));
        
        if (showDAO.addBatchShowsSpecificDates(movieId, hall.getId(), addedDates, addedTimes)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Shows generated successfully!");
            alert.showAndWait();
            handleBack(null);
        } else {
            showError("Failed to generate shows. Please check database connection.");
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/ShowScheduling.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
