package service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import service.dto.User;
import service.paramresolver.UserServiceParamResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith( {
    UserServiceParamResolver.class
})
class UserServiceTest {
    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(UserService userService) {
        System.out.println("Before each: " + this);
        this.userService = userService;
    }

    @Test
    void usersEmptyIfNoUserAdded(UserService userService) {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);
        List<User> users = userService.getAll();
        assertThat(users).hasSize(2);
    }

    @Test
    @Tag("login")
    void loginSuccessIfUserExists() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
        assertThat(maybeUser).isPresent();
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
    }

    @Test
    @Tag("login")
    void loginFailedIfPasswordIsNotCorrect() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), "dummy");
        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);
        Map<Integer, User> users = userService.getAllConvertedById();
        assertAll(() -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()), () -> assertThat(users).containsValues(IVAN, PETR));
    }

    @Test
    @Tag("login")
    void throwExceptionIfUsernameOrPasswordIsNull() {
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy")),
            () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null)));
    }

    @Test
    @Tag("login")
    void loginFailedIfUserDoesNotExist() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login("dummy", IVAN.getPassword());
        assertTrue(maybeUser.isEmpty());
    }

    @ParameterizedTest
//    @NullSource
//    @EmptySource
//    @ValueSource(strings = {
//        "Ivan", "Petr"
//    })
    @MethodSource("getArgumentsForLoginTest")
    void loginParametrizedTest(String username, String password, Optional<User> user) {
        userService.add(IVAN, PETR);

        Optional<User> maybeUser = userService.login(username, password);
        assertThat(maybeUser).isEqualTo(user);
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
            Arguments.of("Ivan", "123", Optional.of(IVAN)),
            Arguments.of("Petr", "111", Optional.of(PETR)),
            Arguments.of("Petr", "dummy", Optional.empty()),
            Arguments.of("dummy", "123", Optional.empty())
        );
    }

    @AfterEach
    void deleteDataFromDB() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }
}


