package com.sigudong.raas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by babybong on 2019-03-13.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sigudong_b_imsi")
public class GisBjd {
    @Id
    private String id;
    private String type;
    private GisBjdProperties properties;
    private Geometry geometry;

}
