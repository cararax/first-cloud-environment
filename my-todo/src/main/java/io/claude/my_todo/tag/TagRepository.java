package io.claude.my_todo.tag;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface TagRepository extends MongoRepository<Tag, Integer> {

    Tag findFirstByUserId(Integer id);

}
