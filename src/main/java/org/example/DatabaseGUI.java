package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.Random;
import java.util.Vector;
import java.io.*;
import java.util.PriorityQueue;

class DatabaseGUI extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField dataField;
    private BTree bTree;
    private int nextKey = 1;
    private PriorityQueue<Integer> availableKeys;

    public DatabaseGUI() {
        int degree = 50;
        bTree = new BTree(degree);
        setTitle("Simple Database");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        bTree = loadTree();
        availableKeys = loadAvailableKeys();

        JPanel inputPanel = new JPanel(new FlowLayout());
        dataField = new JTextField(20);
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton editButton = new JButton("Edit");
        JButton searchButton = new JButton("Search");

        inputPanel.add(new JLabel("Data:"));
        inputPanel.add(dataField);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);
        inputPanel.add(editButton);
        inputPanel.add(searchButton);

        tableModel = new DefaultTableModel(new Object[]{"Key", "Data"}, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        add(inputPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addRecord());
        deleteButton.addActionListener(e -> deleteRecord());
        editButton.addActionListener(e -> editRecord());
        searchButton.addActionListener(e -> searchRecord());

        loadRecordsIntoTable();
    }

    private void addRecord() {
        String data = dataField.getText();
        if (!data.isEmpty()) {
            int key = getNextKey();
            Record record = new Record(key, data);
            bTree.insert(record);
            tableModel.addRow(new Object[]{key, data});
            dataField.setText("");
            saveData(bTree);
        }
    }

    private void deleteRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int key = (int) tableModel.getValueAt(selectedRow, 0);
            tableModel.removeRow(selectedRow);
            bTree.delete(key);
            availableKeys.add(key);
            saveData(bTree);
        }
    }

    private void editRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int key = (int) tableModel.getValueAt(selectedRow, 0);
            String newData = dataField.getText();
            if (!newData.isEmpty()) {
                Record record = bTree.search(key);
                if (record != null) {
                    record.setData(newData);
                    tableModel.setValueAt(newData, selectedRow, 1);
                    dataField.setText("");
                    saveData(bTree);
                }
            }
        }
    }

    private void searchRecord() {
        try {
            int key = Integer.parseInt(dataField.getText());
            Record result = bTree.search(key);
            if (result != null) {
                JOptionPane.showMessageDialog(this, "Found: Key=" + result.getKey() + ", Data=" + result.getData());
            } else {
                JOptionPane.showMessageDialog(this, "Record not found");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid key");
        }
    }

    private int getNextKey() {
        return availableKeys.isEmpty() ? nextKey++ : availableKeys.poll();
    }

    private void loadRecordsIntoTable() {
        for (Record record : bTree.getAllRecords()) {
            tableModel.addRow(new Object[]{record.getKey(), record.getData()});
        }
    }

    public void saveData(BTree tree) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.dat"))) {
            oos.writeObject(tree);
            oos.writeInt(nextKey);
            System.out.println("Дані успішно збережено.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Помилка при збереженні дерева.");
        }
    }

    @SuppressWarnings("unchecked")
    private BTree loadTree() {
        File file = new File("data.dat");
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                BTree loadedTree = (BTree) ois.readObject();
                nextKey = ois.readInt();
                System.out.println("Дані завантажено успішно.");
                return loadedTree;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Помилка при завантаженні даних: файл пошкоджений або неправильний формат.");
            }
        } else {
            System.out.println("Файл не знайдено або він порожній. Створюємо нове дерево.");
        }
        return new BTree(50);
    }


    @SuppressWarnings("unchecked")
    private PriorityQueue<Integer> loadAvailableKeys() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.dat"))) {
            ois.readObject();
            return (PriorityQueue<Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Файл не знайдено або пошкоджено, створюється нова черга.");
            return new PriorityQueue<>();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DatabaseGUI().setVisible(true));
    }

}