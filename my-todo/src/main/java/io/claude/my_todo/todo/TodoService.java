package io.claude.my_todo.todo;

import io.claude.my_todo.events.BeforeDeleteList;
import io.claude.my_todo.events.BeforeDeleteTag;
import io.claude.my_todo.events.BeforeDeleteTodo;
import io.claude.my_todo.events.BeforeDeleteUser;
import io.claude.my_todo.list.ListRepository;
import io.claude.my_todo.tag.Tag;
import io.claude.my_todo.tag.TagRepository;
import io.claude.my_todo.user.User;
import io.claude.my_todo.user.UserRepository;
import io.claude.my_todo.util.CustomCollectors;
import io.claude.my_todo.util.NotFoundException;
import io.claude.my_todo.util.ReferencedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(rollbackFor = Exception.class)
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final ListRepository listRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher publisher;

    public TodoService(final TodoRepository todoRepository, final UserRepository userRepository,
            final ListRepository listRepository, final TagRepository tagRepository,
            final ApplicationEventPublisher publisher) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
        this.listRepository = listRepository;
        this.tagRepository = tagRepository;
        this.publisher = publisher;
    }

    public List<TodoDTO> findAll() {
        final List<Todo> todos = todoRepository.findAll(Sort.by("id"));
        return todos.stream()
                .map(todo -> mapToDTO(todo, new TodoDTO()))
                .toList();
    }

    public TodoDTO get(final Integer id) {
        return todoRepository.findById(id)
                .map(todo -> mapToDTO(todo, new TodoDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final TodoDTO todoDTO) {
        final Todo todo = new Todo();
        mapToEntity(todoDTO, todo);
        return todoRepository.save(todo).getId();
    }

    public void update(final Integer id, final TodoDTO todoDTO) {
        final Todo todo = todoRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(todoDTO, todo);
        todoRepository.save(todo);
    }

    public void delete(final Integer id) {
        final Todo todo = todoRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteTodo(id));
        todoRepository.delete(todo);
    }

    private TodoDTO mapToDTO(final Todo todo, final TodoDTO todoDTO) {
        todoDTO.setId(todo.getId());
        todoDTO.setTitle(todo.getTitle());
        todoDTO.setDescription(todo.getDescription());
        todoDTO.setPriority(todo.getPriority());
        todoDTO.setIsCompleted(todo.getIsCompleted());
        todoDTO.setCompletedAt(todo.getCompletedAt());
        todoDTO.setDueDate(todo.getDueDate());
        todoDTO.setReminderAt(todo.getReminderAt());
        todoDTO.setPosition(todo.getPosition());
        todoDTO.setCreatedAt(todo.getCreatedAt());
        todoDTO.setUpdatedAt(todo.getUpdatedAt());
        todoDTO.setUser(todo.getUser() == null ? null : todo.getUser().getId());
        todoDTO.setList(todo.getList() == null ? null : todo.getList().getId());
        todoDTO.setParent(todo.getParent() == null ? null : todo.getParent().getId());
        todoDTO.setTodoTagTags(todo.getTodoTagTags().stream()
                .map(tag -> tag.getId())
                .toList());
        return todoDTO;
    }

    private Todo mapToEntity(final TodoDTO todoDTO, final Todo todo) {
        todo.setTitle(todoDTO.getTitle());
        todo.setDescription(todoDTO.getDescription());
        todo.setPriority(todoDTO.getPriority());
        todo.setIsCompleted(todoDTO.getIsCompleted());
        todo.setCompletedAt(todoDTO.getCompletedAt());
        todo.setDueDate(todoDTO.getDueDate());
        todo.setReminderAt(todoDTO.getReminderAt());
        todo.setPosition(todoDTO.getPosition());
        todo.setCreatedAt(todoDTO.getCreatedAt());
        todo.setUpdatedAt(todoDTO.getUpdatedAt());
        final User user = todoDTO.getUser() == null ? null : userRepository.findById(todoDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        todo.setUser(user);
        final io.claude.my_todo.list.List list = todoDTO.getList() == null ? null : listRepository.findById(todoDTO.getList())
                .orElseThrow(() -> new NotFoundException("list not found"));
        todo.setList(list);
        final Todo parent = todoDTO.getParent() == null ? null : todoRepository.findById(todoDTO.getParent())
                .orElseThrow(() -> new NotFoundException("parent not found"));
        todo.setParent(parent);
        final List<Tag> todoTagTags = iterableToList(tagRepository.findAllById(
                todoDTO.getTodoTagTags() == null ? List.of() : todoDTO.getTodoTagTags()));
        if (todoTagTags.size() != (todoDTO.getTodoTagTags() == null ? 0 : todoDTO.getTodoTagTags().size())) {
            throw new NotFoundException("one of todoTagTags not found");
        }
        todo.setTodoTagTags(new HashSet<>(todoTagTags));
        return todo;
    }

    private <T> List<T> iterableToList(final Iterable<T> iterable) {
        final List<T> list = new ArrayList<T>();
        iterable.forEach(item -> list.add(item));
        return list;
    }

    public Map<Integer, String> getTodoValues() {
        return todoRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Todo::getId, Todo::getTitle));
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final Todo userTodo = todoRepository.findFirstByUserId(event.getId());
        if (userTodo != null) {
            referencedException.setKey("user.todo.user.referenced");
            referencedException.addParam(userTodo.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteList.class)
    public void on(final BeforeDeleteList event) {
        final ReferencedException referencedException = new ReferencedException();
        final Todo listTodo = todoRepository.findFirstByListId(event.getId());
        if (listTodo != null) {
            referencedException.setKey("list.todo.list.referenced");
            referencedException.addParam(listTodo.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteTodo.class)
    public void on(final BeforeDeleteTodo event) {
        final ReferencedException referencedException = new ReferencedException();
        final Todo parentTodo = todoRepository.findFirstByParentIdAndIdNot(event.getId(), event.getId());
        if (parentTodo != null) {
            referencedException.setKey("todo.todo.parent.referenced");
            referencedException.addParam(parentTodo.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteTag.class)
    public void on(final BeforeDeleteTag event) {
        // remove many-to-many relations at owning side
        todoRepository.findAllByTodoTagTagsId(event.getId()).forEach(todo ->
                todo.getTodoTagTags().removeIf(tag -> tag.getId().equals(event.getId())));
    }

}
