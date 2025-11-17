package io.claude.my_todo.events;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class BeforeDeleteTag {

    private Integer id;

}
