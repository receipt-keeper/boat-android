package com.windrr.boat.core

/**
 * 이번 프로세스가 "이미 로그인된 상태로" 시작됐는지를 1회만 기록해 두는 전역 상태.
 *
 * 알림 권한 다이얼로그는 "가입/로그인을 완료한 뒤 앱을 완전히 껐다가 재실행했을 때"만 떠야 한다.
 * 즉, 이번 프로세스 도중에 새로 로그인/회원가입을 완료한 경우(같은 프로세스 안에서 HomeActivity로 전환된 경우)에는
 * 아직 노출 대상이 아니고, 다음 콜드 스타트부터 노출 대상이 된다.
 *
 * [wasLoggedInAtProcessStart]는 Application.onCreate()에서 토큰 저장소를 1회 동기 조회해 세팅하며,
 * 그 이후로는 앱이 실행되는 동안 값이 바뀌지 않는다(이번 프로세스 중 로그인이 새로 발생해도 true로 바뀌지 않음).
 */
object AppLaunchState {
    var wasLoggedInAtProcessStart: Boolean = false
        internal set
}
