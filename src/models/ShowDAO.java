package models;

import utils.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShowDAO {

    public List<ShowTableItem> getUpcomingShows() {
        return getShowsByDateFilter("s.show_date >= CURDATE()", true);
    }

    public List<ShowTableItem> getTodayShows() {
        return getShowsByDateFilter("s.show_date = CURDATE()", false);
    }

    private List<ShowTableItem> getShowsByDateFilter(String dateFilter, boolean includeDateInTime) {
        List<ShowTableItem> shows = new ArrayList<>();
        String sql = "SELECT s.id as show_id, m.title as movie_title, h.name as hall_name, " +
                     "DATE_FORMAT(s.show_date, '%d/%m/%Y') as show_date, DATE_FORMAT(s.show_time, '%H:%i') as show_time, h.total_seats, " +
                     "(SELECT COUNT(*) FROM booking_seats bs JOIN bookings b ON bs.booking_id = b.id WHERE b.show_id = s.id AND b.status != 'CANCELLED') as booked_seats, " +
                     "s.status " +
                     "FROM shows s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN halls h ON s.hall_id = h.id " +
                     "WHERE " + dateFilter + " " +
                     "ORDER BY s.show_date, s.show_time";
                     
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                String showId = "SH-" + rs.getInt("show_id");
                String movieTitle = rs.getString("movie_title");
                String hall = rs.getString("hall_name");
                String time = includeDateInTime ? rs.getString("show_date") + " " + rs.getString("show_time") : rs.getString("show_time");
                int totalSeats = rs.getInt("total_seats");
                int bookedSeats = rs.getInt("booked_seats");
                String seats = bookedSeats + "/" + totalSeats;
                
                String status = "Available";
                if ("CANCELLED".equals(rs.getString("status"))) {
                    status = "Cancelled";
                } else if (bookedSeats >= totalSeats) {
                    status = "Fully Booked";
                }
                
                shows.add(new ShowTableItem(showId, movieTitle, hall, time, seats, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return shows;
    }

    public List<Movie> getActiveMoviesWithShowtimes() {
        Map<Integer, Movie> movieMap = new LinkedHashMap<>();
        
        String sql = "SELECT m.id as movie_id, m.title, m.genre, m.duration_minutes, m.description, " +
                     "s.id as show_id, DATE_FORMAT(s.show_time, '%H:%i') as show_time, h.name as hall_name, h.total_seats, " +
                     "(SELECT COUNT(*) FROM booking_seats bs JOIN bookings b ON bs.booking_id = b.id WHERE b.show_id = s.id AND b.status != 'CANCELLED') as booked_seats " +
                     "FROM movies m " +
                     "JOIN shows s ON m.id = s.movie_id " +
                     "JOIN halls h ON s.hall_id = h.id " +
                     "WHERE s.show_date >= CURDATE() AND s.status = 'SCHEDULED' " +
                     "ORDER BY m.id, s.show_date, s.show_time";
                     
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                
                Movie movie = movieMap.get(movieId);
                if (movie == null) {
                    String title = rs.getString("title");
                    String genre = rs.getString("genre");
                    String runtime = rs.getInt("duration_minutes") + " mins";
                    String description = rs.getString("description");
                    movie = new Movie(String.valueOf(movieId), title, genre, runtime, description, new ArrayList<>());
                    movieMap.put(movieId, movie);
                }
                
                String showId = "SH-" + rs.getInt("show_id");
                String time = rs.getString("show_time");
                String hall = rs.getString("hall_name");
                int totalSeats = rs.getInt("total_seats");
                int bookedSeats = rs.getInt("booked_seats");
                int availableSeats = totalSeats - bookedSeats;
                
                Showtime showtime = new Showtime(showId, time, hall, availableSeats, totalSeats);
                movie.getShowtimes().add(showtime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return new ArrayList<>(movieMap.values());
    }
}
