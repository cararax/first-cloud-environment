package io.claude.my_todo.tag;

import io.claude.my_todo.config.PrimarySequenceService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;


@Component
public class TagListener extends AbstractMongoEventListener<Tag> {

    private final PrimarySequenceService primarySequenceService;

    public TagListener(final PrimarySequenceService primarySequenceService) {
        this.primarySequenceService = primarySequenceService;
    }

    @Override
    public void onBeforeConvert(final BeforeConvertEvent<Tag> event) {
        if (event.getSource().getId() == null) {
            event.getSource().setId(((int)primarySequenceService.getNextValue()));
        }
    }

}
