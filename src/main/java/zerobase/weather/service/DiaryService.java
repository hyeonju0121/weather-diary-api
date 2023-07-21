package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {

    @Value("${openweathermap.key}")
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    /**
     * 매일 새벽 1시에 날씨 데이터를 저장하는 메서드
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
    }

    /**
     * 날씨 일기를 작성하는 메서드
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        // 해당 날짜의 날씨 데이터 가져오기 (이미 DB 에 저장된 데이터 가져오기)
        DateWeather dateWeather = getDateWeather(date);

        // 작성한 일기 값을 DB 에 저장하기
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }

    /**
     * api 에서 현재 날씨 데이터를 가져오는 메서드
     * @return dateWeather
     */
    private DateWeather getWeatherFromApi() {
        // 1. open weather map 에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();

        // 2. 받아온 날씨 json 파싱하기
        Map<String, Object> parseWeather = parseWeather(weatherData);

        // 3. 파싱된 데이터를 DateWeather 객체로 만들기
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parseWeather.get("main").toString());
        dateWeather.setIcon(parseWeather.get("icon").toString());
        dateWeather.setTemperature((double) parseWeather.get("temp"));

        return dateWeather;
    }

    /**
     * 해당 날짜의 날씨 데이터를 가져오는 메서드
     */
    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);

        if (dateWeatherListFromDB.size() == 0) {
            // 해당 날짜의 날씨 데이터가 없다면, 새로 api 에서 해당 일의 날씨 데이터를 받아오기
            // 하지만 지금 사용하는 api 는 과거 날짜의 데이터를 가져오는 것은 "유료"임
            // 따라서 날씨 데이터가 없다면, 우선 현재의 날씨 데이터를 가져오도록 가정하고 구현
            return getWeatherFromApi();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }

    /**
     * 해당 날짜의 일기를 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        return diaryRepository.findAllByDate(date);
    }

    /**
     * 조회 날짜 기간을 설정하여 일기를 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    /**
     * 해당 날짜의 일기를 수정하는 메서드
     */
    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);

        diaryRepository.save(nowDiary);
    }

    /**
     * 해당 날짜의 일기를 삭제하는 메서드
     */
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    /**
     * open weather map 에서 데이터를 String 으로 받아오는 메서드
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

    /**
     * 받아온 날씨 데이터를 JSON 파싱하는 메서드
     */
    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser(); // JSONParser -> JSON 데이터를 파싱하는 기능을 구현한 클래스
        JSONObject jsonObject; // JSONObject -> JSON 객체를 추상화한 클래스

        // json 파싱하는 작업이 정상적으로 동작하지 않는 경우를 핸들링하기 위해 예외처리 진행
        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }
}
