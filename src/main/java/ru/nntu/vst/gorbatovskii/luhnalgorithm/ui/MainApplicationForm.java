package ru.nntu.vst.gorbatovskii.luhnalgorithm.ui;

import ru.nntu.vst.gorbatovskii.luhnalgorithm.utils.AlgorithmUtils;
import ru.nntu.vst.gorbatovskii.luhnalgorithm.utils.AlgorithmUtils.AlgorithmResult;
import ru.nntu.vst.gorbatovskii.luhnalgorithm.utils.DisassemblyUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;

public class MainApplicationForm extends JFrame {

    private JPanel root;
    private JTextArea sourceText;
    private JButton processButton;
    private JTabbedPane tabbedPane1;
    private JTextPane resultTextPane;
    private JSpinner rate;
    private JTable wordMapTable;

    public MainApplicationForm() {
        setContentPane(root);
        rate.setModel(new SpinnerNumberModel(0, 0, 1, 0.1d));
        wordMapTable.setModel(new TableModel(new HashMap<>()));
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = sourceText.getText();
                try {
                    AlgorithmResult result = AlgorithmUtils.referText(DisassemblyUtils.disassembleText(text), (Double) rate.getValue());
                    resultTextPane.setText(result.getResult());
                    wordMapTable.setModel(new TableModel(result.getWordCountMap()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame GUI = new MainApplicationForm();
        GUI.setTitle("Реферирование текста методом Луна");
        GUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GUI.pack();
        GUI.setVisible(true);
    }

    private class TableModel extends AbstractTableModel {

        private Map<String, Integer> wordMap;
        private List<String> orderedKeys;

        public TableModel(Map<String, Integer> wordMap) {
            this.wordMap = wordMap;
            orderedKeys = new LinkedList<>(wordMap.keySet());
            orderedKeys.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Integer.compare(wordMap.get(o2), wordMap.get(o1));
                }
            });
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "Слово";
                case 1: return "Количество";
                default: return "";
            }
        }

        @Override
        public int getRowCount() {
            return wordMap.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return orderedKeys.get(rowIndex);
                case 1:
                    return wordMap.get(orderedKeys.get(rowIndex));
                default:
                    return null;
            }
        }
    }
}
