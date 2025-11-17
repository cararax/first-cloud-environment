package io.claude.my_todo.list;

import io.claude.my_todo.events.BeforeDeleteList;
import io.claude.my_todo.events.BeforeDeleteUser;
import io.claude.my_todo.user.User;
import io.claude.my_todo.user.UserRepository;
import io.claude.my_todo.util.CustomCollectors;
import io.claude.my_todo.util.NotFoundException;
import io.claude.my_todo.util.ReferencedException;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class ListService {

    private final ListRepository listRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public ListService(final ListRepository listRepository, final UserRepository userRepository,
            final ApplicationEventPublisher publisher) {
        this.listRepository = listRepository;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    public java.util.List<ListDTO> findAll() {
        final java.util.List<List> lists = listRepository.findAll(Sort.by("id"));
        return lists.stream()
                .map(list -> mapToDTO(list, new ListDTO()))
                .toList();
    }

    public ListDTO get(final Integer id) {
        return listRepository.findById(id)
                .map(list -> mapToDTO(list, new ListDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final ListDTO listDTO) {
        final List list = new List();
        mapToEntity(listDTO, list);
        return listRepository.save(list).getId();
    }

    public void update(final Integer id, final ListDTO listDTO) {
        final List list = listRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(listDTO, list);
        listRepository.save(list);
    }

    public void delete(final Integer id) {
        final List list = listRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteList(id));
        listRepository.delete(list);
    }

    private ListDTO mapToDTO(final List list, final ListDTO listDTO) {
        listDTO.setId(list.getId());
        listDTO.setName(list.getName());
        listDTO.setDescription(list.getDescription());
        listDTO.setColor(list.getColor());
        listDTO.setIcon(list.getIcon());
        listDTO.setPosition(list.getPosition());
        listDTO.setCreatedAt(list.getCreatedAt());
        listDTO.setUpdatedAt(list.getUpdatedAt());
        listDTO.setUser(list.getUser() == null ? null : list.getUser().getId());
        return listDTO;
    }

    private List mapToEntity(final ListDTO listDTO, final List list) {
        list.setName(listDTO.getName());
        list.setDescription(listDTO.getDescription());
        list.setColor(listDTO.getColor());
        list.setIcon(listDTO.getIcon());
        list.setPosition(listDTO.getPosition());
        list.setCreatedAt(listDTO.getCreatedAt());
        list.setUpdatedAt(listDTO.getUpdatedAt());
        final User user = listDTO.getUser() == null ? null : userRepository.findById(listDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        list.setUser(user);
        return list;
    }

    public Map<Integer, String> getListValues() {
        return listRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(List::getId, List::getName));
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final List userList = listRepository.findFirstByUserId(event.getId());
        if (userList != null) {
            referencedException.setKey("user.list.user.referenced");
            referencedException.addParam(userList.getId());
            throw referencedException;
        }
    }

}
