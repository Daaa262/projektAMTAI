package com.projektamtai.projekt.auth;

import com.projektamtai.projekt.models.LoginRequest;
import com.projektamtai.projekt.models.UserModel;
import com.projektamtai.projekt.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class Login {
    UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secret;

    public Login(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest){
        List<UserModel> all = userRepository.findAll();
        for(UserModel user : all){
            if(user.getUsername().equals(loginRequest.getUsername()) && user.getPassword().equals(loginRequest.getPassword())){
                return generateToken(user.getUsername(), user.getRole());
            }
        }
        return null;
    }

    private String generateToken(String username, Long role){
        return Jwts.builder().setSubject(username).claim("role", role).setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)).signWith(SignatureAlgorithm.HS512, secret).compact();
    }
}
