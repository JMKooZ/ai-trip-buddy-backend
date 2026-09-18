package com.mk.aitripbuddybackend.user.security;

import com.mk.aitripbuddybackend.user.entity.User;
import com.mk.aitripbuddybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuthUserInfo info = extractUserInfo(registrationId, attributes);

        User user = userRepository.findByProviderAndProviderId(registrationId, info.id())
                .map(existing -> {
                    existing.updateProfile(info.nickname(), info.email(), info.profileImageUrl());
                    return existing;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .provider(registrationId)
                        .providerId(info.id())
                        .nickname(info.nickname())
                        .email(info.email())
                        .profileImageUrl(info.profileImageUrl())
                        .build()));

        return new CustomOAuth2User(
                user.getId(),
                user.getNickname(),
                attributes,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @SuppressWarnings("unchecked")
    private OAuthUserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return new OAuthUserInfo(
                    (String) response.get("id"),
                    (String) response.get("name"),
                    (String) response.get("email"),
                    (String) response.get("profile_image")
            );
        }

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = kakaoAccount != null
                    ? (Map<String, Object>) kakaoAccount.get("profile")
                    : null;

            return new OAuthUserInfo(
                    String.valueOf(attributes.get("id")),
                    profile != null ? (String) profile.get("nickname") : "카카오 사용자",
                    kakaoAccount != null ? (String) kakaoAccount.get("email") : null,
                    profile != null ? (String) profile.get("profile_image_url") : null
            );
        }

        throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다: " + registrationId);
    }

    private record OAuthUserInfo(String id, String nickname, String email, String profileImageUrl) {}
}