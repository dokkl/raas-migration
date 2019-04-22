package com.sigudong.raas.repository;

import com.sigudong.raas.model.CodeB;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Created by babybong on 2019-03-15.
 */
public interface CodeBRepository extends ReactiveMongoRepository<CodeB, String>, CodeBCustomRepository {

}
