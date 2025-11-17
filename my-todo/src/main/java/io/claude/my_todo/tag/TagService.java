package io.claude.my_todo.tag;

import io.claude.my_todo.events.BeforeDeleteTag;
import io.claude.my_todo.events.BeforeDeleteUser;
import io.claude.my_todo.user.User;
import io.claude.my_todo.user.UserRepository;
import io.claude.my_todo.util.CustomCollectors;
import io.claude.my_todo.util.NotFoundException;
import io.claude.my_todo.util.ReferencedException;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(rollbackFor = Exception.class)
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public TagService(final TagRepository tagRepository, final UserRepository userRepository,
            final ApplicationEventPublisher publisher) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    public List<TagDTO> findAll() {
        final List<Tag> tags = tagRepository.findAll(Sort.by("id"));
        return tags.stream()
                .map(tag -> mapToDTO(tag, new TagDTO()))
                .toList();
    }

    public TagDTO get(final Integer id) {
        return tagRepository.findById(id)
                .map(tag -> mapToDTO(tag, new TagDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final TagDTO tagDTO) {
        final Tag tag = new Tag();
        mapToEntity(tagDTO, tag);
        return tagRepository.save(tag).getId();
    }

    public void update(final Integer id, final TagDTO tagDTO) {
        final Tag tag = tagRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(tagDTO, tag);
        tagRepository.save(tag);
    }

    public void delete(final Integer id) {
        final Tag tag = tagRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteTag(id));
        tagRepository.delete(tag);
    }

    private TagDTO mapToDTO(final Tag tag, final TagDTO tagDTO) {
        tagDTO.setId(tag.getId());
        tagDTO.setName(tag.getName());
        tagDTO.setColor(tag.getColor());
        tagDTO.setCreatedAt(tag.getCreatedAt());
        tagDTO.setUser(tag.getUser() == null ? null : tag.getUser().getId());
        return tagDTO;
    }

    private Tag mapToEntity(final TagDTO tagDTO, final Tag tag) {
        tag.setName(tagDTO.getName());
        tag.setColor(tagDTO.getColor());
        tag.setCreatedAt(tagDTO.getCreatedAt());
        final User user = tagDTO.getUser() == null ? null : userRepository.findById(tagDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        tag.setUser(user);
        return tag;
    }

    public Map<Integer, String> getTagValues() {
        return tagRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Tag::getId, Tag::getName));
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final Tag userTag = tagRepository.findFirstByUserId(event.getId());
        if (userTag != null) {
            referencedException.setKey("user.tag.user.referenced");
            referencedException.addParam(userTag.getId());
            throw referencedException;
        }
    }

}
