package io.claude.my_todo.todo;

import io.claude.my_todo.list.List;
import io.claude.my_todo.tag.Tag;
import io.claude.my_todo.user.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;


@Document
@Getter
@Setter
public class Todo {

    @Id
    private Integer id;

    @NotNull
    @Size(max = 255)
    private String title;

    private String description;

    private Integer priority;

    private Boolean isCompleted;

    private OffsetDateTime completedAt;

    private OffsetDateTime dueDate;

    private OffsetDateTime reminderAt;

    private Integer position;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @DocumentReference(lazy = true)
    @NotNull
    private User user;

    @DocumentReference(lazy = true)
    private List list;

    @DocumentReference(lazy = true)
    private Todo parent;

    @DocumentReference(lazy = true, lookup = "{ 'parent' : ?#{#self._id} }")
    @ReadOnlyProperty
    private Set<Todo> parentTodos = new HashSet<>();

    @DocumentReference(lazy = true)
    private Set<Tag> todoTagTags = new HashSet<>();

    @CreatedDate
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    private OffsetDateTime lastUpdated;

    @Version
    private Integer version;

}
