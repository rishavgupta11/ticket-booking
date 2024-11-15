package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {

    private ObjectMapper objectMapper = new ObjectMapper();
    private List<User> userList;
    private User user;
    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() {
        try {
            userList = objectMapper.readValue(new File(USER_FILE_PATH), new TypeReference<List<User>>() {});
        } catch (IOException e) {
            System.err.println("Error loading user list from file: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            userList = new ArrayList<>(); // Initialize to an empty list to avoid NullPointerException
        }
    }

    public Boolean loginUser () {
        Optional<User> foundUser  = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser .isPresent();
    }

    public Boolean signUp(User user1) {
        userList.add(user1);
        saveUserListToFile();
        return Boolean.TRUE;
    }

    private void saveUserListToFile() {
        try {
            File usersFile = new File(USER_FILE_PATH);
            objectMapper.writeValue(usersFile, userList);
        } catch (IOException e) {
            System.err.println("Error saving user list to file: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    public void fetchBookings() {
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        if (userFetched.isPresent()) {
            userFetched.get().printTickets();
        } else {
            System.out.println("No bookings found for user: " + user.getName());
        }
    }

    public Boolean cancelBooking(String ticketId) {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the ticket id to cancel");
        ticketId = s.next();

        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        String finalTicketId = ticketId;
        boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(finalTicketId));
        if (removed) {
            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            System.err.println("Error fetching trains: " + ex.getMessage());
            ex.printStackTrace(); // Print stack trace for debugging
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats (seats);
                    System.out.println("Seat booked successfully.");
                    return true;
                } else {
                    System.out.println("Seat is already booked.");
                    return false;
                }
            } else {
                System.out.println("Invalid seat selection.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error booking train seat: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return false;
        }
    }
}