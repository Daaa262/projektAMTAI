package com.projektamtai.projekt.repositories;

import com.projektamtai.projekt.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long>{
}