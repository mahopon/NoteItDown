package com.tcyao.nid.note.repository;

import com.tcyao.nid.note.entity.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Long> {

    List<Notebook> findByOwner_Id(UUID ownerId);

    Optional<Notebook> findByIdAndOwner_Id(Long id, UUID ownerId);
}