# Limbo Locker

通过更简单的方式在SpringBoot项目中使用Redisson分布式锁。

## 特性

* 单锁、联锁模板（简化普通Redisson分布式锁的使用方式）
* 支持注解声明加、解锁（基于Spring AOP实现）
* 支持注解一键开启（基于SpringBoot自动装配实现）
* 注解加锁时，支持通过SpEL计算锁名称

## 快速开始

#### Maven
    <dependency>
        <groupId>io.github.limbo-world</groupId>
        <artifactId>limbo-locker-spring-boot-starter</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

#### YML
    server:
      port: 9090
    
    spring:
      redis:
        password: 123456
        host: 127.0.0.1
        lettuce:
          pool:
          max-idle: 10
          min-idle: 1
          max-active: 20
          max-wait: 3000

#### Java
```java
// 1. 在启动类上添加开启注解
@EnableLocker
@SpringBootApplication
public class Demo {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .web(WebApplicationType.SERVLET)
                .sources(Demo.class)
                .build()
                .run(args);
    }

}
```

```java
// 2. 在需要加锁的类上添加锁注解

// 单锁
@Locked(expression = "'thisIsALock:' + #id ", waitTime = 4000)
public String lock(String id) {
    log.info("在方法内 id={}", id);
    return UUIDUtils.randomID();
}

// 联锁
@MultiLocked(expressions = {
        "'thisIsALock:' + #id1", 
        "'thisIsALock:' + #id2"
}, waitTime = 4000, holdTime = 20000)
public String multiLock(String id1, String id2) {
    log.info("在联锁方法内 id1={} id2={}", id1, id2);
    return UUIDUtils.randomID();
}
```

## 更多

想要获取更多支持，或加入项目，可联系 brozen.lau@gmail.com 或 ysodevilo@163.com