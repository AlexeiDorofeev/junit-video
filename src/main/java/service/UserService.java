package service;

import service.dto.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class UserService {
    private final List<User> users = new ArrayList<>();

    public List<User> getAll() {
        return users;
    }

    public void add(User... users) {
        this.users.addAll(Arrays.asList(users));
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException();
        }
        return users.stream()
            .filter(user -> user.getUsername().equals(username))
            .filter(user -> user.getPassword().equals(password))
            .findFirst();
    }

    public Map<Integer, User> getAllConvertedById() {
        return users.stream()
            .collect(toMap(User::getId, Function.identity()));
    }
}
