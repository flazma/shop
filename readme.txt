# shop


1. Spring 기반의 셋탑 플레이어
- test by raspberrypi model B
 
2. BasicPlayer 를 이용한 mp3 재생
- 리부팅 시 자동으로 플레이어 구동
- 5곡으로 저장 후 1곡시 저장 후 스트리밍(저장된 곡으로 재생)
- 네트워크 오류, 중복로그인, PM 시  비상용 음원 재생
- 비상용 음원의 경우 CM 여부 확인 안됨
- 1분이상 재생 시 재생 로그 쌓여야 함 (추가 개선 필요)


maven install 로 jar파일 생성 가능



armbian으로 OS구성 시 Oracle JAVA가 아닌 OpenJDK로 구동 시 구동 에러 발생
https://support.ipconfigure.com/hc/en-us/articles/213384266-Install-Oracle-JRE-on-Debian-Linux-including-ARM-architectures-

