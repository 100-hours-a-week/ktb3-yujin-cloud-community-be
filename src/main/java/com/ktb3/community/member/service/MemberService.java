package com.ktb3.community.member.service;

import com.ktb3.community.common.exception.BusinessException;
import com.ktb3.community.config.SecurityConfig;
import com.ktb3.community.file.dto.ImageRequest;
import com.ktb3.community.file.entity.File;
import com.ktb3.community.file.service.FileService;
import com.ktb3.community.member.dto.MemberDto;
import com.ktb3.community.member.dto.MemberDto.SignUpResponse;
import com.ktb3.community.member.dto.MemberDto.SignUpRequest;
import com.ktb3.community.member.entity.Member;
import com.ktb3.community.member.entity.MemberAuth;
import com.ktb3.community.member.repository.MemberAuthRepository;
import com.ktb3.community.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberAuthRepository memberAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    // 이메일 중복확인
    public boolean isEmailDuplicate(String email){
        return memberRepository.existsByEmail(email);
    }

    // 닉네임 중복확인
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname((nickname));
    }

    // 회원가입
    @Transactional
    public SignUpResponse signUp(SignUpRequest request){

        if(!request.isPasswordMatching()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }

        // member에 회원정보 저장
        Member member = Member.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .build();

        Member savedMember = memberRepository.save(member);

        // member_auth에 비밀번호 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        MemberAuth memberAuth = MemberAuth.builder()
                .member(savedMember)
                .password(encodedPassword)  // 암호화된 비밀번호
                .build();
        memberAuthRepository.save(memberAuth);

        return SignUpResponse.from(savedMember);

    }

    // 회원 상세 정보 조회
    public MemberDto.DetailResponse getMemberDetail(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 프로필 이미지 조회
        String profileUrl = fileService.getProfileImageUrl(memberId);

        return MemberDto.DetailResponse.from(member, profileUrl);
    }

    // 닉네임 수정
    @Transactional
    public String updateNickname(String nickname,Long memberId) {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."));

        // 중복 체크
        if(isNicknameDuplicate(nickname)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다.");
        }

        member.updateNickname(nickname);

        return member.getNickname();
    }

    // 프로필 이미지 수정 (파일 메타데이터 저장)
    @Transactional
    public MemberDto.DetailResponse updateProfileImage(Long memberId, ImageRequest req) {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."));

        File saved = fileService.saveProfileImage(member, req);
        String imageKey = saved.getFilePath();
        String imageUrl = fileService.buildFileUrl(imageKey);


        return MemberDto.DetailResponse.from(member, imageUrl);
    }


    // 프로필 이미지 삭제 (S3 + DB)
    @Transactional
    public void deleteProfileImage(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."));

        fileService.deleteProfileImage(member);
    }


    // 회원 탈퇴 - 논리삭제
    @Transactional
    public void deleteMember(Long memberId) {

        // 1. 회원확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."));

        // 2. 이미 탈퇴한 회원인지 확인
        if(member.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 탈퇴한 회원입니다.");
        }

        // 3. 회원 논리 삭제
        member.delete();

        // 4. 회원 프로필 논리 삭제
        fileService.softDeleteProfileImage(memberId);

    }
}
