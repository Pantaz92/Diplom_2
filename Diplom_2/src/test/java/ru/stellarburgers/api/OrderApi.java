package ru.stellarburgers.api;

import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import ru.stellarburgers.dto.Order;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

public class OrderApi {
    private final Gson gson;
    private static final String ORDER_GET_INGREDIENTS_ENDPOINT = "/ingredients";
    private static final String ORDER_CREATE_ADN_GET_ENDPOINT = "/orders";

    public static final List<String> INVALID_INGREDIENT_HASH = List.of("invalidHash");
    public static final String ORDER_CREATE_NO_INGREDIENTS_400_ERROR_MESSAGE = "Ingredient ids must be provided";

    public OrderApi(Gson gson) {
        this.gson = gson;
    }

    @Step("Получаем список доступных ингредиентов")
    public List<String> getIngredientsHash() {
        Response ingredientsResponse = RestAssured.given()
                .when()
                .get(ORDER_GET_INGREDIENTS_ENDPOINT);
        ingredientsResponse.then().statusCode(SC_OK);

        return ingredientsResponse.jsonPath().getList("data._id", String.class);
    }

    @Step("Создаём заказ")
    public Response createOrder(Order order, String token) {
        String jsonBody = gson.toJson(order);
        return RestAssured.given()
                .header("Authorization", token)
                .body(jsonBody)
                .when()
                .post(ORDER_CREATE_ADN_GET_ENDPOINT);
    }

    @Step("Получаем заказ конкретного пользователя")
    public Response getUserOrder(String token) {
        return RestAssured.given()
                .header("Authorization", token)
                .when()
                .get(ORDER_CREATE_ADN_GET_ENDPOINT);
    }
}
