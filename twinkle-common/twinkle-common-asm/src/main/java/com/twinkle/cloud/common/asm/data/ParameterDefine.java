package com.twinkle.cloud.common.asm.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2019-06-27 22:15<br/>
 *
 * @author chenxj
 * @see
 * @since JDK 1.8
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ParameterDefine implements Define {
    /**
     * Class Parameter: public/protect/private, static, final....
     * Method Parameter: final...
     */
    private int access;
    /**
     * Parameter Name.
     */
    @NonNull
    private String name;
    /**
     * The Type for this parameter.
     */
    @NonNull
    private TypeDefine typeDefine;
    /**
     * Each executable line will have one label in Java.
     * So, we can specify the start position and end position.
     */
    private int startLabelIndex;
    /**
     * Match the startLabelIndex.
     */
    private int endLabelIndex;
    /**
     * The annotations of this parameter.
     */
    private List<AnnotationDefine> annotationDefineList;
}
