package com.projektamtai.projekt.auth;

import com.projektamtai.projekt.models.UserModel;
import com.projektamtai.projekt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TEST {
    private UserRepository userRepository;

    @Autowired
    public TEST(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(){
        List<UserModel> all = userRepository.findAll();

        for(UserModel user : all){
            System.out.println(user.getUsername());
        }
    }
}
