package org.mariotaku.patchlib.common.model;

import java.util.*;

/**
 * Created by mariotaku on 15/11/29.
 */

public class ExceptionInfo {

    final List<Operator> operators = new ArrayList<>();
    IsSetMode setMode = IsSetMode.NONE;

    public static ExceptionInfo parse(String string) {
        final ExceptionInfo info = new ExceptionInfo();
        Scanner scanner = new Scanner(string);
        while (scanner.hasNext()) {
            info.addOperator(scanner.next());
        }
        return info;
    }

    @Override
    public String toString() {
        return "ExceptionInfo{" +
                "operators=" + operators +
                ", setMode=" + setMode +
                '}';
    }

    private void addOperator(String token) {
        final Mode mode;
        final String modifier;
        if (token.startsWith("+")) {
            mode = Mode.ADD;
            modifier = token.substring(1).trim();
            setSetMode(IsSetMode.FALSE);
        } else if (token.startsWith("-")) {
            mode = Mode.REMOVE;
            modifier = token.substring(1).trim();
            setSetMode(IsSetMode.FALSE);
        } else {
            mode = Mode.SET;
            modifier = token.trim();
            setSetMode(IsSetMode.TRUE);
        }
        operators.add(new Operator(mode, modifier));
    }

    private void setSetMode(IsSetMode newMode) {
        if (setMode != IsSetMode.NONE && setMode != newMode) {
            throw new IllegalArgumentException("Illegal modifier mode combination " + setMode + " VS " + newMode);
        }
        setMode = newMode;
    }

    public String[] process(String[] exceptions) {
        Set<String> result = new HashSet<>();
        if (setMode == IsSetMode.TRUE) {
            for (Operator operator : operators) {
                result.add(operator.exception);
            }
            return result.toArray(new String[result.size()]);
        } else {
            if (exceptions != null) {
                Collections.addAll(result, exceptions);
            }
            for (Operator operator : operators) {
                if (operator.mode == Mode.ADD) {
                    result.add(operator.exception);
                } else if (operator.mode == Mode.REMOVE) {
                    result.remove(operator.exception);
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    enum Mode {
        ADD, REMOVE, SET
    }

    enum IsSetMode {
        TRUE,
        FALSE,
        NONE
    }

    static class Operator {
        Mode mode;
        String exception;

        Operator(Mode mode, String exception) {
            this.mode = mode;
            this.exception = exception;
        }

        @Override
        public String toString() {
            return "Operator{" +
                    "mode=" + mode +
                    ", exception='" + exception + '\'' +
                    '}';
        }
    }
}
