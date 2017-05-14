package net.chakmeshma.brutengine.development;

/**
 * Created by chakmeshma on 03.05.2017.
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public final class Tester {
    private static HashMap<Long, HashSet<TesterModule>> _callerReferences;
    private static TesterResult _lastTestResult;

    private static void _assertCurrentThreadHaveRequiredModule(TesterCommandString _command) throws InstantiationException, IllegalAccessException {
        long currentThreadID = Thread.currentThread().getId(); //TODO extend

        if (_callerReferences == null) {
            _callerReferences = (HashMap<Long, HashSet<TesterModule>>) Collections.synchronizedMap(new HashMap<Long, HashSet<TesterModule>>());
        }

        if (!_callerReferences.containsKey(currentThreadID)) {
            _callerReferences.put(currentThreadID, (HashSet<TesterModule>) Collections.synchronizedSet(new HashSet<TesterModule>()));
        }

        if (!_callerReferences.get(currentThreadID).contains(_command)) {
            _callerReferences.get(currentThreadID).add(newCommandModuleInstance(_command));
        }
    }

    private static TesterModule newCommandModuleInstance(TesterCommandString command) throws IllegalAccessException, InstantiationException {
        Class commandModuleClass = getCommandModuleClass(command);

        return (TesterModule) commandModuleClass.newInstance();
    }

    public static TesterResult test(TesterCommandString command) {
        try {
            _assertCurrentThreadHaveRequiredModule(command);
        } catch (InstantiationException e) {
            //TODO implement
        } catch (IllegalAccessException e) {
            //TODO implement
        }

        return getCommandModuleObject(command).test(command);
    }

    //TODO implement
    private static TesterModule getCommandModuleObject(TesterCommandString command) {

        return null;
    }

    //TODO implement
    private static Class getCommandModuleClass(TesterCommandString command) {
        return null;
    }
}
