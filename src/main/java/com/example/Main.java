package com.example;

import io.blaze.server.App;

public class Main {

    public static void main(String[] args) {
        new App()
                .addController(new AppController())
                .addFilter(new IpFilter())
                .addFilter(new CustomCorsFilter())
                .start();
    }

}
