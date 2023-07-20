package zerobase.weather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

@Service
public class DiaryService {

    @Value("${openweathermap.key}")
    private String apiKey;

    public void createDiary(LocalDate date, String text) {
        // 1. open weather map 에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();

        // 2. 받아온 날씨 json 파싱하기

        // 3. 파싱된 데이터 + 작성한 일기 값을 DB 에 저장하기
    }


    /**
     * open weather map 에서 데이터를 String 으로 받아오기
     */
    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl); // URL 객체 생성
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Http 형식으로 url 연결
            connection.setRequestMethod("GET"); // GET 으로 요청
            int responseCode = connection.getResponseCode(); // 응답 코드

            BufferedReader br;
            if (responseCode == 200) { // 200 OK 이면 API 값 받아오기
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else { // 아닌 경우에는 error code 반환
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }
    }
}
