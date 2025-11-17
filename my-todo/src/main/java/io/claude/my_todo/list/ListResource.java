package io.claude.my_todo.list;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/lists", produces = MediaType.APPLICATION_JSON_VALUE)
public class ListResource {

    private final ListService listService;

    public ListResource(final ListService listService) {
        this.listService = listService;
    }

    @GetMapping
    public ResponseEntity<List<ListDTO>> getAllLists() {
        return ResponseEntity.ok(listService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListDTO> getList(@PathVariable(name = "id") final Integer id) {
        return ResponseEntity.ok(listService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Integer> createList(@RequestBody @Valid final ListDTO listDTO) {
        final Integer createdId = listService.create(listDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Integer> updateList(@PathVariable(name = "id") final Integer id,
            @RequestBody @Valid final ListDTO listDTO) {
        listService.update(id, listDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteList(@PathVariable(name = "id") final Integer id) {
        listService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
