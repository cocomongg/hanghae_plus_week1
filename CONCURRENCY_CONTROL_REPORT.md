# 동시성 제어 보고서
<br/>

## 목차
#### 1. 멀티 스레드
#### 2. 동시성 이슈
#### 3. PointService - 동시성 이슈 발생 부분
#### 4. 동시성 제어 방식
#### 5. 성능 비교
#### 6. 요약
<br/>
<br/>

## 1. 멀티 스레드
- 멀티 스레드(Multi Thread)란 하나의 프로세스 내에서 여러 개의 스레드가 동시에 작업을 수행하는 것을 의미
- 멀티 스레드 환경에서는 동시에 실행되는 둘 이상의 스레드가 메모리 공간(힙 영역 등)을 공유하기 때문에 동시성 이슈가 발생한다.
<br/>
<br/>

## 2. 동시성 이슈
- 동시성 이슈란 여러 스레드가 동시에 공유 자원에 접근하여 데이터를 읽고 쓰는 과정에서 발생하는 문제를 의미
- 동시성 이슈로 인해 데이터 불일치 등의 문제를 발생시켜 시스템의 안정성과 신뢰성을 저하시킨다.
<br/>
<br/>

## 3. PointService - 동시성 문제 발생 원인

![스크린샷 2024-09-27 오전 12 45 14](https://github.com/user-attachments/assets/76ab2620-9f5e-4e56-a018-792dd4304ddf)

- 위 그림에서 총 사용한 금액은 800 point이기 때문에 최종적으로 `UserPointTable`에는 Thread B 에서만 계산된 잔액이 저장되게 된다.
- 두 개이상의 Thread가 `UserPointTable`의 같은 자원에 접근할 경우(포인트 충전/사용) 동시성 이슈가 발생한다.
<br/>
<br/>

## 4. 동시성 문제 해결 방법

### (1) 암시적 Lock - `synchronized`
- 자바에서 제공하는 동기화 키워드
- 클래스, 메서드 혹은 블록 단위로 적용가능
- 장점
  - 간단한 구현을 통해 동시성을 제어할 수 있다.
- 단점
  - 세밀한 락 제어나 타임아웃 설정 등이 불가능하다.
<br/>

- PointService `synchronized` 적용 예제 코드
```java
public PointDetail charge(long id, long amount) {
    pointValidator.checkAmount(amount);

    synchronized (this) {
        UserPoint userPoint = userPointRepository.selectById(id)
            .orElse(UserPoint.empty(id));

        UserPoint savedUserPoint =
            userPointRepository.insertOrUpdate(userPoint.charge(amount, MAX_AMOUNT));

        PointHistory chargeHistory =
            PointHistory.createChargeHistory(id, amount, System.currentTimeMillis());
        pointHistoryRepository.insert(chargeHistory);

        return PointDetail.of(savedUserPoint);
    }
}
```
<br/>
<br/>

### (2) 명시적 Lock - `ReentrantLock`
- `java.util.concurrent.locks` 패키지에서 제공하는 락 클래스
- synchronized보다 유연한 락 제어를 제공
- 장점
  - 락의 획득과 해제를 명시적으로 관리할 수 있다.
  - 공정성(fairness)를 적용하면 스레드 간의 공평한 락 획득이 가능하다. (오래기다린 스레드부터 락 획득, 성능 저하 이슈 가능성 있음)
- 단점
  - 락 해제를 명시적으로 해야 하므로, 개발자 실수로 인한 데드락이 발생할 수 있다.
<br/>

- PointService `ReentrantLock` 적용 예제 코드
```java
private final ReentrantLock lock = new ReentrantLock();

public PointDetail charge(long id, long amount) {
    pointValidator.checkAmount(amount);

    lock.lock();
    try {
        UserPoint userPoint = userPointRepository.selectById(id)
            .orElse(UserPoint.empty(id));

        UserPoint savedUserPoint =
            userPointRepository.insertOrUpdate(userPoint.charge(amount, MAX_AMOUNT));

        PointHistory chargeHistory =
            PointHistory.createChargeHistory(id, amount, System.currentTimeMillis());
        pointHistoryRepository.insert(chargeHistory);

        return PointDetail.of(savedUserPoint);
    } finally {
        lock.unlock();
    }
}
```
<br/>
<br/>

### (3) Selective Lock - (ConcurrentHashMap + ReentrantLock)
- `ConcurrentHashMap`과 `ReentrantLock`을 조합하여 특정 키나 자원에만 락을 적용하는 방식
- 장점
  - 동시성 제어가 필요한 부분에만 락을 적용하므로 위의 방식들 보다는 성능상 이점이 있다.
- 단점
  - 락의 수가 많아지면 락 관리가 어려워진다.
<br/>

- PointService `Selective Lock` 적용 예제 코드
```java
private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

public PointDetail charge(long id, long amount) {
    pointValidator.checkAmount(amount);

    ReentrantLock lock = lockMap.computeIfAbsent(id, key -> new ReentrantLock());
    lock.lock();
    try {
        UserPoint userPoint = userPointRepository.selectById(id)
            .orElse(UserPoint.empty(id));

        UserPoint savedUserPoint =
            userPointRepository.insertOrUpdate(userPoint.charge(amount, MAX_AMOUNT));

        PointHistory chargeHistory =
            PointHistory.createChargeHistory(id, amount, System.currentTimeMillis());
        pointHistoryRepository.insert(chargeHistory);

        return PointDetail.of(savedUserPoint);
    } finally {
        lock.unlock();
    }
}
```
<br/>

## 5. 성능 비교
\* 별다른 성능 측정 도구를 사용하지 않고, 실행 시간으로만 비교<br/>
\* thread count: 30 기준

### (1) 암시적 Lock - `synchronized`
![스크린샷 2024-09-27 오전 2 00 22](https://github.com/user-attachments/assets/99940e69-b46b-4d6f-ab7b-3221e79b658d)

### (2) 명시적 Lock - `ReentrantLock`
![스크린샷 2024-09-27 오전 2 13 44](https://github.com/user-attachments/assets/ba79d6e4-b24f-4f30-accd-e1084999fa17)

### (3) Selective Lock - (ConcurrentHashMap + ReentrantLock)
![스크린샷 2024-09-27 오전 2 17 27](https://github.com/user-attachments/assets/ebd5e0e5-d3af-4ce1-80ec-bbb86c30aca7)

- 위 사진에서 확인할 수 있듯이 여러 스레드가 접근하는 공유자원이 한 개일 때 발생하는 동시성 제어의 성능(실행 시간)은 비슷하지만,<br/>
접근하는 공유자원이 여러개일때 발생하는 동시성 제어의 성능(실행시간)은 확연히 차이가 난다.

## 6. 요약
- 동시성 제어를 할 수 있는 대표적인 방식들에 대해 알 수 있었다. <br/>특히 공유 자원이 아닐 수도 있음에도 무분별한 lock을 거는 것보다 선택적으로 lock을 거는게 성능상 유리하다는 점도 알 수 있었다.<br/>
이번 프로젝트에서도 적용할 수 있는 범위 내에서 성능이 가장 좋다고 생각한 `ConcurrentHashMap` + `ReentrantLock` 을 조합하여 사용했다.<br/>

