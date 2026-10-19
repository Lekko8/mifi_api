package main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.google.gson.*;

public class Main {

    public static final String MOSCOW = "https://api.weather.yandex.ru/v2/forecast?lat=55.75396&lon=37.620393";
    public static final String SPB = "https://api.weather.yandex.ru/v2/forecast?lat=59.9388&lon=30.3150";
    public static final String SINGAPORE = "https://api.weather.yandex.ru/v2/forecast?lat=1.292703&lon=103.846963";
    public static final String SEUL = "https://api.weather.yandex.ru/v2/forecast?lat=37.56653&lon=126.9780";
    public static final String SANJOSE = "https://api.weather.yandex.ru/v2/forecast?lat=9.9347&lon=-84.0875";

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите ключ API: ");
        String key = scanner.nextLine().trim();

        while (true) {
            System.out.println("Выберите город:");
            System.out.println("[1] Москва");
            System.out.println("[2] Санкт-Петербург");
            System.out.println("[3] Сингапур");
            System.out.println("[4] Сеул");
            System.out.println("[5] Сан-Хосе");

            String city = scanner.nextLine().trim();

            System.out.println("Введите количество дней для расчёта средней температуры: ");
            int limit = scanner.nextInt();

            String answer = null;

            switch (city) {
                case "1":
                    answer = getWeather(key, MOSCOW, limit);
                    break;
                case "2":
                    answer = getWeather(key, SPB, limit);
                    break;
                case "3":
                    answer = getWeather(key, SINGAPORE, limit);
                    break;
                case "4":
                    answer = getWeather(key, SEUL, limit);
                    break;
                case "5":
                    answer = getWeather(key, SANJOSE, limit);
                    break;
                default:
                    System.out.println("Я тебя по ip вычислю, приеду и снесу кабину, " +
                            "если не справляешься с простым выбором из 5 городов");
            }

            if (answer == null) {
                System.out.println("Ответ пустой");
                return;
            }

            System.out.println("Получен ответ: ");
            System.out.println(answer);
            System.out.println("Температура: " + nowTemp(answer));
            System.out.println("Средняя: " + avgTemp(answer, limit));
        }
    }

    static String getWeather(String key, String city, int limit) throws Exception {
        String url = city + "&limit=" + limit;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", key)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Ошибка: " + response.statusCode());
            System.out.println(response.body());
            return null;
        }

        return response.body();
    }

    static double nowTemp(String data) {
        JsonObject root = JsonParser.parseString(data).getAsJsonObject();
        JsonObject fact = root.getAsJsonObject("fact");
        return fact.get("temp").getAsDouble();
    }

    static double avgTemp(String data, int limit) {
        JsonObject root = JsonParser.parseString(data).getAsJsonObject();
        JsonArray forecasts = root.getAsJsonArray("forecasts");

        int days = Math.min(limit, forecasts.size());
        double sum = 0;

        for (int i = 0; i < days; i++) {
            JsonObject forecast = forecasts.get(i).getAsJsonObject();
            JsonObject parts = forecast.getAsJsonObject("parts");
            JsonObject day = parts.getAsJsonObject("day");

            double temperature = day.get("temp_avg").getAsDouble();
            sum += temperature;
        }
        return sum / days;
    }

}

