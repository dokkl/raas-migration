package com.sigudong.raas.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by babybong on 2019-03-15.
 */
@Data
@Document
@NoArgsConstructor
public class CodeB {
    @Id
    private String code;
    private String sido;
    private String sigugun;
    private String eebmeundong;
    private Boolean exist;

    public CodeB(String code) {
        this.code = code;
    }
}
