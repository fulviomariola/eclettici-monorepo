package it.eclettici.backend.repository;

import it.eclettici.backend.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    // Recupera tutti gli allegati legati a un determinato video
    List<Attachment> findByVideoId(Long videoId);
}