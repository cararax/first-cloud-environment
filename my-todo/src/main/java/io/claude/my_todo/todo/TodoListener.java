package io.claude.my_todo.todo;

import io.claude.my_todo.config.PrimarySequenceService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;


@Component
public class TodoListener extends AbstractMongoEventListener<Todo> {

    private final PrimarySequenceService primarySequenceService;

    public TodoListener(final PrimarySequenceService primarySequenceService) {
        this.primarySequenceService = primarySequenceService;
    }

    @Override
    public void onBeforeConvert(final BeforeConvertEvent<Todo> event) {
        if (event.getSource().getId() == null) {
            event.getSource().setId(((int)primarySequenceService.getNextValue()));
        }
    }

}
