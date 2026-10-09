package com.hscmt.common.aop;

import com.hscmt.common.enumeration.AuthCd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserRoleCheckRequired {
    AuthCd enableAuth() default AuthCd.NORMAL;
}
