package it.eclettici.backend.service;

import it.eclettici.backend.dto.AttachmentResponseDto;
import it.eclettici.backend.entity.Attachment;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.repository.AttachmentRepository;
import it.eclettici.backend.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final VideoRepository videoRepository;

    // Definiamo un percorso di archiviazione fisso sul disco (creerà una cartella "uploads/attachments" nella radice del progetto)
    private final Path rootLocation = Paths.get("uploads/attachments").toAbsolutePath().normalize();

    public AttachmentService(AttachmentRepository attachmentRepository, VideoRepository videoRepository) {
        this.attachmentRepository = attachmentRepository;
        this.videoRepository = videoRepository;
        try {
            // Crea la directory fisica all'avvio se non esiste
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile inizializzare la cartella di archiviazione dei file", e);
        }
    }

    @Transactional
    public AttachmentResponseDto saveAttachment(Long videoId, MultipartFile file) throws IOException {
        // 1. Verifica esistenza del video
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video non trovato con ID: " + videoId));

        // 2. Controllo di sicurezza sul nome del file (evita tentativi di Path Traversal come "../file.txt")
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new IllegalArgumentException("Nome del file non valido.");
        }

        // 3. Generazione di un nome file univoco sul disco per evitare collisioni
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetLocation = this.rootLocation.resolve(uniqueFilename);

        // 4. Scrittura fisica del file su disco
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 5. Creazione e persistenza del record nel Database
        Attachment attachment = new Attachment();
        attachment.setName(originalFilename);
        attachment.setFilePath(targetLocation.toString());
        attachment.setFileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        attachment.setSize(file.getSize());
        attachment.setVideo(video);

        Attachment saved = attachmentRepository.save(attachment);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getAttachmentsByVideo(Long videoId) {
        return attachmentRepository.findByVideoId(videoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Attachment getPhysicalAttachment(UUID id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allegato non trovato con ID: " + id));
    }

    @Transactional
    public void deleteAttachment(UUID id) throws IOException {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allegato non trovato con ID: " + id));

        // 1. Elimina il file fisico dal disco
        Path path = Paths.get(attachment.getFilePath());
        Files.deleteIfExists(path);

        // 2. Elimina il record dal DB
        attachmentRepository.delete(attachment);
    }

    private AttachmentResponseDto mapToDto(Attachment attachment) {
        AttachmentResponseDto dto = new AttachmentResponseDto();
        dto.setId(attachment.getId());
        dto.setName(attachment.getName());
        dto.setFileType(attachment.getFileType());
        dto.setSize(attachment.getSize());
        dto.setCreatedAt(attachment.getCreatedAt());
        return dto;
    }
}