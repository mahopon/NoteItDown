package com.tcyao.nid.storage.reaper;

import com.tcyao.nid.storage.entity.Upload;
import com.tcyao.nid.storage.repository.UploadRepository;
import com.tcyao.nid.storage.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnclaimedUploadReaperTest {

    @Mock
    private UploadRepository repository;

    @Mock
    private UploadService uploadService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private UnclaimedUploadReaper reaper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reaper, "grace", Duration.ofHours(48));
    }

    @SuppressWarnings("unchecked")
    private void runCallbackImmediately() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Integer> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    private Upload upload() {
        Upload upload = new Upload("a.png", "image/png", UUID.randomUUID());
        upload.setId(UUID.randomUUID());
        return upload;
    }

    @Test
    void reap_deletesReapableAndMarksUnreferenced() {
        Upload victim = upload();
        when(repository.findReapable(any())).thenReturn(List.of(victim));
        runCallbackImmediately();
        when(repository.markUnreferencedForReaping(any())).thenReturn(3);

        reaper.reap();

        verify(uploadService).delete(victim.getId());
        verify(repository).markUnreferencedForReaping(any());
    }

    @Test
    void reap_whenOneDeleteFails_continuesWithTheRest() {
        Upload first = upload();
        Upload second = upload();
        when(repository.findReapable(any())).thenReturn(List.of(first, second));
        doThrow(new RuntimeException("storage down")).when(uploadService).delete(first.getId());
        runCallbackImmediately();
        when(repository.markUnreferencedForReaping(any())).thenReturn(0);

        reaper.reap();

        verify(uploadService).delete(first.getId());
        verify(uploadService).delete(second.getId());
        verify(repository).markUnreferencedForReaping(any());
    }
}
