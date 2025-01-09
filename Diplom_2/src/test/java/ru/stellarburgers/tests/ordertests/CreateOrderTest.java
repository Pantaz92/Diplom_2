package ru.stellarburgers.tests.ordertests;

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
import static org.hamcrest.CoreMatchers.notNullValue;
import static ru.stellarburgers.api.OrderApi.INVALID_INGREDIENT_HASH;
import static ru.stellarburgers.api.OrderApi.ORDER_CREATE_NO_INGREDIENTS_400_ERROR_MESSAGE;
import static ru.stellarburgers.api.UserApi.EMPTY_TOKEN;

public class CreateOrderTest extends BaseTest {
    private UserApi userApi;
    private OrderApi orderApi;
    private String token;
    private Order order;
    List<String> ingredientHashes;
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
        ingredientHashes = new ArrayList<>(orderApi.getIngredientsHash());

        Collections.shuffle(ingredientHashes);
        List<String> randomIngredients = ingredientHashes.stream()
                .limit(3)
                .collect(Collectors.toList());
        order = new Order(randomIngredients);

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
    @Description("Ожидаем статус код 200 при успешном создании заказа с валидными ингридиентами")
    @DisplayName("Успешное создание заказа с валидными ингридиентами")
    public void testCreateOrderWithIngredientsSuccess() {
        Response createOrderResponse = orderApi.createOrder(order, token);
        createOrderResponse
                .then()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true))
                .body("order._id", notNullValue());
    }

    @Test
    @Description("Проверяем что получим 400 ошибку при создании заказа без ингридиентов")
    @DisplayName("Ошибка создании заказа без ингридиентов")
    public void testCreateOrderWithoutIngredientsFail() {
        order.setIngredients(null);
        Response createOrderResponse = orderApi.createOrder(order, token);
        createOrderResponse
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .body("success", equalTo(false))
                .body("message", equalTo(ORDER_CREATE_NO_INGREDIENTS_400_ERROR_MESSAGE));
    }

    @Test
    @Description("Проверяем что сервер отдаст 500 ошибку при создании заказа с неверным хэшем ингридиента")
    @DisplayName("Ошибка создания заказа с несуществующим/неверным ингридиентом")
    public void testCreateOrderInvalidIngredientsFail() {
        order.setIngredients(INVALID_INGREDIENT_HASH);
        Response createOrderResponse = orderApi.createOrder(order, token);
        createOrderResponse
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @Description("Проверяем что сервер отдаст 401 ошибку при создании заказа без авторизации")
    @DisplayName("Ошибка создания заказа без авторизации")
    public void testCreateOrderUnauthorizedFail() {
        Response createOrderResponse = orderApi.createOrder(order, EMPTY_TOKEN);
        createOrderResponse
                .then()
                .statusCode(SC_UNAUTHORIZED);
    }
}
