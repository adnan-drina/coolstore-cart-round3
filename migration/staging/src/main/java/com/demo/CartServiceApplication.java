package com.demo;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.springframework.cloud.openfeign.EnableFeignClients;

@QuarkusMain
@EnableFeignClients
public class CartServiceApplication {

    public static void main(String[] args) {
        Quarkus.run(args);
    }
}