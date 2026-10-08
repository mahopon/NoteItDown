package com.tcyao.nid.common.seed;

import com.tcyao.nid.identity.entity.Role;
import com.tcyao.nid.note.enums.NotebookKind;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "nid.seed")
public record SeedProperties(boolean enabled, List<SeedUser> users) {

    public record SeedUser(String email, String password, Role role, List<SeedNotebook> notebooks) {
    }

    public record SeedNotebook(String title, NotebookKind kind, List<SeedNote> notes) {
    }

    public record SeedNote(String title, String text) {
    }
}
