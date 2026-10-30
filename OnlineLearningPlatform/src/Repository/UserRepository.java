package Repository;

import Models.User;
import java.util.ArrayList;

public class UserRepository {

    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            user.setUserId(DataStore.generateUserId());
        }
        // Verify unique email
        User existing = findByEmail(user.getEmail());
        if (existing != null && !existing.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }
        DataStore.users.add(user);
        return user;
    }

    public User findById(String userId) {
        if (userId == null) return null;
        for (int i = 0; i < DataStore.users.size(); i++) {
            User u = DataStore.users.get(i);
            if (u.getUserId().equalsIgnoreCase(userId.trim())) {
                return u;
            }
        }
        return null;
    }

    public User findByEmail(String email) {
        if (email == null) return null;
        for (int i = 0; i < DataStore.users.size(); i++) {
            User u = DataStore.users.get(i);
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                return u;
            }
        }
        return null;
    }

    public ArrayList<User> findAll() {
        return new ArrayList<>(DataStore.users);
    }

    public boolean update(User user) {
        if (user == null || user.getUserId() == null) return false;
        for (int i = 0; i < DataStore.users.size(); i++) {
            if (DataStore.users.get(i).getUserId().equals(user.getUserId())) {
                DataStore.users.set(i, user);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String userId) {
        if (userId == null) return false;
        for (int i = 0; i < DataStore.users.size(); i++) {
            if (DataStore.users.get(i).getUserId().equalsIgnoreCase(userId.trim())) {
                DataStore.users.remove(i);
                return true;
            }
        }
        return false;
    }
}
