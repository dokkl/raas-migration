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
public class SigudongProperties {
    @Field("OBJECTID")
    private Long objectId;
    @Field("adm_nm")
    private String admNm;
    @Field("adm_cd")
    private String admCd;
    @Field("adm_cd2")
    private String admCd2;

    @Transient
    private String si;

    @Transient
    private String gu;

    @Transient
    private String dong;
/*
"OBJECTID" : 89,
        "adm_nm" : "서울특별시 동대문구 답십리2동",
        "adm_cd" : "1106086",
        "adm_cd2" : "1123061000"*/
}
