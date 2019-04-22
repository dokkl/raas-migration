package com.sigudong.raas.repository;

import com.sigudong.raas.model.Sigudong;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Created by babybong on 2019-03-13.
 */
public interface SigudongRepository extends ReactiveCrudRepository<Sigudong, String> {
}
