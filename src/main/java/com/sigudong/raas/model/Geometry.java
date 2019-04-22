package com.sigudong.raas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by babybong on 2019-03-13.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Geometry {
    private String type;
    private List<?> coordinates;
}
