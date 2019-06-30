package com.twinkle.cloud.common.asm.data;

import lombok.Data;

import java.util.List;

/**
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2019-06-26 15:33<br/>
 *
 * @author chenxj
 * @see
 * @since JDK 1.8
 */
@Data
public class ClassDefine {
    private String name;
    private String fullName;
    private String pathName;
    private List<AnnotationDefine> annotationList;
}
