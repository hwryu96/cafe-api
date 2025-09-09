# API 명세

본 문서는 API 정의서입니다.  
모든 응답은 공통 래퍼(`ApiResponse`) 형식을 따릅니다.

---
## 기본 정보

- **Base URL**: `/api`
- **Content-Type**: `application/json`
- **인증/컨텍스트**: 샘플 구현에서는 요청 헤더로 회원 컨텍스트를 주입합니다.
    - `x-cafe-member-id: <long>` — 서버에서 `MemberContext`로 주입
- **개인정보 처리**: 이름/휴대전화/생년월일은 **DB 저장 시 자동 암호화**되고, 조회 시 복호화됩니다.
---

## 공통 응답 포맷

``` json
{
  "code": "OK",
  "message": "성공",
  "data": { /* 엔드포인트별 응답 */ }
}
```

## 검증 오류 예시

``` json
{
  "code": "BAD_REQUEST",
  "message": "전화번호 패턴이 유효하지 않아요."
}
```
---

## 회원 API

### 회원 가입

- `POST /api/members`

**Request (Body)**
``` json
{
  "name": "홍길동",
  "phone": "010-1234-5678",
  "birth": "1996-08-29",
  "gender": "MALE"
}
```
| 이름     | 필수 | 예제            | 비고                        |
|--------|----|---------------|---------------------------|
| name   | O  | 홍길동           | 회원 이름                     |
| phone  | O  | 010-0000-0000 | 회원 전화번호                   |
| birth  | O  | 1996-08-29    | 회원 생년월일                   |
| gender | O  | MALE          | 회원의 성별,  (MALE or FEMALE) |

**Response**
``` json
{
  "code": "OK",
  "message": "성공",
  "data": { 
    "id": 8035260081017210801 
  }
}
```

| 이름 | 필수 | 예제 | 비고                                  |
|----|----|----|-------------------------------------|
| id | O  | 10 | 회원의 아이디, 추후 x-cafe-member-id 헤더로 사용 |

**예시 cURL**
``` shell
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "name":"홍길동",
    "phone":"010-1234-5678",
    "birth":"1996-08-29",
    "gender":"MALE"
  }'
```

### 회원 탈퇴

- `DELETE /api/members/withdraw`
- 로그인 사용자만 사용 가능 (x-cafe-member-id)

**Response**
``` json
{
  "code": "OK",
  "message": "성공"
}
```
**발생 예외**
- `MEMBER_NOT_FOUND`: 탈퇴할 회원의 정보가 존재하지 않음

**예시 cURL**
``` shell
curl -X DELETE http://localhost:8080/api/members/withdraw \
  -H "x-cafe-member-id: 8035260081017210801"
```

### 회원 탈퇴 철회

- `POST /api/members/{id}/restore`
> 탈퇴 사용자는 로그인이 불가하므로 path 사용

| 이름        | 필수 | 예제 | 비고             |
|-----------|----|----|----------------|
| id (path) | O  | 10 | 탈퇴 철회할 회원의 아이디 |


**Response**
``` json
{
  "code": "OK",
  "message": "성공"
}
```
**발생 예외**
- `RESTORE_MEMBER_NOT_FOUND`: 탈퇴 철회할 회원의 정보가 존재하지 않음
- `RESTORE_WINDOW_EXPIRED`: 탈퇴 철회할 가능한 기간이 경과됨

**예시 cURL**
``` shell
curl -X POST http://localhost:8080/api/members/8035260081017210801/restore 
```
---

## 주문 API

### 주문 (결제 포함)

- `POST /api/orders`

**Request (Body)**
``` json
{
  "productId": 1
}
```
| 이름     | 필수 | 예제            | 비고          |
|--------|----|---------------|-------------|
| productId   | O  | 1             | 구매할 상품의 아이디 |

- 샘플 상품의 가격은 productId * 1000으로 구현
- 로그인 사용자만 사용 가능 (x-cafe-member-id) 

**Response**
``` json
{
  "code": "OK",
  "message": "성공",
  "data": { 
    "orderId": 8035260081017210801 
  }
}
```

| 이름      | 필수 | 예제 | 비고      |
|---------|----|----|---------|
| orderId | O  | 2  | 주문의 아이디 |

**발생 예외**
- `LIMIT_EXCEEDED`: 한도 초과로 인한 결제 실패
- `ORDER_FAILED`: 다른 사유로 인한 결제 실패

**예시 cURL**
``` shell
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "x-cafe-member-id: 8035260081017210801" \
  -d '{ "productId": 1 }' 
```

### 주문 취소

- `POST /api/orders/{orderId}/cancel`

**Request**

| 이름             | 필수 | 예제            | 비고          |
|----------------|----|---------------|-------------|
| orderId (path) | O  | 1             | 취소할 주문의 아이디 |

- 로그인 사용자만 사용 가능 (x-cafe-member-id)

**Response**
``` json
{
  "code": "OK",
  "message": "성공",
  "data": { 
    "orderId": 8035260081017210801 
  }
}
```

| 이름      | 필수 | 예제 | 비고           |
|---------|----|----|--------------|
| orderId | O  | 2  | 취소 된 주문의 아이디 |

**발생 예외**
- `ORDER_NOT_FOUND`: 취소할 주문이 존재하지 않음
- `REFUNDABLE_PAYMENT_NOT_FOUND`: 환불이 가능한 결제 내역이 없음
- `CANCEL_NOT_ALLOWED`: 취소 가능 상태 아님

**예시 cURL**
``` shell
curl -X POST http://localhost:8080/api/orders/8035260081017210801/cancel \
  -H "Content-Type: application/json" \
  -H "x-cafe-member-id: 8035260081017210801"
```