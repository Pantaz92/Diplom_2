package ru.stellarburgers.tests.usertests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.stellarburgers.api.OrderApi;
import ru.stellarburgers.api.UserApi;
import ru.stellarburgers.core.BaseTest;
import ru.stellarburgers.core.GsonProvider;
import ru.stellarburgers.dto.Order;
import ru.stellarburgers.dto.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static ru.stellarburgers.api.UserApi.EMPTY_TOKEN;

public class GetUserOrdersTest extends BaseTest {
    private UserApi userApi;
    private OrderApi orderApi;
    private String token;
    Faker faker;

    @Before
    @Override
    public void setUp() {
        super.setUp();

        userApi = new UserApi(GsonProvider.getGson());
        faker = new Faker();
        User user = new User(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().firstName()
        );
        token = userApi.createUserAndGetToken(user);

        orderApi = new OrderApi(GsonProvider.getGson());
        List<String> ingredientHashes = new ArrayList<>(orderApi.getIngredientsHash());
        Collections.shuffle(ingredientHashes);
        List<String> randomIngredients = ingredientHashes.stream()
                .limit(3)
                .collect(Collectors.toList());

        Order order = new Order(randomIngredients);
        Response createResponse = orderApi.createOrder(order, token);
        createResponse.then().statusCode(SC_OK);

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
    @Description("Ожидаем статус код 200 при успешном получении списка заказов конкретного пользователя")
    @DisplayName("Успешное получение списка заказов конкретного пользователя")
    public void testGetUserOrderSuccess() {
        Response getOrderResponse = orderApi.getUserOrder(token);
        getOrderResponse
                .then()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @Description("Ожидаем статус код 401 при получении списка заказов конкретного пользователя без авторизации")
    @DisplayName("Ошибка при получении списка заказов конкретного пользователя без авторизации")
    public void testGetUserOrderUnauthorizedFail() {
        Response getOrderResponse = orderApi.getUserOrder(EMPTY_TOKEN);
        getOrderResponse
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false));
    }
}