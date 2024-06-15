package com.sparta.icy.AOP;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j(topic = "RequestAop")
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerAop {
    @Pointcut("execution(* com.sparta.icy.controller.UserController.*(..))")
    private void User() {}
    @Pointcut("execution(* com.sparta.icy.controller.NewsfeedController.*(..))")
    private void Newsfeed() {}

    @Pointcut("execution(* com.sparta.icy.controller.LogController.*(..))")
    private void Log() {}

    @Pointcut("execution(* com.sparta.icy.controller.CommentController.*(..))")
    private void Comment() {}

    @Around("User() || Newsfeed() || Log() || Comment()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURL = request.getRequestURL().toString();
        String method = request.getMethod();

        try {
            // 핵심기능 수행
            Object output = joinPoint.proceed();
            return output;
        } finally {
            log.info("Request URL: {}, HTTP Method: {}", requestURL, method);
        }
    }
}