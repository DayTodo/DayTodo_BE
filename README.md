# DayTodo_BE

## 프로필 이미지 S3 설정

프로필 이미지는 로컬 파일 시스템에 저장하지 않고 AWS S3에 업로드합니다.

- `AWS_S3_BUCKET`: 프로필 이미지 버킷 이름
- `AWS_REGION`: 버킷 리전

Access Key와 Secret Key는 애플리케이션 설정에 저장하지 않습니다. 배포 환경에서는
Elastic Beanstalk EC2 Instance Profile을 포함한 AWS SDK 기본 자격 증명 체인을 사용합니다.

현재 버킷과 IAM 권한은 아직 준비되지 않았습니다. S3 설정이 비어 있어도 애플리케이션은
시작되지만, 실제 프로필 이미지 업로드 시 `PROFILE_IMAGE_STORAGE_NOT_CONFIGURED` 오류가
반환됩니다. 버킷 정책상 반환된 객체 URL을 클라이언트가 읽을 수 있도록 별도 접근 정책도
배포 전에 준비해야 합니다.

## Firebase Cloud Messaging 설정

푸시 알림을 실제 발송하려면 `FIREBASE_ENABLED=true`와 `FIREBASE_PROJECT_ID`를 설정하고,
Firebase 서비스 계정 파일 경로를 `GOOGLE_APPLICATION_CREDENTIALS`로 제공해야 합니다.
서비스 계정 JSON은 저장소에 커밋하지 않습니다. 설정하지 않은 환경에서도 서버와 테스트는
시작되지만 푸시 발송은 실패 상태로 기록되고 재시도됩니다.

코스 D-1/D-0 알림은 기본적으로 `Asia/Seoul` 기준 매일 오전 9시에 생성됩니다.
`NOTIFICATION_ZONE`, `COURSE_REMINDER_CRON`, `NOTIFICATION_DISPATCH_DELAY_MS` 환경변수로
시간대, 생성 시각, 실패 알림 재시도 주기를 변경할 수 있습니다.

## 정책 문서

`src/main/resources/policies`의 두 문서는 현재 출시 전 자리표시자입니다. 운영 배포 전에
법무 검토를 거친 이용약관과 개인정보처리방침 본문으로 반드시 교체해야 합니다.
