package ru.netology.netologydiplombackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.netology.netologydiplombackend.model.File;
import ru.netology.netologydiplombackend.model.User;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByFilename(String fileName);

    void deleteByFilename(String fileName);

    @Query("SELECT f FROM File f WHERE f.user = :user ORDER BY f.uploadedAt LIMIT :limit")
    List<File> findTopByUser(@Param("user") User user, @Param("limit") int limit);
}
