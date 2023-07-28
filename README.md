# ⛅ 날씨 일기 API 서비스 
날씨 일기를 CRUD 할 수 있는 API 구현
<br/><br/>
<img width="120%" src="https://github.com/hyeonju0121/weather-diary-api-project/assets/67223214/ce15f709-c039-4517-90e1-205786ec2f93"/>
<br/><br/>


## ⚙ 개발 환경
- Language : `Java`
- Framework : `Spring boot 2.7.13`
- Database : `MySQL`
- Build system : `Gradle`
- JDK : `JDK 1.8`
<br/><br/><br/>

## 📑 최종 구현 API
1. **POST / create / diary**
- 일기 텍스트와 날씨를 이용해서 DB에 일기를 저장
2. **GET / read / diary**
- 선택한 날짜의 모든 일기 데이터 조회
3. **GET / read / diaries**
- 선택한 기간중의 모든 일기 데이터 조회
4. **PUT / update / diary**
- 선택한 날짜의 일기 수정
5. **DELETE / delete / diary**
- 선택한 날짜의 일기 데이터 삭제
