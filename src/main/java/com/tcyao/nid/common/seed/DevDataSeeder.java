package com.tcyao.nid.common.seed;

import com.tcyao.nid.identity.entity.Role;
import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.identity.repository.UserRepository;
import com.tcyao.nid.note.entity.Notebook;
import com.tcyao.nid.note.enums.NotebookKind;
import com.tcyao.nid.note.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Dev-only seed data loader. Creates the users, notebooks and notes declared under
 * {@code nid.seed} in {@code application-dev.yaml}.
 *
 * <p>Seeding is idempotent per user: a user whose email already exists is skipped, so
 * the seeder can run on every startup and new entries can be added over time.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "nid.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements ApplicationRunner {

    private final SeedProperties properties;
    private final UserRepository userRepository;
    private final NotebookRepository notebookRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<SeedProperties.SeedUser> users = properties.users();
        if (users == null || users.isEmpty()) {
            log.info("Seed data has no users configured; skipping.");
            return;
        }
        transactionTemplate.executeWithoutResult(status -> seed(users));
    }

    private void seed(List<SeedProperties.SeedUser> seedUsers) {
        for (SeedProperties.SeedUser seedUser : seedUsers) {
            if (userRepository.findByEmail(seedUser.email()).isPresent()) {
                log.info("Seed user {} already exists; skipping.", seedUser.email());
                continue;
            }

            User user = new User();
            user.setEmail(seedUser.email());
            user.setHashedPassword(passwordEncoder.encode(seedUser.password()));
            user.setRole(seedUser.role() != null ? seedUser.role() : Role.USER);
            userRepository.save(user);

            List<SeedProperties.SeedNotebook> notebooks = notebooksOf(seedUser);
            for (SeedProperties.SeedNotebook seedNotebook : notebooks) {
                NotebookKind kind = seedNotebook.kind() != null ? seedNotebook.kind() : NotebookKind.PERSONAL;
                Notebook notebook = new Notebook(seedNotebook.title(), kind, user);

                for (SeedProperties.SeedNote seedNote : notesOf(seedNotebook)) {
                    notebook.addNote(seedNote.title(), seedNote.text(), user);
                }

                notebookRepository.save(notebook);
            }

            log.info("Seeded user {} with {} notebook(s).", user.getEmail(), notebooks.size());
        }
    }

    private List<SeedProperties.SeedNotebook> notebooksOf(SeedProperties.SeedUser user) {
        return user.notebooks() != null ? user.notebooks() : List.of();
    }

    private List<SeedProperties.SeedNote> notesOf(SeedProperties.SeedNotebook notebook) {
        return notebook.notes() != null ? notebook.notes() : List.of();
    }
}
