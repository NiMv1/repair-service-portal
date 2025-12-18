package io.github.nimv1.repair.config;

import io.github.nimv1.repair.entity.User;
import io.github.nimv1.repair.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Инициализация тестовых данных при запуске.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Инициализация тестовых пользователей...");

            // Администратор
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Администратор Системы")
                    .email("admin@repair.local")
                    .phone("+7 (999) 000-00-00")
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .build());

            // Менеджер
            userRepository.save(User.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Иванов Иван Иванович")
                    .email("manager@repair.local")
                    .phone("+7 (999) 111-11-11")
                    .role(User.Role.MANAGER)
                    .enabled(true)
                    .build());

            // Техники
            userRepository.save(User.builder()
                    .username("tech1")
                    .password(passwordEncoder.encode("tech123"))
                    .fullName("Петров Пётр Петрович")
                    .email("tech1@repair.local")
                    .phone("+7 (999) 222-22-22")
                    .role(User.Role.TECHNICIAN)
                    .enabled(true)
                    .build());

            userRepository.save(User.builder()
                    .username("tech2")
                    .password(passwordEncoder.encode("tech123"))
                    .fullName("Сидоров Сидор Сидорович")
                    .email("tech2@repair.local")
                    .phone("+7 (999) 333-33-33")
                    .role(User.Role.TECHNICIAN)
                    .enabled(true)
                    .build());

            // Диспетчер
            userRepository.save(User.builder()
                    .username("dispatcher")
                    .password(passwordEncoder.encode("disp123"))
                    .fullName("Козлова Мария Сергеевна")
                    .email("dispatcher@repair.local")
                    .phone("+7 (999) 444-44-44")
                    .role(User.Role.DISPATCHER)
                    .enabled(true)
                    .build());

            log.info("Тестовые пользователи созданы");
        }
    }
}
