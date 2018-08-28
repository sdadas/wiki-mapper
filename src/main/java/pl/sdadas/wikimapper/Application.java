package pl.sdadas.wikimapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.Executors;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
@EnableAsync
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public AsyncListenableTaskExecutor asyncExecutor() {
        return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(3));
    }
}
