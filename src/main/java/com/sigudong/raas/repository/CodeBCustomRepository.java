package com.sigudong.raas.repository;


import com.sigudong.raas.model.CodeB;
import reactor.core.publisher.Mono;

/**
 * Created by babybong on 2019-03-15.
 */
public interface CodeBCustomRepository {
    Mono<CodeB> findByCode(String code);
}
