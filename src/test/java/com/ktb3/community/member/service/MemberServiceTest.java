package com.ktb3.community.member.service;

import com.ktb3.community.member.dto.MemberDto;
import com.ktb3.community.member.entity.Member;
import com.ktb3.community.member.entity.MemberAuth;
import com.ktb3.community.member.repository.MemberAuthRepository;
import com.ktb3.community.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    // 가짜 객체 생성
    private MemberRepository memberRepository;

    @Mock
    private MemberAuthRepository memberAuthRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    // mock객체를 멤버서비스에 주입
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 테스트")
    void createMember(){
        // given
        String email = "test@naver.com";
        String nickname = "테스트";

        MemberDto.SignUpRequest request = MemberDto.SignUpRequest.builder()
                .email("test@naver.com")
                .nickname("테스트")
                .password("Test1234!")
                .confirmPassword("Test1234!")
                .build();

        Member member = Member.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .build();

        // Mock 설정
        when(passwordEncoder.encode(anyString())).thenReturn("encodePW");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberAuthRepository.save(any(MemberAuth.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = memberService.signUp(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getNickname()).isEqualTo(nickname);

        // 동작 검증 (Mock 호출 확인)
        verify(memberRepository).save(any(Member.class));
        verify(memberAuthRepository).save(any(MemberAuth.class));
        verify(passwordEncoder).encode(anyString());
    }
}
