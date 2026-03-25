package com.springboot.spring_security.config;

import com.springboot.spring_security.models.Role;
import com.springboot.spring_security.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


// FILE NÀY DÙNG ĐỂ NẠP DỮ LIỆU MỒI CHO BẢNG ROLE KHI CHƯƠNG TRÌNH CHẠY LẦN ĐẦU TIÊN
// VÌ NẾU KHÔNG CÓ THẺ DỮ LIỆU TRONG BẢNG ROLE VÀ PERMISSION THÌ KHÔNG THỂ TẠO ĐƯỢC USER MỚI
// VÌ TRONG DỮ LIỆU CỦA USER CẦN CÓ 1 TRƯỜNG LÀ ROLE 

// Đoạn code chạy ngầm này được Spring Boot triệu hồi dưới tầng Database 
// (vượt qua hoàn toàn tầng phân quyền Request Filter API ở lớp ngoài). 


@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final com.springboot.spring_security.repositories.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem bảng Role đã có dữ liệu chưa
        if (roleRepository.count() == 0) {
            log.info("Bảng Role đang rỗng. Tiến hành nạp dữ liệu mồi (Seeding)...");

            Role userRole = new Role();
            userRole.setRoleName("USER");
            userRole.setDescription("Quyền người dùng cơ bản");

            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole.setDescription("Quyền quản trị viên toàn quyền");

            // Lưu vào Database
            roleRepository.save(userRole);
            roleRepository.save(adminRole);

            log.info("Khởi tạo Role USER và ADMIN thành công!");
        } else {
            log.info("Cơ sở dữ liệu đã có Role. Bỏ qua bước Seeding.");
        }

        // Tạo sẵn 1 tài khoản ADMIN vĩnh viễn để bạn đi Test API
        if (userRepository.findByUserName("superadmin") == null) {
            com.springboot.spring_security.models.User adminUser = new com.springboot.spring_security.models.User();
            adminUser.setUserName("superadmin");
            adminUser.setPassword(passwordEncoder.encode("admin123456"));
            adminUser.setFullName("Quản Trị Viên");
            adminUser.setEmail("admin@domain.com");
            
            java.util.List<Role> roles = new java.util.ArrayList<>();
            roles.add(roleRepository.findById("ADMIN").get());
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            log.info("Khởi tạo tài khoản Quản trị mặc định thành công! (Tên: superadmin - Pass: admin123456)");
        }
    }
}
