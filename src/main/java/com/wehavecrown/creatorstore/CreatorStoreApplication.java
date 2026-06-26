package com.wehavecrown.creatorstore;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CreatorStoreApplication {

    public static void main(String[] args) {
        // Configuration of dotenv
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        dotenv.entries().forEach((entry) -> System.setProperty(
                entry.getKey(), entry.getValue()
        ));

        SpringApplication.run(CreatorStoreApplication.class, args);
    }
}
