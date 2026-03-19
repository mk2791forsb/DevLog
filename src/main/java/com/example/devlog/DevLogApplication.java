package com.example.devlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Bootアプリケーションの起動クラスです。
 * ここからアプリケーションの実行（組み込みTomcatの起動など）が開始されます。
 */
@SpringBootApplication
public class DevLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevLogApplication.class, args);
    }

}