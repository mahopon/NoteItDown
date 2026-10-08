package com.tcyao.nid.storage.repository;

import com.tcyao.nid.storage.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface UploadRepository extends JpaRepository<Upload, UUID> {

    /**
     * Marks every upload that no note references (and that is not already marked).
     */
    @Modifying
    @Query(value = """
            UPDATE uploads SET reap_marked_at = :now
            WHERE reap_marked_at IS NULL
              AND NOT EXISTS (SELECT 1 FROM note_attachments na WHERE na.upload_id = uploads.id)
            """, nativeQuery = true)
    int markUnreferencedForReaping(@Param("now") Instant now);

    /**
     * Uploads marked before {@code cutoff} that are still unreferenced, i.e. ready to delete.
     * The reference is re-checked here so re-attaching cancels the pending deletion.
     */
    @Query(value = """
            SELECT u.* FROM uploads u
            WHERE u.reap_marked_at IS NOT NULL
              AND u.reap_marked_at < :cutoff
              AND NOT EXISTS (SELECT 1 FROM note_attachments na WHERE na.upload_id = u.id)
            """, nativeQuery = true)
    List<Upload> findReapable(@Param("cutoff") Instant cutoff);
}
