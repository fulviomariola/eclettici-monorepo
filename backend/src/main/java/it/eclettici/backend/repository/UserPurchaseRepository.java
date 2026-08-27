package it.eclettici.backend.repository;

import it.eclettici.backend.entity.UserPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPurchaseRepository extends JpaRepository<UserPurchase, Long> {
    boolean existsByUserIdAndCourseId(UUID userId, Long courseId);

    List<UserPurchase> findByUserId(UUID userId);
}