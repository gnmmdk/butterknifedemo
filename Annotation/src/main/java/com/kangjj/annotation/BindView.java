package com.kangjj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//SOURRCE 注解仅在源码中保留，class文件中不存在
// CLASS注解在源码和class文件中都存在，单运行时不存在
// RUNTIME注解在源码，class文件中存在且运行时可以通过反射机制获取到
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface BindView {
    int value();
}
