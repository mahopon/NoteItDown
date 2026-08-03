package com.tcyao.nid.note.service;

import com.tcyao.nid.note.dto.CreateNoteRequest;
import com.tcyao.nid.note.dto.CreateNoteResponse;
import com.tcyao.nid.note.dto.GetNoteResponse;
import com.tcyao.nid.note.dto.UpdateNoteRequest;
import com.tcyao.nid.note.entity.Note;
import com.tcyao.nid.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository repository;

    public CreateNoteResponse createNote(CreateNoteRequest request) {
        Note newNote = new Note();
        newNote.setTitle(request.title());
        newNote.setText(request.text());
        repository.save(newNote);

        return new CreateNoteResponse(newNote.getId(), newNote.getTitle(), newNote.getText());
    }

    @Transactional(readOnly = true)
    public GetNoteResponse getNote(Long id) {
        Note foundNote = repository.findById(id).orElseThrow();
        return new GetNoteResponse(foundNote.getId(), foundNote.getTitle(), foundNote.getText());
    }

    @Transactional(readOnly = true)
    public List<GetNoteResponse> getAllNotes() {
        return repository.findAll().stream()
                .map(note -> new GetNoteResponse(note.getId(), note.getTitle(), note.getText()))
                .toList();
    }

    @Transactional
    public void updateNote(Long id, UpdateNoteRequest request) {
        Note note = repository.findById(id).orElseThrow();
        if (note.getTitle() == null ||!note.getTitle().equals(request.title())) note.setTitle(request.title());
        if (note.getText() == null || !note.getText().equals(request.text())) note.setText(request.text());
    }

    @Transactional
    public void deleteNote(Long id) {
        Note note = repository.findById(id).orElseThrow();
        repository.delete(note);
    }
}
