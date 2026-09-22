package com.tcyao.nid.note.repository;

import com.tcyao.nid.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByIdAndCreatedBy_Id(Long id, UUID createdById);

    List<Note> findByCreatedBy_Id(UUID createdById);
}