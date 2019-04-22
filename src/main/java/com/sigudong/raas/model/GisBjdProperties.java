package com.sigudong.raas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by babybong on 2019-03-13.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class GisBjdProperties {

    @Field("EMD_CD")
    private String emdCd;
    @Field("EMD_ENG_NM")
    private String emdEngNm;
    @Field("EMD_KOR_NM")
    private String emdKorNm;

    @Transient
    private String sido;

    @Transient
    private String sigugun;

    @Transient
    private String eebmeundong;

    @Transient
    private String sidoCd;

    @Transient
    private String sigugunCd;

}
