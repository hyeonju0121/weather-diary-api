package zerobase.weather.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class DiaryService {

    public void createDiary(LocalDate date, String text) {
        // 1. open weather map 에서 날씨 데이터 가져오기

        // 2. 받아온 날씨 json 파싱하기

        // 3. 파싱된 데이터 + 작성한 일기 값을 DB 에 저장하기
    }

}
