package io.claude.my_todo.todo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface TodoRepository extends MongoRepository<Todo, Integer> {

    Todo findFirstByUserId(Integer id);

    Todo findFirstByListId(Integer id);

    Todo findFirstByParentIdAndIdNot(Integer id, Integer currentId);

    List<Todo> findAllByTodoTagTagsId(Integer id);

}
