### 11월 18일 전체 코드 다시 올림


본인이 안드로이드에서 실행시 공기계 연결 필수, 애뮬레이터 사용시 알림화면만 사용 가능, 모니터링모드는 안드로이드에서 제공하는 화면이 나올 것

!현재 DB연결 위해 알림화면 일부 사용 불가 

----

골격데이터가 실시간 서버로 가는 것을 확인하고싶다면 아래 부분들을 본인 테스트서버 IP주소로 바꾸세요.

seniorguard.network.SkeletonNetwork.kt 12번째줄 http://테스트서버IP쓰세요:8000/

res.xml.network-security-config.xml파일 4번째줄 <domain includeSubdomains="true">테스트 서버 IP로 바꾸세요</domain>
