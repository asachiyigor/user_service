package school.faang.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients("school.faang.user_service.client")
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "User Service",
                version = "1.0.0",
                description = "Description: Faang School - User service project"
        )
)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        module.addSerializer(LocalDateTime.class, localDateTimeSerializer);
        return JsonMapper.builder()
            .addModule(module)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .build();
    }
}