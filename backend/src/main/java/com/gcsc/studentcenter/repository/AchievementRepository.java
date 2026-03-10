package com.gcsc.studentcenter.repository;

import com.gcsc.studentcenter.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findAllByAuthor_UsernameOrderByCreatedAtDesc(String username);
}
