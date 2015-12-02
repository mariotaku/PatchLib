package org.mariotaku.patchlib.common.model;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by mariotaku on 15/11/29.
 */

public class ModifierInfo {

    final List<Operator> operators = new ArrayList<>();
    IsSetMode setMode = IsSetMode.NONE;

    public static ModifierInfo parse(String string) {
        final ModifierInfo info = new ModifierInfo();
        Scanner scanner = new Scanner(string);
        while (scanner.hasNext()) {
            info.addOperator(scanner.next());
        }
        return info;
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

    public int process(int modifiers) {
        if (setMode == IsSetMode.TRUE) {
            int newModifiers = 0;
            for (Operator operator : operators) {
                newModifiers |= operator.modifier;
            }
            return newModifiers;
        } else {
            for (Operator operator : operators) {
                if (operator.mode == Mode.ADD) {
                    modifiers = regulateModifiers(modifiers, operator.modifier);
                } else if (operator.mode == Mode.REMOVE) {
                    modifiers &= ~operator.modifier;
                }
            }
        }
        return modifiers;
    }

    private int regulateModifiers(int modifiers, int modifier) {
        if (isVisibilityModifier(modifier)) {
            modifiers &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
        }
        return modifiers | modifier;
    }

    private boolean isVisibilityModifier(int modifier) {
        return (modifier & Opcodes.ACC_PUBLIC) != 0 || (modifier & Opcodes.ACC_PRIVATE) != 0 ||
                (modifier & Opcodes.ACC_PROTECTED) != 0;
    }

    @Override
    public String toString() {
        return "ModifierInfo{" +
                "operators=" + operators +
                ", setMode=" + setMode +
                '}';
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
        int modifier;

        Operator(Mode mode, String modifier) {
            this.mode = mode;
            this.modifier = parseModifier(modifier);
        }

        static int parseModifier(String string) {
            switch (string) {
                case "public":
                    return Opcodes.ACC_PUBLIC;
                case "private":
                    return Opcodes.ACC_PRIVATE;
                case "protected":
                    return Opcodes.ACC_PROTECTED;
                case "static":
                    return Opcodes.ACC_STATIC;
                case "final":
                    return Opcodes.ACC_FINAL;
            }
            throw new IllegalArgumentException("Unsupported modifier " + string);
        }

        @Override
        public String toString() {
            return "Operator{" +
                    "mode=" + mode +
                    ", modifier=" + modifier +
                    '}';
        }
    }
}
