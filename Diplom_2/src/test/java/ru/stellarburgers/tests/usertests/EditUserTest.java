package ru.stellarburgers.tests.usertests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.stellarburgers.api.UserApi;
import ru.stellarburgers.core.BaseTest;
import ru.stellarburgers.core.GsonProvider;
import ru.stellarburgers.dto.User;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static ru.stellarburgers.api.UserApi.EMPTY_TOKEN;
import static ru.stellarburgers.api.UserApi.USER_401_EDIT_ERROR_MESSAGE;

public class EditUserTest extends BaseTest {
    private UserApi userApi;
    private User user;
    private String token;
    Faker faker;

    @Before
    @Override
    public void setUp() {
        super.setUp();

        userApi = new UserApi(GsonProvider.getGson());

        faker = new Faker();
        user = new User(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().firstName()
        );
    }

    @After
    public void tearDown() {
        if (token != null) {
            Response response = userApi.deleteUser(token);
            response.then()
                    .statusCode(SC_ACCEPTED)
                    .and()
                    .body("success", equalTo(true));
        }
        token = null;
    }

    @Test
    @Description("Проверяем успешное изменение email у пользователя")
    @DisplayName("Успешное изменение email пользователя")
    public void testAuthorizedEditUserEmailSuccess() {
        token = userApi.createUserAndGetToken(user);
        user.setPassword(null);
        user.setEmail(faker.internet().emailAddress());

        Response editResponse = userApi.editUser(user, token);
        editResponse.then()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()));
    }

    @Test
    @Description("Проверяем успешное изменение name у пользователя")
    @DisplayName("Успешное изменение name пользователя")
    public void testAuthorizedEditUserNameSuccess() {
        token = userApi.createUserAndGetToken(user);
        user.setPassword(null);
        user.setName(faker.name().firstName());

        Response editResponse = userApi.editUser(user, token);
        editResponse.then()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true))
                .body("user.name", equalTo(user.getName()));
    }

    @Test
    @Description("Проверяем ошибку при изменении name у пользователя без токена")
    @DisplayName("Ошибка при изменении name пользователя без авторизации")
    public void testUnauthorizedEditUserNameFail() {
        user.setPassword(null);
        user.setName(faker.name().firstName());

        Response editResponse = userApi.editUser(user, EMPTY_TOKEN);
        editResponse.then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(USER_401_EDIT_ERROR_MESSAGE));
    }

    @Test
    @Description("Проверяем ошибку 401 при изменении email у пользователя без токена")
    @DisplayName("Ошибка при изменении email пользователя без авторизации")
    public void testUnauthorizedEditUserEmailFail() {
        user.setPassword(null);
        user.setEmail(faker.internet().emailAddress());

        Response editResponse = userApi.editUser(user, EMPTY_TOKEN);
        editResponse.then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(USER_401_EDIT_ERROR_MESSAGE));
    }
}
