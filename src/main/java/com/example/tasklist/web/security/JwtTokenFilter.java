package com.example.tasklist.web.security;

import com.example.tasklist.domain.exception.ResourceNotFoundException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;


@AllArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String bearerToken = ((HttpServletRequest)servletRequest).getHeader("Authorization");
        if(bearerToken !=null && bearerToken.startsWith("Bearer ")){
            bearerToken = bearerToken.substring(7);
        }
        try{
            if(bearerToken !=null && jwtTokenProvider.validateToken(bearerToken)){
                Authentication authentication = jwtTokenProvider.getAuthentication(bearerToken);
                if(authentication!=null){
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }catch (Exception ignored){}
        filterChain.doFilter(servletRequest,servletResponse);
    }



}
