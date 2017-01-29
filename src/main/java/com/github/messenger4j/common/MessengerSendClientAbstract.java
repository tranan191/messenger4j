package com.github.messenger4j.common;

import com.github.messenger4j.common.MessengerHttpClient.HttpResponse;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Max Grabenhorst
 * @author Andriy Koretskyy
 * @since 0.8.0
 */
public abstract class MessengerSendClientAbstract<P, R> {

    private final Gson gson;
    private final JsonParser jsonParser;
    private final String requestUrl;
    private final MessengerHttpClient httpClient;

    protected MessengerSendClientAbstract(String requestUrl, MessengerHttpClient httpClient) {
        this.gson = new GsonBuilder().registerTypeAdapter(Float.class, floatSerializer()).create();
        this.jsonParser = new JsonParser();
        this.requestUrl = requestUrl;
        this.httpClient = httpClient;
    }

    protected R sendPayload(P payload, MessengerHttpClient.HttpMethod httpMethod)
            throws MessengerApiException, MessengerIOException {

        try {
            final String jsonBody = this.gson.toJson(payload);
            final HttpResponse httpResponse = this.httpClient.execute(this.requestUrl, jsonBody, httpMethod);
            final JsonObject responseJsonObject = this.jsonParser.parse(httpResponse.getBody()).getAsJsonObject();

            if (httpResponse.getStatusCode() >= 200 && httpResponse.getStatusCode() < 300) {
                return responseFromJson(responseJsonObject);
            } else {
                throw MessengerApiException.fromJson(responseJsonObject);
            }
        } catch (IOException e) {
            throw new MessengerIOException(e);
        }
    }

    protected abstract R responseFromJson(JsonObject responseJsonObject);

    private JsonSerializer<Float> floatSerializer() {
        return new JsonSerializer<Float>() {
            public JsonElement serialize(Float floatValue, java.lang.reflect.Type type, JsonSerializationContext context) {
                if (floatValue.isNaN() || floatValue.isInfinite()) {
                    return null;
                }
                return new JsonPrimitive(new BigDecimal(floatValue).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        };
    }
}