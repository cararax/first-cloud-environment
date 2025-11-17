package io.claude.my_todo.list;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface ListRepository extends MongoRepository<List, Integer> {

    List findFirstByUserId(Integer id);

}
