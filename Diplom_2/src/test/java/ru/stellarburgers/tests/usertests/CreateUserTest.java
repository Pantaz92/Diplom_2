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

public class CreateUserTest extends BaseTest {

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
    @Description("Проверяем что код ответа 200 и в ответе есть токен")
    @DisplayName("Успешное создание пользователя со всеми полями")
    public void testCreateUserWithAllFieldsSuccess() {
        Response response = userApi.createUser(user);
        response
                .then().statusCode(SC_OK)
                .and()
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()));

        token = response.then().extract().path("accessToken");
    }

    @Test
    @Description("Проверяем что нельзя создать существующего пользователя")
    @DisplayName("Создание существующего пользователя")
    public void testCreateExistUserError() {
        Response responseWithSuccess = userApi.createUser(user);
        responseWithSuccess
                .then().statusCode(SC_OK);

        token = responseWithSuccess.then().extract().path("accessToken");

        Response responseWithError = userApi.createUser(user);
        responseWithError
                .then().statusCode(SC_FORBIDDEN)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }
}
