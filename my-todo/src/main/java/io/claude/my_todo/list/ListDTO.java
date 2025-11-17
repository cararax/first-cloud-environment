package io.claude.my_todo.list;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ListDTO {

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

    @NotNull
    private Integer user;

}
