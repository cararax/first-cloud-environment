package io.claude.my_todo.tag;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TagDTO {

    private Integer id;

    @NotNull
    @Size(max = 50)
    private String name;

    @Size(max = 7)
    private String color;

    private OffsetDateTime createdAt;

    @NotNull
    private Integer user;

}
