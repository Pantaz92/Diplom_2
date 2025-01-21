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
import static org.hamcrest.CoreMatchers.notNullValue;
import static ru.stellarburgers.api.UserApi.USER_401_LOGIN_ERROR_MESSAGE;

public class LoginUserTest extends BaseTest {
    private UserApi userApi;
    private User user;
    private String token;

    @Before
    @Override
    public void setUp() {
        super.setUp();

        userApi = new UserApi(GsonProvider.getGson());

        Faker faker = new Faker();
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
            token = null;
        }
    }

    @Test
    @Description("Проверяем успешный логин с валидными данными пользователя")
    @DisplayName("Успешный логин существующим пользователем с валидными данными")
    public void testLoginUserWithValidFieldsSuccess() {
        Response createResponse = userApi.createUser(user);
        createResponse
                .then().statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
        token = createResponse.then().extract().path("accessToken");

        user.setName(null);
        Response loginResponse = userApi.loginUser(user);
        loginResponse.then().statusCode(SC_OK)
                .and()
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()));
    }

    @Test
    @Description("Проверяем получение ошибки 401 при логине с неверным email")
    @DisplayName("Неусппешный логин с неверным email")
    public void testLoginUserWithInvalidEmailError() {
        Response createResponse = userApi.createUser(user);
        createResponse
                .then().statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
        token = createResponse.then().extract().path("accessToken");

        user.setName(null);
        user.setEmail("invalidEmail");
        Response loginResponse = userApi.loginUser(user);
        loginResponse.then().statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(USER_401_LOGIN_ERROR_MESSAGE));
    }

    @Test
    @Description("Проверяем получение ошибки 401 при логине с неверным password")
    @DisplayName("Неусппешный логин с неверным password")
    public void testLoginUserWithInvalidPasswordError() {
        Response createResponse = userApi.createUser(user);
        createResponse
                .then().statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
        token = createResponse.then().extract().path("accessToken");

        user.setName(null);
        user.setPassword("invalidPassword");
        Response loginResponse = userApi.loginUser(user);
        loginResponse.then().statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(USER_401_LOGIN_ERROR_MESSAGE));
    }
}
