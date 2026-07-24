package models;

import utils.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShowDAO {

    public List<ShowTableItem> getTodayShows() {
        List<ShowTableItem> shows = new ArrayList<>();
        String sql = "SELECT s.id as show_id, m.title as movie_title, h.name as hall_name, " +
                     "DATE_FORMAT(s.show_time, '%H:%i') as show_time, h.total_seats, " +
                     "(SELECT COUNT(*) FROM booking_seats bs JOIN bookings b ON bs.booking_id = b.id WHERE b.show_id = s.id AND b.status != 'CANCELLED') as booked_seats, " +
                     "s.status " +
                     "FROM shows s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN halls h ON s.hall_id = h.id " +
                     "WHERE s.show_date = CURDATE()";
                     
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                String showId = "SH-" + rs.getInt("show_id");
                String movieTitle = rs.getString("movie_title");
                String hall = rs.getString("hall_name");
                String time = rs.getString("show_time");
                int totalSeats = rs.getInt("total_seats");
                int bookedSeats = rs.getInt("booked_seats");
                String seats = bookedSeats + "/" + totalSeats;
                
                String status = "Available";
                if ("CANCELLED".equals(rs.getString("status"))) {
                    status = "Cancelled";
                } else if (bookedSeats >= totalSeats) {
                    status = "Fully Booked";
                }
                
                String rawTime = rs.getString("show_time"); // HH:mm
                int hour = 0;
                try {
                    hour = Integer.parseInt(rawTime.split(":")[0]);
                } catch (Exception e) {}
                
                String period = "Morning";
                if (hour >= 12 && hour < 17) period = "Afternoon";
                else if (hour >= 17 && hour < 20) period = "Evening";
                else if (hour >= 20) period = "Night";
                
                shows.add(new ShowTableItem(showId, movieTitle, hall, time, seats, status, period));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return shows;
    }

    public List<ShowTableItem> getUpcomingShows() {
        List<ShowTableItem> shows = new ArrayList<>();
        String sql = "SELECT s.id as show_id, m.title as movie_title, h.name as hall_name, " +
                     "DATE_FORMAT(s.show_time, '%H:%i') as show_time, h.total_seats, " +
                     "(SELECT COUNT(*) FROM booking_seats bs JOIN bookings b ON bs.booking_id = b.id WHERE b.show_id = s.id AND b.status != 'CANCELLED') as booked_seats, " +
                     "s.status " +
                     "FROM shows s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN halls h ON s.hall_id = h.id " +
                     "WHERE s.show_date >= CURDATE()";
                     
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                String showId = "SH-" + rs.getInt("show_id");
                String movieTitle = rs.getString("movie_title");
                String hall = rs.getString("hall_name");
                String time = rs.getString("show_time");
                int totalSeats = rs.getInt("total_seats");
                int bookedSeats = rs.getInt("booked_seats");
                String seats = bookedSeats + "/" + totalSeats;
                
                String status = "Available";
                if ("CANCELLED".equals(rs.getString("status"))) {
                    status = "Cancelled";
                } else if (bookedSeats >= totalSeats) {
                    status = "Fully Booked";
                }
                
                String rawTime = rs.getString("show_time");
                int hour = 0;
                try {
                    hour = Integer.parseInt(rawTime.split(":")[0]);
                } catch (Exception e) {}
                
                String period = "Morning";
                if (hour >= 12 && hour < 17) period = "Afternoon";
                else if (hour >= 17 && hour < 20) period = "Evening";
                else if (hour >= 20) period = "Night";
                
                shows.add(new ShowTableItem(showId, movieTitle, hall, time, seats, status, period));
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
                     "ORDER BY m.id, s.show_time";
                     
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

    public List<Movie> getAllActiveMoviesWithShows() {
        Map<Integer, Movie> movieMap = new LinkedHashMap<>();
        
        String sql = "SELECT m.id as movie_id, m.title, m.genre, m.duration_minutes, m.description, m.poster_path, m.rating, " +
                     "s.id as show_id, DATE_FORMAT(s.show_date, '%d/%m/%Y') as show_date, DATE_FORMAT(s.show_time, '%H:%i') as show_time, s.status as show_status, " +
                     "h.name as hall_name, h.total_seats, " +
                     "(SELECT COUNT(*) FROM booking_seats bs JOIN bookings b ON bs.booking_id = b.id WHERE b.show_id = s.id AND b.status != 'CANCELLED') as booked_seats " +
                     "FROM movies m " +
                     "LEFT JOIN shows s ON m.id = s.movie_id " +
                     "LEFT JOIN halls h ON s.hall_id = h.id " +
                     "WHERE m.status = 'ACTIVE' " +
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
                    movie = new Movie("M" + movieId, title, genre, runtime, description, new ArrayList<>());
                    movie.setPosterPath(rs.getString("poster_path"));
                    movie.setRating(rs.getDouble("rating"));
                    movieMap.put(movieId, movie);
                }
                
                int showIdInt = rs.getInt("show_id");
                if (!rs.wasNull()) {
                    String showStatus = rs.getString("show_status");
                    // Skip cancelled shows
                    if ("CANCELLED".equals(showStatus)) {
                        continue;
                    }

                    String showId = "SH-" + showIdInt;
                    String date = rs.getString("show_date");
                    String time = rs.getString("show_time");
                    String hall = rs.getString("hall_name");
                    int totalSeats = rs.getInt("total_seats");
                    int bookedSeats = rs.getInt("booked_seats");
                    int availableSeats = totalSeats - bookedSeats;

                    // Format display time as "dd/MM HH:mm" for better UI
                    String displayTime = date.substring(0, 5) + " " + time;
                    Showtime st = new Showtime(showId, displayTime, hall, availableSeats, totalSeats);
                    st.setRawDate(date);
                    st.setRawTime(time);

                    movie.getShowtimes().add(st);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return new ArrayList<>(movieMap.values());
    }

    public boolean addShow(int movieId, int hallId, String date, String time, String status) {
        String sql = "INSERT INTO shows (movie_id, hall_id, show_date, show_time, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, movieId);
            stmt.setInt(2, hallId);
            stmt.setString(3, date);
            stmt.setString(4, time);
            stmt.setString(5, status);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isHallOccupied(int hallId, String date, String start, String end) {
        String sql = "SELECT COUNT(*) FROM shows s " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "WHERE s.hall_id = ? AND s.show_date = ? AND s.status != 'CANCELLED' " +
                     "AND ( " +
                     "  (s.show_time <= ? AND ADDTIME(s.show_time, SEC_TO_TIME(m.duration_minutes * 60)) > ?) " +
                     "  OR " +
                     "  (s.show_time >= ? AND s.show_time < ?) " +
                     ")";
                     
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hallId);
            stmt.setString(2, date);
            stmt.setString(3, start); // new start
            stmt.setString(4, start); // new start
            stmt.setString(5, start); // new start
            stmt.setString(6, end);   // new end
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addBatchShowsSpecificDates(int movieId, int hallId, List<java.time.LocalDate> dates, List<String> times) {
        String sql = "INSERT INTO shows (movie_id, hall_id, show_date, show_time, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (java.time.LocalDate d : dates) {
                    for (String time : times) {
                        stmt.setInt(1, movieId);
                        stmt.setInt(2, hallId);
                        stmt.setString(3, d.toString()); // YYYY-MM-DD
                        stmt.setString(4, time + ":00"); // HH:mm:00
                        stmt.setString(5, "SCHEDULED");
                        stmt.addBatch();
                    }
                }

                stmt.executeBatch();
                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean updateShow(int showId, int hallId, String date, String time, String status) {
        String sql = "UPDATE shows SET hall_id = ?, show_date = ?, show_time = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hallId);
            stmt.setString(2, date);
            stmt.setString(3, time);
            stmt.setString(4, status);
            stmt.setInt(5, showId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteShow(int showId) {
        String sql = "DELETE FROM shows WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, showId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
