package com.example.tasklist.web.security;

import com.example.tasklist.domain.exception.AccessDeniedException;
import com.example.tasklist.domain.user.Role;
import com.example.tasklist.domain.user.User;
import com.example.tasklist.service.UserService;
import com.example.tasklist.service.props.JwtProperties;
import com.example.tasklist.web.dto.auth.JwtResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private SecretKey key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());// в этой строке ->
        // берется из jwtProperties секретный ключ, превращается в массив байт и ->
        //метод hmacShaKeyFor принимает именно массив байтов секретного ключа и использует его для создания экземпляра "Key" для алгоритма HMAC SHA
        //HMAC (Hash-Based Message Authentication Code) использует хеш-функцию (в данном случае SHA) вместе с секретным ключом для создания или проверки подписи токена.
    }


    public String createAccessToken(final Long userId,
                                    final String username,
                                    final Set<Role> roles) {
        Claims claims = Jwts.claims() // класс который хранит инфу в юзере
                .subject(username)
                .add("id", userId)
                .add("roles", resolveRoles(roles))
                .build();
        Instant validity = Instant.now()
                .plus(jwtProperties.getAccess(), ChronoUnit.HOURS);
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(validity))
                .signWith(key)
                .compact();
    }

    private List<String> resolveRoles(Set<Role> roles){
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }
    // не принимает роли, потому что нет необходимости, рефрештокен только обновляет пару токенов, он приходит и получает новый рефрешь токен и аксес токен, не зайдествуется для секьюрити для доступа к методам, првоерки пользователя
    public String createRefreshToken(final Long userId, final String username) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("id", userId)
                .build();
        Instant validity = Instant.now()
                .plus(jwtProperties.getRefresh(), ChronoUnit.DAYS);
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(validity))
                .signWith(key)
                .compact();
    }

    public JwtResponse refreshUserTokens(String refreshToken){
        JwtResponse jwtResponse = new JwtResponse();
        if(!validateToken(refreshToken)){
            throw new AccessDeniedException();
        }
        Long userId = Long.valueOf(getId(refreshToken));
        User user = userService.getById(userId);
        jwtResponse.setId(userId);
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setAccessToken(createAccessToken(userId,user.getUsername(),user.getRoles()));
        jwtResponse.setRefreshToken(createRefreshToken(userId,user.getUsername()));
        return jwtResponse;
    }

    public boolean validateToken(final String token) {
            Jws<Claims> claims = Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token); // возвращает содержимое(payload), если подпись верифицирован успешно
            return !claims.getPayload().getExpiration().before(new Date());
    }

    private String getId(String token){
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id")
                .toString();
    }
    private String getUsername(String token) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject(); // используем getSubject() потому что в subject() в методе createAccessToken() и createRefreshToken() в начале помещали username => subject = username
    }
    //Вообще токен можно сравнить с http запросом. Есть тело запроса, заголовок запроса и таким образом хранится информация о токене/запросе.



    //Этот метод будет возвращать аутентификацию которая нужна нам для того чтобы спрингу предоставить инфу о пользователе которого мы проверили.
    public Authentication getAuthentication(String token){
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }




}
