# Closet Lounge

## 📌 프로젝트 소개
Closet Lounge는 패션에 관심 있는 사용자들이 모여 소통하는 폐쇄형 패션 커뮤니티 서비스입니다.
누구나 익명으로 글을 남길 수 있는 일반 커뮤니티와 달리, 이 서비스는 회원가입을 필수로 하며 모든 활동이 로그인 기반으로 이루어지는 비익명 환경을 제공합니다.
취향이 비슷한 사용자들끼리 깊이 있는 패션 정보, 스타일링 팁, 코디 리뷰 등을 공유하도록 설계했습니다.

백엔드는 JWT 기반 인증/인가, QueryDSL을 활용한 효율적인 데이터 조회,
S3 Presigned URL 기반 이미지 업로드,
그리고 AWS EC2·RDS·ALB를 통한 배포 자동화까지
실제 서비스로 확장할 수 있는 수준의 구조를 목표로 구현했습니다.

사용자는 게시물, 댓글, 좋아요 등 기본적인 커뮤니티 기능을 사용할 수 있으며,
패션 커뮤니티에서 중요한 프로필 이미지와 게시글 이미지 업로드 기능을
안정적으로 처리하기 위해 S3와 백엔드 간의 안전한 파일 처리 흐름을 구축했습니다.

## 🗄 ERD (Database Schema)
<img width="1520" height="692" alt="Image" src="https://github.com/user-attachments/assets/3fe77e88-40e4-4bd9-8f31-f117726bdda6" />

* ERD는 인증/회원/게시물/댓글/좋아요/파일/토큰의 핵심 도메인으로 구성되어 있으며,
소프트 삭제 기반의 운영 전략, presigned URL 업로드 흐름, 복합키를 통한 무결성 보장,
Refresh Token의 세션성, QueryDSL 기반 조회 최적화 등을 고려해 설계했습니다.
## 🚀 주요 기능 요약
### 인증 & 인가 (JWT 기반)
* Access Token + Refresh Token 구조
* HttpOnly 쿠키 기반 인증 처리
* @AuthMemberId 커스텀 어노테이션 + Argument Resolver 구현
* 로그인/회원가입/토큰 재발급/로그아웃

### 회원 기능

* 회원가입 / 로그인 / 로그아웃
* 프로필 이미지 업로드(S3)
* 회원 정보 수정 / 탈퇴

### 게시물 기능

* 게시물 CRUD
* 게시물 목록 조회 (커서 기반 무한 스크롤)
* 게시물 이미지 파일 업로드(S3 Presigned URL)
* 조회수 / 좋아요 / 댓글 기능
* 댓글 “더보기” (Limit 기반 페이징)

### 파일 업로드 기능

* Presigned URL 기반 업로드 (FE → S3 직접 업로드)
* Backend → S3 SDK 기반 삭제
* 게시물/프로필 이미지 분리 저장

## 🌐 서버 아키텍처
<img width="607" height="661" alt="Image" src="https://github.com/user-attachments/assets/a85ba6af-aaeb-4532-b8a4-8aa711a8715c" />

* 초기에는 단일 EC2에 FE·BE·DB를 모두 올려 빠르게 기능을 검증하는 방식으로 시작했습니다.
이후 CI/CD를 적용하면서 빌드/배포 과정을 자동화했고,
서비스 구조가 안정화되자 단일 서버 구조의 한계를 명확히 느끼게 되었습니다.
* 이후 Frontend/Backend EC2 분리, RDS로의 DB 이전, ALB를 통한 트래픽 분배, HTTPS 도메인 연결까지 확장했습니다.

## 💻 기술 스택
| 분야        | 기술                            |
| --------- | ----------------------------- |
| Language  | JavaScript (Vanilla)          |
| Runtime   | Node.js                       |
| Framework | Express.js (MPA)              |
| Styling   | CSS                           |
| Infra     | AWS EC2 / ALB / S3 / 외부 DNS   |
| Deploy    | GitHub Actions → EC2 배포 파이프라인 |


## 📂 폴더 구조
<details>
<summary>폴더 구조 보기/숨기기</summary>

```
ktb3-yujin-cloud-community-be/
├── .github/
│   └── workflows/
│       └── ci-cd.yml               # GitHub Actions CI/CD 파이프라인
├── src/
│   ├── main/
│   │   ├── java/com/ktb3/community/
│   │   │   ├── auth/               # 인증/인가 도메인
│   │   │   │   ├── annotation/     # @AuthMemberId 커스텀 어노테이션
│   │   │   │   ├── controller/     # 로그인/회원가입/토큰 재발급 API
│   │   │   │   ├── dto/            # 인증 요청/응답 DTO
│   │   │   │   ├── entity/         # RefreshToken 엔티티
│   │   │   │   ├── repository/     # RefreshToken 저장소
│   │   │   │   ├── resolver/       # AuthMemberIdResolver
│   │   │   │   ├── service/        # AuthService, TokenService
│   │   │   │   └── util/           # CookieUtil
│   │   │   ├── member/             # 회원 도메인
│   │   │   │   ├── controller/     # 회원 정보 조회/수정/탈퇴 API
│   │   │   │   ├── dto/            # 회원 요청/응답 DTO
│   │   │   │   ├── entity/         # Member, MemberAuth 엔티티
│   │   │   │   ├── repository/     # 회원 저장소
│   │   │   │   └── service/        # 회원 비즈니스 로직
│   │   │   ├── post/               # 게시물 도메인
│   │   │   │   ├── controller/     # Post, Comment, Like API
│   │   │   │   ├── dto/            # 게시물/댓글/좋아요 DTO
│   │   │   │   ├── entity/         # Post, PostComment, PostLike 엔티티
│   │   │   │   ├── repository/     # JPA + QueryDSL 저장소
│   │   │   │   └── service/        # 게시물 비즈니스 로직
│   │   │   ├── file/               # 파일 업로드 도메인
│   │   │   │   ├── controller/     # Presigned URL 발급 API
│   │   │   │   ├── dto/            # 파일 요청/응답 DTO
│   │   │   │   ├── entity/         # File 엔티티
│   │   │   │   ├── repository/     # 파일 메타데이터 저장소
│   │   │   │   └── service/        # S3Service, FileService
│   │   │   ├── common/             # 공통 모듈
│   │   │   │   ├── constant/       # 상수 정의
│   │   │   │   ├── controller/     # 페이지 컨트롤러
│   │   │   │   ├── exception/      # 전역 예외 처리
│   │   │   │   ├── filter/         # JwtAuthFilter
│   │   │   │   └── util/           # JwtProvider
│   │   │   ├── config/             # 설정 클래스
│   │   │   │   ├── QuerydslConfig.java
│   │   │   │   ├── S3Config.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebConfig.java
│   │   │   │   └── WebFilterConfig.java
│   │   │   └── CommunityApplication.java
│   │   └── resources/
│   │       ├── application.yml     # 애플리케이션 설정
│   │       └── templates/          # 뷰 템플릿
│   └── test/
├── Dockerfile                      # 프로덕션용 Docker 이미지 빌드 파일
├── docker-compose.yml              # Docker Compose 설정
├── build.gradle                    # Gradle 빌드 설정
└── README.md                       
```
</details>

## 👩‍💻 트러블슈팅

## ▶️ 시연 영상

## Frontend 보기
[Frontend Github 링크](https://github.com/Yu-Jin22/ktb3-yujin-cloud-community-fe) 

## 프로젝트 후기
* Java와 Spring Boot를 사용해 하나의 서비스를 끝까지 구현해 본 프로젝트였습니다.
  해당 언어가 처음이어서 구조가 익숙치 않아 쉽지 않았지만, API를 어떻게 더 효율적으로 설계하고,
  도메인 로직을 어떻게 분리해야 하는지 계속 고민하며 개발할 수 있었습니다. 
* 또한 인증, 파일 업로드, QueryDSL 최적화, AWS 배포 등 웹 서비스에 필요한 다양한 기술 요소들을 직접 구현하면서 
단순히 동작하는 코드를 넘어서 “왜 이렇게 동작하는지”를 이해하게 된 시간이기도 했습니다.
* 아쉬운 점도 분명 있습니다.
아직 리팩터링하고 싶은 부분도 많고,
처음 설계할 때 고려하지 못했던 것들도 뒤늦게 발견되었지만,
오히려 그런 과정 하나하나가 큰 배움이 되었다고 느낍니다.
* 이번 프로젝트를 통해
서비스를 처음부터 끝까지 설계·구현·배포해 보는 경험의 가치를 크게 느꼈고,
다음 프로젝트에서는 구조적인 완성도와 품질을 더 높이고 싶습니다.
