# 6팀

(팀 협업 문서 링크 게시)

## 팀원 구성

|             김응진             |                            이민주                            |                            이원길                            |                            이주녕                            |                            허원재                            |
| :----------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
|                                | <img width="160px" src="C:\Users\leeco\Desktop\blog\realitsyourman.github.io\assets\img\a7781d01-fea9-4454-97e7-c7c51415f283.png"/> | <img width="160px" src="C:\Users\leeco\Desktop\blog\realitsyourman.github.io\assets\img\5266f84b-8020-427a-8daf-bc2a63456ff6.png"/> | <img width="160px" src="C:\Users\leeco\Desktop\blog\realitsyourman.github.io\assets\img\139120379.png"/> | <img width="160px" src="C:\Users\leeco\Desktop\blog\realitsyourman.github.io\assets\img\39307905.png"/> |
| [@김웅진](https://github.com/) |              [@m0276](https://github.com/m0276)              |     [@realitsyourman](https://github.com/realitsyourman)     |         [@JunyungLee](https://github.com/JunyungLee)         |              [@Oince](https://github.com/Oince)              |

---

## 프로젝트 소개

- Java Spring 백엔드 금융 지수 데이터를 한눈에 제공하는 대시보드 서비스 구축
- 프로젝트 기간: 2025.03.13 ~ 2025.03.24

------

## 기술 스택

- Backend: Spring Boot, Spring Data JPA
- Database: postgresQL
- 공통 Tool: Git & Github, Discord

------

## 팀원별 구현 기능 상세

### 김응진

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- 구현1
  - 설명1
- 구현2
  - 설명2

### 이민주

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- 구현1
  - 설명1
- 구현2
  - 설명2

### 이원길

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- 구현1
  - 설명1
- 구현2
  - 설명2

### 이주녕

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- 구현1
  - 설명1
- 구현2
  - 설명2

### 허원재

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- 구현1
  - 설명1
- 구현2
  - 설명2

------

## 파일 구조

```plaintext
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┣ com
 ┃ ┃ ┃ ┣ example
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┣ AuthController.java
 ┃ ┃ ┃ ┃ ┃ ┣ UserController.java
 ┃ ┃ ┃ ┃ ┃ ┗ AdminController.java
 ┃ ┃ ┃ ┃ ┣ model
 ┃ ┃ ┃ ┃ ┃ ┣ User.java
 ┃ ┃ ┃ ┃ ┃ ┗ Course.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┣ UserRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ CourseRepository.java
 ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┣ AuthService.java
 ┃ ┃ ┃ ┃ ┃ ┣ UserService.java
 ┃ ┃ ┃ ┃ ┃ ┗ AdminService.java
 ┃ ┃ ┃ ┃ ┣ security
 ┃ ┃ ┃ ┃ ┃ ┣ SecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┗ JwtAuthenticationEntryPoint.java
 ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┣ LoginRequest.java
 ┃ ┃ ┃ ┃ ┃ ┗ UserResponse.java
 ┃ ┃ ┃ ┃ ┣ exception
 ┃ ┃ ┃ ┃ ┃ ┣ GlobalExceptionHandler.java
 ┃ ┃ ┃ ┃ ┃ ┗ ResourceNotFoundException.java
 ┃ ┃ ┃ ┃ ┣ utils
 ┃ ┃ ┃ ┃ ┃ ┣ JwtUtils.java
 ┃ ┃ ┃ ┃ ┃ ┗ UserMapper.java
 ┃ ┃ ┃ ┣ resources
 ┃ ┃ ┃ ┃ ┣ application.properties
 ┃ ┃ ┃ ┃ ┗ static
 ┃ ┃ ┃ ┃ ┃ ┣ css
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ style.css
 ┃ ┃ ┃ ┃ ┃ ┣ js
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ script.js
 ┃ ┃ ┃ ┣ webapp
 ┃ ┃ ┃ ┃ ┣ WEB-INF
 ┃ ┃ ┃ ┃ ┃ ┗ web.xml
 ┃ ┃ ┃ ┣ test
 ┃ ┃ ┃ ┃ ┣ java
 ┃ ┃ ┃ ┃ ┃ ┣ com
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ example
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ AuthServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserControllerTest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ApplicationTests.java
 ┃ ┃ ┃ ┣ resources
 ┃ ┃ ┃ ┃ ┣ application.properties
 ┃ ┃ ┃ ┃ ┗ static
 ┃ ┃ ┃ ┃ ┃ ┣ css
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ style.css
 ┃ ┃ ┃ ┃ ┃ ┣ js
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ script.js
 ┣ pom.xml
 ┣ Application.java
 ┣ application.properties
 ┣ .gitignore
 ┗ README.md
```

------

## 구현 홈페이지

(개발한 홈페이지에 대한 링크 게시)

https://www.codeit.kr/

------

## 프로젝트 회고록

(제작한 발표자료 링크 혹은 첨부파일 첨부)
