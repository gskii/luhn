package ru.nntu.vst.gorbatovskii.luhnalgorithm.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisassemblyUtils {

    private static final String STATEMENT_PATTERN = "[^!?.]+[!?.]";
    private static final String WORD_PATTERN = "[a-zа-я]+";

    public static List<DisassemblyResult> disassembleText(String input) {
        List<DisassemblyResult> disassemblyResults = new LinkedList<>();
        int statementCounter = 0;
        for (String statement : splitOnStatements(input)) {
            DisassemblyResult result = new DisassemblyResult();
            result.setStatementNumber(statementCounter++);
            result.setStatement(statement);
            result.setWords(splitOnWords(statement));
            disassemblyResults.add(result);
        }

        return disassemblyResults;
    }

    private static List<String> splitOnStatements(String input) {
        List<String> statements = new LinkedList<>();
        Pattern pattern = Pattern.compile(STATEMENT_PATTERN);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            statements.add(matcher.group().trim());
        }
        return statements;
    }

    private static List<String> splitOnWords(String input) {
        List<String> statements = new LinkedList<>();
        Pattern pattern = Pattern.compile(WORD_PATTERN);
        Matcher matcher = pattern.matcher(input.toLowerCase());
        while (matcher.find()) {
            statements.add(matcher.group().trim());
        }
        return statements;
    }

    public static class DisassemblyResult {
        private int statementNumber;
        private String statement;
        private List<String> words;

        public int getStatementNumber() {
            return statementNumber;
        }

        public void setStatementNumber(int statementNumber) {
            this.statementNumber = statementNumber;
        }

        public String getStatement() {
            return statement;
        }

        public void setStatement(String statement) {
            this.statement = statement;
        }

        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> words) {
            this.words = words;
        }

        @Override
        public String toString() {
            return "DisassemblyResult{" +
                    "statementNumber=" + statementNumber +
                    ", statement='" + statement + '\'' +
                    ", words=" + words +
                    '}';
        }
    }
}
