package com.tcyao.nid.storage.reaper;

import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.repository.UploadRepository;
import com.tcyao.nid.storage.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Periodically cleans up uploads that no note references any more (removed via reconcile, or
 * whose note/notebook was deleted).
 *
 * <p>Two-phase, mark then delete: a run marks unreferenced uploads, and a later run deletes
 * those marked before the grace window. Re-attaching an upload cancels the pending deletion
 * because the delete query re-checks references.
 *
 * <p>Never-uploaded attachments are intentionally out of scope for now (optimistic upload).
 */
@Component
@ConditionalOnProperty(prefix = "nid.upload.reaper", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class UnclaimedUploadReaper {

    private final UploadRepository repository;
    private final UploadService uploadService;
    private final TransactionTemplate transactionTemplate;

    @Value("${nid.upload.reaper.grace:PT48H}")
    private Duration grace;

    @Scheduled(fixedDelayString = "${nid.upload.reaper.interval:PT48H}")
    public void reap() {
        Instant cutoff = Instant.now().minus(grace);
        List<UUID> reapable = repository.findReapable(cutoff).stream()
                .map(Upload::getId)
                .toList();

        for (UUID uploadId : reapable) {
            try {
                uploadService.delete(uploadId);
            } catch (Exception e) {
                log.warn("Failed to reap upload {}", uploadId, e);
            }
        }

        Integer marked = transactionTemplate.execute(status ->
                repository.markUnreferencedForReaping(Instant.now()));
        log.info("Upload reaper: deleted {}, marked {} for reaping", reapable.size(), marked);
    }
}
