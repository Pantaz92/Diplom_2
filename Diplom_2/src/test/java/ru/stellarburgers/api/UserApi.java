package ru.stellarburgers.api;

import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import ru.stellarburgers.dto.User;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserApi {
    private final Gson gson;
    private static final String USER_CREATE_ENDPOINT = "/auth/register";
    private static final String USER_GET_EDIT_DELETE_ENDPOINT = "/auth/user";
    private static final String USER_LOGIN_ENDPOINT = "/auth/login";

    public static final String EMPTY_TOKEN = "";
    public static final String USER_403_CREATE_ERROR_MESSAGE = "Email, password and name are required fields";
    public static final String USER_401_LOGIN_ERROR_MESSAGE = "email or password are incorrect";
    public static final String USER_401_EDIT_ERROR_MESSAGE = "You should be authorised";

    public UserApi(Gson gson) {
        this.gson = gson;
    }

    @Step("Создаём пользователя")
    public Response createUser(User user) {
        String jsonBody = gson.toJson(user);
        return RestAssured.given()
                .body(jsonBody)
                .when()
                .post(USER_CREATE_ENDPOINT);
    }

    @Step("Удаляем пользователя")
    public Response deleteUser(String token) {
        return RestAssured.given()
                .header("Authorization", token)
                .when()
                .delete(USER_GET_EDIT_DELETE_ENDPOINT);
    }

    @Step("Логинимся под пользователем")
    public Response loginUser(User user) {
        String jsonBody = gson.toJson(user);
        return RestAssured.given()
                .body(jsonBody)
                .when()
                .post(USER_LOGIN_ENDPOINT);
    }

    @Step("Редактируем данные пользователя")
    public Response editUser(User user, String token) {
        String jsonBody = gson.toJson(user);
        return RestAssured.given()
                .header("Authorization", token)
                .body(jsonBody)
                .when()
                .patch(USER_GET_EDIT_DELETE_ENDPOINT);
    }

    @Step("Создаём пользователя и получаем токен")
    public String createUserAndGetToken(User user) {
        Response createResponse = createUser(user);
        createResponse
                .then()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));

        return createResponse.then().extract().path("accessToken");
    }
}
