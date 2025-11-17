package io.claude.my_todo.todo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TodoDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    private String title;

    private String description;

    private Integer priority;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;

    private OffsetDateTime completedAt;

    private OffsetDateTime dueDate;

    private OffsetDateTime reminderAt;

    private Integer position;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @NotNull
    private Integer user;

    private Integer list;

    private Integer parent;

    private List<Integer> todoTagTags;

}
