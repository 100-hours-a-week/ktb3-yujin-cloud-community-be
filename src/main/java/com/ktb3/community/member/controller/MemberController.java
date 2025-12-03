package com.ktb3.community.member.controller;

import com.ktb3.community.auth.annotation.AuthMemberId;
import com.ktb3.community.auth.service.AuthService;
import com.ktb3.community.auth.util.CookieUtil;

import com.ktb3.community.file.dto.ImageRequest;
import com.ktb3.community.member.dto.MemberDto;
import com.ktb3.community.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * 이메일 중복확인
     */
    @PostMapping("/email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@Valid @RequestBody MemberDto.CheckEmailRequest request){
        boolean isDuplicate = memberService.isEmailDuplicate(request.getEmail());
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    /**
     * 닉네임 중복확인
     */
    @PostMapping("/nickname")
    public ResponseEntity<Map<String,Boolean>> checkNickname(@Valid @RequestBody MemberDto.CheckNicknameRequest request) {
        boolean isDuplicate = memberService.isNicknameDuplicate(request.getNickname());
        return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
    }

    /**
     * 회원가입
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody MemberDto.SignUpRequest request){
        memberService.signUp(request);
        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    /**
     * 회원 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<MemberDto.DetailResponse> getMember(@AuthMemberId Long memberId) {

        MemberDto.DetailResponse response = memberService.getMemberDetail(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 이미지 수정 (이미지 메타데이터 DB 저장)
     */
    @PatchMapping("/profile-image")
    public ResponseEntity<MemberDto.DetailResponse> updateProfileImage(
            @RequestBody ImageRequest request,
            @AuthMemberId Long memberId
    ) {
        return ResponseEntity.ok(memberService.updateProfileImage(memberId, request));
    }

    /**
     * 프로필 이미지 삭제(기본 이미지로 클릭시)
     */
    @DeleteMapping("/profile-image")
    public ResponseEntity<Map<String, Object>> deleteProfileImage(
            @AuthMemberId Long memberId
    ) {
        memberService.deleteProfileImage(memberId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * 닉네임 수정
     */
    @PatchMapping("/nickname")
    public ResponseEntity<Map<String, String>> updateNickname(
            @RequestBody @Valid MemberDto.CheckNicknameRequest request,
            @AuthMemberId Long memberId
    ) {

        String newNickname = memberService.updateNickname(request.getNickname(), memberId);

        return ResponseEntity.ok(Map.of("nickname", newNickname));
    }

    /**
     * 회원탈퇴
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<Map<String, String>> deleteMember (@AuthMemberId Long memberId, HttpServletRequest request) {

        // 1. 탈퇴
        memberService.deleteMember(memberId);

        // 2. 로그아웃
        authService.logout(request);

        // 3. 브라우저 쿠키 삭제
        ResponseCookie atCookie = cookieUtil.deleteAccessTokenCookie();
        ResponseCookie rtCookie = cookieUtil.deleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .headers(h -> {
                    h.add(HttpHeaders.SET_COOKIE, atCookie.toString());
                    h.add(HttpHeaders.SET_COOKIE, rtCookie.toString());
                })
                .body(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

}
