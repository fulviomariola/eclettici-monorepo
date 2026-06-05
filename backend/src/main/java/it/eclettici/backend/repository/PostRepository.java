package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

// Ovvero che è collegato direttamente al DB
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
}