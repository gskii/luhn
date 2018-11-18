package ru.nntu.vst.gorbatovskii.luhnalgorithm.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import ru.nntu.vst.gorbatovskii.luhnalgorithm.utils.DisassemblyUtils.DisassemblyResult;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AlgorithmUtils {

    private static final String ENGLISH_WORD = "[a-z]+";
    private static final String RUSSIAN_WORD = "[а-я]+";

    private static final String INTERJECTION = "МЕЖД";
    private static final String PREPOSITION = "ПРЕДЛ";
    private static final String PARTICLE = "ЧАСТ";
    private static final String PRONOUN = "МС";

    private static final String INFO_DELIMITER = "\\|";

    public static AlgorithmResult referText(List<DisassemblyResult> statements, double rate) throws IOException {

        List<AlgorithmContextItem> algorithmContextItems = composeNormalForms(statements);
        Map<String, Integer> wordCountMap = composeWordGlobalMap(algorithmContextItems);
        calculateScore(wordCountMap, algorithmContextItems);

        int statementMax = (int) Math.floor(algorithmContextItems.size() * rate);
        String result = algorithmContextItems.stream().sorted(new Comparator<AlgorithmContextItem>() {
            @Override
            public int compare(AlgorithmContextItem o1, AlgorithmContextItem o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        }).collect(Collectors.toList()).subList(0, statementMax).stream().sorted(new Comparator<AlgorithmContextItem>() {
            @Override
            public int compare(AlgorithmContextItem o1, AlgorithmContextItem o2) {
                return Integer.compare(o1.getSource().getStatementNumber(), o2.getSource().getStatementNumber());
            }
        }).map(new Function<AlgorithmContextItem, String>() {
            @Override
            public String apply(AlgorithmContextItem algorithmContext) {
                return algorithmContext.getSource().getStatement();
            }
        }).collect(Collectors.joining(" "));

        return new AlgorithmResult(algorithmContextItems, wordCountMap, result);
    }

    private static List<AlgorithmContextItem> composeNormalForms(List<DisassemblyResult> statements) throws IOException {
        LuceneMorphology russianMorph = new RussianLuceneMorphology();
        LuceneMorphology englishMorph = new EnglishLuceneMorphology();
        List<AlgorithmContextItem> algorithmContextItems = new LinkedList<>();

        for (DisassemblyResult statement : statements) {
            AlgorithmContextItem result = new AlgorithmContextItem(statement);
            for (String word : statement.getWords()) {
                List<String> normalForms;
                if (word.matches(ENGLISH_WORD)) {
                    normalForms = englishMorph.getNormalForms(word);
                } else if (word.matches(RUSSIAN_WORD)) {
                    normalForms = russianMorph.getMorphInfo(word);

                    // Remove interjections and prepositions
                    normalForms.removeIf(info -> info.contains(INTERJECTION) || info.contains(PREPOSITION) || info.contains(PARTICLE) || info.contains(PRONOUN));
                    normalForms = normalForms.stream().map(new Function<String, String>() {
                        @Override
                        public String apply(String s) {
                            return s.split(INFO_DELIMITER, 0)[0];
                        }
                    }).collect(Collectors.toList());
                } else {
                    normalForms = Collections.singletonList(word);
                }
                if (normalForms != null && !normalForms.isEmpty()) {
                    result.getWordFormMatchingMap().put(word, normalForms);
                }
            }
            algorithmContextItems.add(result);
        }
        return algorithmContextItems;
    }

    private static Map<String, Integer> composeWordGlobalMap(List<AlgorithmContextItem> algorithmContextItems) {
        Map<String, Integer> wordCountMap = new HashMap<>();
        for (AlgorithmContextItem context : algorithmContextItems) {
            for (List<String> normalForms : context.getWordFormMatchingMap().values()) {
                for (String normalForm : normalForms) {
                    Integer counter = wordCountMap.get(normalForm);
                    wordCountMap.put(normalForm, counter == null ? 1 : ++counter);
                }
            }
        }
        return wordCountMap;
    }

    private static void calculateScore(Map<String, Integer> wordCountMap, List<AlgorithmContextItem> algorithmContextItems) {
        for (AlgorithmContextItem result : algorithmContextItems) {
            double score = 0;
            for (Map.Entry<String, List<String>> entry : result.getWordFormMatchingMap().entrySet()) {
                score += entry.getValue().stream().map(new Function<String, Integer>() {
                    @Override
                    public Integer apply(String s) {
                        return wordCountMap.get(s);
                    }
                }).sorted(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1.compareTo(o2);
                    }
                }).findFirst().orElse(0);
            }
            result.setScore(score / result.getWordFormMatchingMap().keySet().size());
        }
    }

    public static class AlgorithmContextItem {
        private Map<String, List<String>> wordFormMatchingMap = new HashMap<>();
        private DisassemblyResult source;
        private double score;

        public AlgorithmContextItem(DisassemblyResult source) {
            this.source = source;
        }

        public DisassemblyResult getSource() {
            return source;
        }

        public void setSource(DisassemblyResult source) {
            this.source = source;
        }

        public Map<String, List<String>> getWordFormMatchingMap() {
            return wordFormMatchingMap;
        }

        public void setWordFormMatchingMap(Map<String, List<String>> wordFormMatchingMap) {
            this.wordFormMatchingMap = wordFormMatchingMap;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    public static class AlgorithmResult {
        private List<AlgorithmContextItem> contextItems;
        private Map<String, Integer> wordCountMap;
        private String result;

        public AlgorithmResult(List<AlgorithmContextItem> contextItems, Map<String, Integer> wordCountMap, String result) {
            this.contextItems = contextItems;
            this.wordCountMap = wordCountMap;
            this.result = result;
        }

        public List<AlgorithmContextItem> getContextItems() {
            return contextItems;
        }

        public void setContextItems(List<AlgorithmContextItem> contextItems) {
            this.contextItems = contextItems;
        }

        public Map<String, Integer> getWordCountMap() {
            return wordCountMap;
        }

        public void setWordCountMap(Map<String, Integer> wordCountMap) {
            this.wordCountMap = wordCountMap;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
