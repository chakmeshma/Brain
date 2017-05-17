package net.chakmeshma.brutengine.system;


public interface StateVariable {
    void setValues(float[] values);

    void setValues(int[] values);

    void setValues(boolean[] values);

    void setValue(float value);

    void setValue(int value);

    void setValue(boolean value);

    Class getValueType();

    int getValuesCount();

    String getDefinedName();

    String getDefinedTypeName();

    boolean hasChanged();

    void commitChange();

    //region inner classes
    interface StateVariableMatcher {
        boolean matches(StateVariable stateVariable);

        final class EqualityMatcher implements StateVariableMatcher
        {
            private String declaredDefinedTypeName;
            private String declaredDefinedName;

            public EqualityMatcher(String declaredDefinedTypeName, String declaredDefinedName) {
                this.declaredDefinedName = declaredDefinedName;
                this.declaredDefinedTypeName = declaredDefinedTypeName;
            }

            @Override
            public boolean matches(StateVariable stateVariable) {
                return (stateVariable.getDefinedName().equals(this.declaredDefinedName) && stateVariable.getDefinedTypeName().equals(this.declaredDefinedTypeName));
            }
        }
    }
    //endregion
}
