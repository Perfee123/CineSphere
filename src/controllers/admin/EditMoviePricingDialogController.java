package controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Movie;
import models.MovieDAO;

import java.time.LocalDate;

public class EditMoviePricingDialogController {

    @FXML private DatePicker showingFromPicker;
    @FXML private DatePicker showingUntilPicker;
    @FXML private TextField adultPriceField;
    @FXML private TextField kidsPriceField;

    private Movie movie;
    private models.MovieDTO unsavedMovieDto;
    public boolean saveSuccessful = false;
    private MovieDAO movieDAO = new MovieDAO();

    public void initData(Movie movie) {
        this.movie = movie;
        
        if (movie.getShowingFrom() != null && !movie.getShowingFrom().isEmpty()) {
            try { showingFromPicker.setValue(LocalDate.parse(movie.getShowingFrom())); } catch (Exception e) {}
        } else {
            showingFromPicker.setValue(LocalDate.now());
        }
        
        if (movie.getShowingUntil() != null && !movie.getShowingUntil().isEmpty()) {
            try { showingUntilPicker.setValue(LocalDate.parse(movie.getShowingUntil())); } catch (Exception e) {}
        } else {
            showingUntilPicker.setValue(LocalDate.now().plusMonths(2));
        }
        
        if (movie.getAdultPrice() > 0) {
            adultPriceField.setText(String.valueOf(movie.getAdultPrice()));
        } else {
            adultPriceField.setText("350.0");
        }
        
        if (movie.getKidsPrice() > 0) {
            kidsPriceField.setText(String.valueOf(movie.getKidsPrice()));
        } else {
            kidsPriceField.setText("200.0");
        }
    }

    public void initDataForNewTMDB(models.MovieDTO dto) {
        this.unsavedMovieDto = dto;
        showingFromPicker.setValue(LocalDate.now());
        showingUntilPicker.setValue(LocalDate.now().plusMonths(2));
        adultPriceField.setText("350.0");
        kidsPriceField.setText("200.0");
    }

    @FXML
    public void handleSave() {
        if (showingFromPicker.getValue() == null || showingUntilPicker.getValue() == null ||
            adultPriceField.getText().isEmpty() || kidsPriceField.getText().isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields.");
            alert.showAndWait();
            return;
        }

        try {
            double adultP = Double.parseDouble(adultPriceField.getText());
            double kidsP = Double.parseDouble(kidsPriceField.getText());
            
            if (unsavedMovieDto != null) {
                // We are adding a new TMDB movie
                Movie createdMovie = movieDAO.createMovie(unsavedMovieDto);
                if (createdMovie != null) {
                    createdMovie.setShowingFrom(showingFromPicker.getValue().toString());
                    createdMovie.setShowingUntil(showingUntilPicker.getValue().toString());
                    createdMovie.setAdultPrice(adultP);
                    createdMovie.setKidsPrice(kidsP);
                    if (movieDAO.updateMoviePricing(createdMovie)) {
                        this.saveSuccessful = true;
                        Stage stage = (Stage) adultPriceField.getScene().getWindow();
                        stage.close();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to add movie to database.");
                    alert.showAndWait();
                }
            } else {
                // Updating an existing movie
                movie.setShowingFrom(showingFromPicker.getValue().toString());
                movie.setShowingUntil(showingUntilPicker.getValue().toString());
                movie.setAdultPrice(adultP);
                movie.setKidsPrice(kidsP);

                if (movieDAO.updateMoviePricing(movie)) {
                    this.saveSuccessful = true;
                    Stage stage = (Stage) adultPriceField.getScene().getWindow();
                    stage.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update movie details in database.");
                    alert.showAndWait();
                }
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid price format.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) adultPriceField.getScene().getWindow();
        stage.close();
    }
}
