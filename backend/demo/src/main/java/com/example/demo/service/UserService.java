package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private JwtUtil jwtUtil;

    @Autowired
    public void AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    private final UserRepository userRepository;


    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    public String createUser(UserDTO dto) {
        User user = UserMapper.toEntity(dto);
        String user_password = dto.getPassword();
        String secure_password = passwordEncoder.encode(user_password);

        user.setPassword(secure_password);
        System.out.println("Encoded password: " + secure_password);
        userRepository.save(user);
        return this.signIn(dto.getEmail(), user_password);

    }
    public String signIn(String email, String password){
        User user = userRepository.findByEmail(email);
        System.out.println("pw in db is " + user.getPassword());
        System.out.println("pw in request is " + passwordEncoder.encode(password));
        if (user != null && passwordEncoder.matches(password, user.getPassword())){
            return this.jwtUtil.generateToken(email);
        } else {
            return "bad credentials";
        }
// Sign-in failed
    }

    public Long getUserIdFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object userIdAttr = request.getAttribute("user_id");
            if (userIdAttr instanceof Long) {
                return (Long) userIdAttr;
            } else if (userIdAttr instanceof Integer) {
                return ((Integer) userIdAttr).longValue();
            }
        }
        throw new RuntimeException("User ID not found in request");
    }

    public String verifyJWT(String token) {
        System.out.println("verifyJWT called with token: " + token);
        if (this.jwtUtil.isTokenValid(token)){
            return "Token is valid and email is " + this.jwtUtil.extractEmail(token);
        } else {
            return "Token is invalid";
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(Long id, UserDTO dto) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return null;
        }

        if (dto.getEmail() != null) {
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            existingUser.setPassword(dto.getPassword());
        }
        // Add similar checks for other fields as needed

        return userRepository.save(existingUser);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }


}
