package com.hsh.backend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4876238262151052486L;
    private int pageSize = 10;
    private int pageNum = 1;
}
