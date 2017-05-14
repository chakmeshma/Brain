package net.chakmeshma.brutengine.development.exceptions;

import net.chakmeshma.brutengine.utilities.GLUtilities.GLAdaptionRule;

/**
 * Created by chakmeshma on 10.05.2017.
 */

public class InvalidAdaptionOperationException extends InvalidOperationException {
    GLAdaptionRule typeAdaptionRule;

    public InvalidAdaptionOperationException(GLAdaptionRule typeAdaptionRule) {
        super("adaption rule listed but not implemented");

        this.typeAdaptionRule = typeAdaptionRule;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
