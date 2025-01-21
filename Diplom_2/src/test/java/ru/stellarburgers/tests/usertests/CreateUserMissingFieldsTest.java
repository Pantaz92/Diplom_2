package ru.stellarburgers.tests.usertests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import jdk.jfr.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.stellarburgers.api.UserApi;
import ru.stellarburgers.core.BaseTest;
import ru.stellarburgers.core.GsonProvider;
import ru.stellarburgers.dto.User;


import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.CoreMatchers.equalTo;
import static ru.stellarburgers.api.UserApi.USER_403_CREATE_ERROR_MESSAGE;

@RunWith(Parameterized.class)
public class CreateUserMissingFieldsTest extends BaseTest {

    private final User user;
    private final String expectedErrorMessage;

    public CreateUserMissingFieldsTest (User user, String expectedErrorMessage) {
        this.user = user;
        this.expectedErrorMessage = expectedErrorMessage;
    }

    @Parameterized.Parameters(name = "Отсутствует поле: {0}")
    public static Object[][] getTestData() {
        return new Object[][]{
                {new User(null, "password123", "Alex"), USER_403_CREATE_ERROR_MESSAGE},
                {new User("email123@mail.ru", null, "Alex"), USER_403_CREATE_ERROR_MESSAGE},
                {new User("email123@mail.ru", "password123", null), USER_403_CREATE_ERROR_MESSAGE}
        };
    }


    @Test
    @Description("Проверяем, что создание пользователя без обязательных полей возвращает 403 ошибку и верное сообщение")
    @DisplayName("Создание пользователя без обязательных полей")
    public void testCreateUserWithMissingFieldsError() {
        UserApi userApi = new UserApi(GsonProvider.getGson());

        Response response = userApi.createUser(user);
        response.then()
                .statusCode(SC_FORBIDDEN)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(expectedErrorMessage));
    }
}
