package com.hscmt.simulation.user.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.JwtTokenErrorCode;
import com.hscmt.common.exception.error.UserErrorCode;
import com.hscmt.common.jwt.JwtToken;
import com.hscmt.common.jwt.enumeration.TokenState;
import com.hscmt.common.util.CryptoUtil;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.common.jwt.JwtTokenProvider;
import com.hscmt.simulation.user.domain.User;
import com.hscmt.simulation.user.dto.*;
import com.hscmt.simulation.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SimulationTx(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider provider;

    /* 사용자 등록 */
    @SimulationTx
    public void registerUser (SignupDto signupDto) {
        userRepository.findById(signupDto.getUserId())
                .ifPresentOrElse(findUser -> {
                    throw new RestApiException(UserErrorCode.USER_ALREADY_EXISTS);
                }, () -> {
                    User user = new User(signupDto);
                    userRepository.save(user);
                });
    }

    /* 사용자 정보 수정 */
    @SimulationTx
    public void updateUser (UserUpsertDto dto) {
        userRepository.findById(dto.getUserId())
                .ifPresentOrElse(findUser -> {
                    if (dto.getUserPwd() != null && !dto.getUserPwd().isBlank()) {
                        if (!CryptoUtil.isMatched(dto.getUserPwd(), findUser.getSaltKey(), findUser.getUserPwd())) {
                            throw new RestApiException(UserErrorCode.INVALID_USER_INFO);
                        }
                    }
                    findUser.updateInfo(dto);
                }, () -> {
                    throw new RestApiException(UserErrorCode.USER_NOT_FOUND);
                });
    }

    /* 사용자 삭제 */
    @SimulationTx
    public void deleteUser (String userId) {
        userRepository.findById(userId)
                .ifPresentOrElse( findUser -> {
                    /* 사용자 삭제 */
                    userRepository.deleteById(userId);
                },() -> {
                    throw new RestApiException(UserErrorCode.USER_NOT_FOUND);
                });
    }

    /* 사용자 존재여부 확인 */
    public Boolean existUserById (String userId) {
        User findUser = userRepository.findById(userId).orElse(null);
        return findUser == null ? false : true;
    }

    /* 로그인 */
    @SimulationTx
    public JwtToken login (LoginDto loginDto) {

        User findUser = userRepository.findById(loginDto.getId()).orElse(null);

        if (findUser == null) {
           throw new RestApiException(UserErrorCode.INVALID_USER_INFO);
        } else {
            String inputPassword = loginDto.getPassword();
            String userSalt = findUser.getSaltKey();
            boolean isMatched =  CryptoUtil.isMatched(inputPassword, userSalt, findUser.getUserPwd());
            if (isMatched) {

                Map<String, Object> claims = new HashMap<>();
                claims.put("role", findUser.getAuthCd());
                String subject = findUser.getUserId();
                String accessToken = provider.generateAccessToken(subject, claims);
                String refreshToken = provider.generateRefreshToken(subject, claims);

                findUser.changeRefreshToken(refreshToken);

                return JwtToken.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            } else {
                throw new RestApiException(UserErrorCode.INVALID_USER_INFO);
            }
        }
    }

    @SimulationTx
    public void logout () {
        String subject = provider.getSubject();
        User findUser = userRepository.findById(subject).orElse(null);
        if (findUser == null) {
            throw new RestApiException(UserErrorCode.USER_NOT_FOUND);
        } else {
            findUser.changeRefreshToken(null);
        }
    }

    /* 사용자 목록 조회 */
    public List<UserDto> findAllUser () {
        return userRepository.findAllUsers();
    }

    /* 사용자 ID 로 사용자 정보 조회 */
    public UserDto findUserById (String userId) {
        return userRepository.findUserById(userId);
    }

    /* 리프레시토큰 확인 */
    @SimulationTx
    @Cacheable(value = CacheConst.CACHE_5SEC, key = "T(com.hscmt.common.cache.CacheKeys).generateKey(#dto.refreshToken)", sync = true)
    public JwtToken checkRefreshToken (RefreshRequestDto dto) {
        String inputToken = dto.getRefreshToken();
        TokenState tokenState = provider.getTokenState(inputToken);

        switch (tokenState) {
            case VALID -> {
                /* 사용자 ID 가져오기 */
                String userId = provider.getSubject(inputToken);
                /* 사용자 정보 조회 */
                User findUser = userRepository.findById(userId).orElse(null);
                if (findUser == null) {
                    /* 사용자 정보가 없다면 */
                    throw new RestApiException(UserErrorCode.USER_NOT_FOUND);
                }
                if (!inputToken.equals((findUser).getRftkVal())) {
                    /* db에 저장된 토큰과 같지 않다면 유효하지 않음. */
                    throw new RestApiException(JwtTokenErrorCode.INVALID_TOKEN);
                }
                Map<String, Object> claims = new HashMap<>();
                claims.put("role", findUser.getAuthCd());


                JwtToken resultToken = JwtToken.builder()
                        .accessToken(provider.generateAccessToken(userId, claims))
                        .refreshToken(provider.generateRefreshToken(userId, claims))
                        .build();

                findUser.changeRefreshToken(resultToken.getRefreshToken());

                return resultToken;
            }
            case EXPIRED -> throw new RestApiException(JwtTokenErrorCode.EXPIRED_TOKEN);
            default -> throw new RestApiException(JwtTokenErrorCode.REFRESH_CHECK_FAIL);
        }
    }
}
