<!-- 메인 이미지? -->
<!-- <br /> -->

# FLIP(Factory Logistics Intelligent Platform)

## 📌 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [주요 기능](#-주요-기능)
3. [기술 아키텍처](#-기술-아키텍처처)
4. [기술 스택](#-주요-기술)
5. [산출물](#-산출물)
6. [팀 소개](#-팀-소개)
   <br />

## 🚀 프로젝트 소개

**SSAFY 12기 2학기 자율 프로젝트 (삼성전자 연계 프로젝트)**

> ⌛ 프로젝트 기간 : 2025.04.14 ~ 2025.05.21 (6주)

> 🏠 [배포 URL](https://samsung-smartfactory.co.kr/)

<!-- > 📝 [발표 자료](수정 바람!) -->

**✨ 스마트 팩토리 관제 시스템**<br />
삼성전자 스마트 팩토리 내 설비 자동화가 고도화됨에 따라 제품 생산에 필요한 자재 및 완성된 제품의 이동(물류) 또한 점차 자동화하는 추세이다.  
이런 상황에서 여러 종류의 물류 상황을 통합 관제하고 다양한 제조사의 자율 주행 로봇들을 통합 제어할 수 있는 시스템이 필요하다.

**💪 시스템 목표**<br />
현재 스마트 팩토리 내에서 운용중인 고정형/이동형 물류 기기의 실시간 상태 메시지를 수신하여 Web 화면에 실시간 모니터링 기능을 제공한다.  
또한 이 메시지를 처리하여 DB에 저장하고 이를 기반으로 최적 물류 작업 배치 알고리즘을 개발한다.

## 🚀 주요 기능

<strong>실시간 관제</strong>

<img src="./readme/control.gif" width="500" height="250"/>

<strong>경로 이동</strong>

<img src="./readme/route.gif" width="500" height="250"/>

## 🗺️ 기술 아키텍처

<img src="./readme/architecture.png" width="500" height="250"/>

## 🚀 프론트엔드 기술 스택

<details>
<summary><strong>펼치기</strong>
</summary>

### `Next.js`

React 기반의 프레임워크, 서버 사이드 렌더링(SSR)을 활용하여 성능 최적화.

### `TypeScript`

코드의 안정성 및 유지 보수성을 높이는 강력한 타입 시스템 제공

### `React-Three-fiber/drei`

three js를 React 생태계에서 재사용 가능하고 독립적인 구성 요소를 사용하여 선언적으로 개발할 수 있는 라이브러리.

### `React-query`

서버 상태 관리를 위한 라이브러리로, 데이터 페칭, 캐싱, 동기화, 업데이트를 간편하고 효율적으로 처리.

### `Zustand`

간단하고 가벼운 클라이언트 상태 관리 라이브러리로, 실시간 데이터의 복잡한 상태를 관리.

### `TailwindCSS`

유틸리티 클래스 기반 CSS 프레임워크로, 빠르고 일관된 UI 개발에 활용.

### `Shadcn`

전체 라이브러리를 종속성으로 설치하는 대신 필요한 컴포넌트만 선택적으로 프로젝트에 직접 커스터마이징 및 적용에 활용

</details>

## 🔧 백엔드 기술 스택

<details>
  <summary><strong>펼치기</strong></summary>

### ☕ Java 17

최신 LTS 버전의 Java를 기반으로, 높은 성능과 안정성을 갖춘 백엔드 애플리케이션 구현에 활용.  
Record, Switch Expression 등의 현대적인 문법을 통해 코드 가독성과 개발 생산성 향상.

### 🚀 Spring Boot 3.4.3

REST API, 스케줄링, 예외 처리, 검증 등의 기능을 빠르고 효율적으로 구현할 수 있도록 지원하는 프레임워크.  
프로젝트 구조와 의존성 관리를 표준화하여 유지보수성 향상.

### 🔐 Spring Security

JWT 기반의 인증 및 인가를 위해 커스텀 필터(`CustomAuthenticationFilter`)와 사용자 정의 토큰(`CustomAuthToken`)을 활용하여 보안 처리 구현.

### 🧩 Spring Data JPA

객체지향적인 방식으로 DB에 접근하며, 복잡한 쿼리도 메서드 정의만으로 간편하게 처리.

### 🗄️ MySQL

대중적이고 안정적인 관계형 데이터베이스.  
다양한 인덱스 전략과 트랜잭션 기능을 활용하여 데이터 정합성과 성능을 확보.  
테이블 간 관계(ERD)를 기반으로 유저-식당-메뉴 간의 구조적 설계 구성.

### ⚡ Redis

인증 토큰, 알림 캐시, 인기 맛집 데이터 등의 임시 저장소로 활용.  
빠른 읽기/쓰기 속도를 바탕으로 실시간 사용자 경험 개선.

### 🗂️ AWS S3

사용자 커스텀 메뉴 이미지 및 식당 이미지 저장소로 사용.  
`Pre-signed URL`을 통해 보안성을 확보하며, 대용량 정적 파일 업로드/다운로드 처리에 활용.

### 📦 Docker

로컬 개발 환경과 배포 환경의 일관성을 확보하기 위해 모든 구성 요소를 컨테이너화하여 관리.

### 🔁 Jenkins

GitLab 연동을 통해 자동화된 CI/CD 파이프라인을 구축.  
코드 커밋 시 자동 빌드 및 배포를 통해 개발 효율성과 안정성 강화.

### 🌐 Nginx

정적 리소스 제공 및 리버스 프록시 서버로 활용.  
API 요청 라우팅, SSL 인증서 설정, 로드밸런싱 등 웹 서버 최적화 구성에 기여.

</details>

## 👥 팀 소개

<table style="text-align: center;" width="100%">
  <!-- <tr>
    <th style="text-align: center;" width="16.66%"><img src="https://lab.ssafy.com/s12-fintech-finance-sub1/S12P21E106/-/raw/readme/exec/readme_assets/member/cheon.png" width="150" height="150"/></th>
    <th style="text-align: center;" width="16.66%"><img src="https://lab.ssafy.com/s12-fintech-finance-sub1/S12P21E106/-/raw/readme/exec/readme_assets/member/min.png" width="150" height="150"/></th>
    <th style="text-align: center;" width="16.66%"><img src="https://lab.ssafy.com/s12-fintech-finance-sub1/S12P21E106/-/raw/readme/exec/readme_assets/member/jeong.png" width="150" height="150"/></th>
    <th style="text-align: center;" width="16.66%"><img src="https://lab.ssafy.com/s12-fintech-finance-sub1/S12P21E106/-/raw/readme/exec/readme_assets/member/lee.png" width="150" height="150"/></th>
    <th style="text-align: center;" width="16.66%"><img src="https://lab.ssafy.com/s12-fintech-finance-sub1/S12P21E106/-/raw/readme/exec/readme_assets/member/choi.png" width="150" height="150"/></th>
    <th style="text-align: center;" width="16.66%"><img src="https://lab.ssafy.com/s12-fintech-finance-sub1/S12P21E106/-/raw/readme/exec/readme_assets/member/sin.png" width="150" height="150"/></th>
  </tr> -->
  <tr>
    <td style="text-align: center;" width="16.66%">이병건<br/></td>
    <td style="text-align: center;" width="16.66%">박성재<br/></td>
    <td style="text-align: center;" width="16.66%">남정호<br/></td>
    <td style="text-align: center;" width="16.66%">민상기<br/>
    <!-- <a href="https://github.com/Steadystudy">@Steadystudy</a> -->
    </td>
    <td style="text-align: center;" width="16.66%">송지영<br/></td>
    <td style="text-align: center;" width="16.66%">박태건<br/></td>
  </tr>
  <tr>
    <td style="text-align: center;" width="16.66%">백엔드 개발 </br> (팀장)</td>
    <td style="text-align: center;" width="16.66%">백엔드 개발</td>
    <td style="text-align: center;" width="16.66%">백엔드 개발</td>
    <td style="text-align: center;" width="16.66%">프론트 개발</td>
    <td style="text-align: center;" width="16.66%">프론트 개발</td>
    <td style="text-align: center;" width="16.66%">디자이너</td>
  </tr>
  <!-- <tr>
    <td style="text-align: center;" width="16.66%">본인의 역할</td>
    <td style="text-align: center;" width="16.66%">본인의 역할</td>
    <td style="text-align: center;" width="16.66%">본인의 역할</td>
    <td style="text-align: center;" width="16.66%">AMR 컨트롤 페이지 담당, 공장 및 AMR 관제 담당</td>
    <td style="text-align: center;" width="16.66%">.</td>
    <td style="text-align: center;" width="16.66%">.</td>
  </tr> -->
</table>
