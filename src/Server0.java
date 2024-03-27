import java.util.ArrayList;
import java.util.List;

public class Server1 {
    private List<User> users;
    private User admin;

    public Server1() {
        users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
        System.out.println("User " + user.getId() + " joined the server.");
        if (admin == null || user.getJoinTime() < admin.getJoinTime()) {
            admin = user;
            System.out.println("User " + user.getId() + " has been assigned as admin.");
        }
    }

    public void removeUser(User user) {
        users.remove(user);
        System.out.println("User " + user.getId() + " left the server.");
        if (user == admin) {
            admin = null;
            if (!users.isEmpty()) {
                // Reassign admin role to the oldest user
                admin = users.get(0);
                for (User u : users) {
                    if (u.getJoinTime() < admin.getJoinTime()) {
                        admin = u;
                    }
                }
                System.out.println("User " + admin.getId() + " has been assigned as the new admin.");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server1 <num_users>");
            return;
        }

        int numUsers = Integer.parseInt(args[0]);
        Server1 server = new Server1();

        // Simulate adding numUsers users
        for (int i = 1; i <= numUsers; i++) {
            User user = new User(i);
            server.addUser(user);
        }

        // Simulate a user leaving the server
        server.removeUser(server.users.get(2));
    }
}
