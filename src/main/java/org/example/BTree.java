package org.example;

import java.io.Serializable;
import java.util.Vector;
import java.io.*;
import java.util.PriorityQueue;

public class BTree implements Serializable {
    private BTreeNode root;
    private final int degree;

    public BTree(int degree) {
        this.degree = degree;
    }

    public Record search(int key) {
        return root == null ? null : root.search(key);
    }

    public void insert(Record record) {
        if (root == null) {
            root = new BTreeNode(degree, true);
            root.keys[0] = record.getKey();
            root.records[0] = record;
            root.numKeys = 1;
        } else {
            if (root.numKeys == 2 * degree - 1) {
                BTreeNode s = new BTreeNode(degree, false);
                s.children[0] = root;
                s.splitChild(0, root);
                int i = 0;
                if (s.keys[0] < record.getKey()) i++;
                s.children[i].insertNonFull(record);
                root = s;
            } else {
                root.insertNonFull(record);
            }
        }
    }

    public void delete(int key) {
        if (root != null) root.delete(key);
    }

    public Vector<Record> getAllRecords() {
        Vector<Record> records = new Vector<>();
        if (root != null) root.collectRecords(records);
        return records;
    }
}
