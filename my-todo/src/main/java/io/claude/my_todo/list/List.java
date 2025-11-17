package io.claude.my_todo.list;

import io.claude.my_todo.todo.Todo;
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
public class List {

    @Id
    private Integer id;

    @NotNull
    @Size(max = 100)
    private String name;

    private String description;

    @Size(max = 7)
    private String color;

    @Size(max = 50)
    private String icon;

    private Integer position;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @DocumentReference(lazy = true)
    @NotNull
    private User user;

    @DocumentReference(lazy = true, lookup = "{ 'list' : ?#{#self._id} }")
    @ReadOnlyProperty
    private Set<Todo> listTodos = new HashSet<>();

    @CreatedDate
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    private OffsetDateTime lastUpdated;

    @Version
    private Integer version;

}
