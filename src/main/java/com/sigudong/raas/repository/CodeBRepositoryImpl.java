package com.sigudong.raas.repository;


import com.sigudong.raas.model.CodeB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

/**
 * Created by babybong on 2019-04-19.
 */
public class CodeBRepositoryImpl implements CodeBCustomRepository {
    @Autowired
    private ReactiveMongoOperations operations;

    public Mono<CodeB> findByCode(String code) {
        Criteria criteria = Criteria.where("_id").is(code);
        Query query = new Query().addCriteria(criteria);

        return operations.findOne(query, CodeB.class);
    }
}
