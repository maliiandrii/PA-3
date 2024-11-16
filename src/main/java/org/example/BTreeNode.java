package org.example;

import java.io.Serializable;
import java.util.Vector;

class BTreeNode implements Serializable {
    int[] keys;
    Record[] records;
    int numKeys;
    BTreeNode[] children;
    boolean isLeaf;
    int degree;

    BTreeNode(int degree, boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new int[2 * degree - 1];
        this.records = new Record[2 * degree - 1];
        this.children = new BTreeNode[2 * degree];
        this.numKeys = 0;
    }

    public Record search(int key) {
        int i = 0;
        while (i < numKeys && key > keys[i]) i++;

        if (i < numKeys && keys[i] == key) return records[i];

        return isLeaf ? null : children[i].search(key);
    }

    public void insertNonFull(Record record) {
        int i = numKeys - 1;
        if (isLeaf) {
            while (i >= 0 && record.getKey() < keys[i]) {
                keys[i + 1] = keys[i];
                records[i + 1] = records[i];
                i--;
            }
            keys[i + 1] = record.getKey();
            records[i + 1] = record;
            numKeys++;
        } else {
            while (i >= 0 && record.getKey() < keys[i]) i--;
            if (children[i + 1].numKeys == 2 * degree - 1) {
                splitChild(i + 1, children[i + 1]);
                if (record.getKey() > keys[i + 1]) i++;
            }
            children[i + 1].insertNonFull(record);
        }
    }

    public void splitChild(int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.degree, y.isLeaf);
        z.numKeys = degree - 1;
        System.arraycopy(y.keys, degree, z.keys, 0, degree - 1);
        System.arraycopy(y.records, degree, z.records, 0, degree - 1);
        if (!y.isLeaf) System.arraycopy(y.children, degree, z.children, 0, degree);
        y.numKeys = degree - 1;
        System.arraycopy(children, i + 1, children, i + 2, numKeys - i);
        children[i + 1] = z;
        System.arraycopy(keys, i, keys, i + 1, numKeys - i);
        keys[i] = y.keys[degree - 1];
        records[i] = y.records[degree - 1];
        numKeys++;
    }

    public void delete(int key) {
        int idx = findKey(key);

        if (idx < numKeys && keys[idx] == key) {
            if (isLeaf) {
                removeFromLeaf(idx);
            } else {
                removeFromNonLeaf(idx);
            }
        } else {
            if (isLeaf) {
                System.out.println("Ключ " + key + " не знайдено в дереві.");
                return;
            }

            boolean flag = (idx == numKeys);

            if (children[idx].numKeys < degree) {
                fill(idx);
            }

            if (flag && idx > numKeys) {
                children[idx - 1].delete(key);
            } else {
                children[idx].delete(key);
            }
        }
    }

    private int findKey(int key) {
        int idx = 0;
        while (idx < numKeys && keys[idx] < key) {
            idx++;
        }
        return idx;
    }

    private void removeFromLeaf(int idx) {
        for (int i = idx + 1; i < numKeys; i++) {
            keys[i - 1] = keys[i];
            records[i - 1] = records[i];
        }
        numKeys--;
    }

    private void removeFromNonLeaf(int idx) {
        int key = keys[idx];

        if (children[idx].numKeys >= degree) {
            Record pred = getPred(idx);
            keys[idx] = pred.getKey();
            records[idx] = pred;
            children[idx].delete(pred.getKey());
        }
        else if (children[idx + 1].numKeys >= degree) {
            Record succ = getSucc(idx);
            keys[idx] = succ.getKey();
            records[idx] = succ;
            children[idx + 1].delete(succ.getKey());
        }
        else {
            merge(idx);
            children[idx].delete(key);
        }
    }

    private Record getPred(int idx) {
        BTreeNode cur = children[idx];
        while (!cur.isLeaf) {
            cur = cur.children[cur.numKeys];
        }
        return cur.records[cur.numKeys - 1];
    }

    private Record getSucc(int idx) {
        BTreeNode cur = children[idx + 1];
        while (!cur.isLeaf) {
            cur = cur.children[0];
        }
        return cur.records[0];
    }

    private void fill(int idx) {
        if (idx != 0 && children[idx - 1].numKeys >= degree) {
            borrowFromPrev(idx);
        } else if (idx != numKeys && children[idx + 1].numKeys >= degree) {
            borrowFromNext(idx);
        } else {
            if (idx != numKeys) {
                merge(idx);
            } else {
                merge(idx - 1);
            }
        }
    }

    private void borrowFromPrev(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx - 1];

        for (int i = child.numKeys - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
            child.records[i + 1] = child.records[i];
        }

        if (!child.isLeaf) {
            for (int i = child.numKeys; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }

        child.keys[0] = keys[idx - 1];
        child.records[0] = records[idx - 1];

        if (!child.isLeaf) {
            child.children[0] = sibling.children[sibling.numKeys];
        }

        keys[idx - 1] = sibling.keys[sibling.numKeys - 1];
        records[idx - 1] = sibling.records[sibling.numKeys - 1];

        child.numKeys++;
        sibling.numKeys--;
    }

    private void borrowFromNext(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];

        child.keys[child.numKeys] = keys[idx];
        child.records[child.numKeys] = records[idx];

        if (!child.isLeaf) {
            child.children[child.numKeys + 1] = sibling.children[0];
        }

        keys[idx] = sibling.keys[0];
        records[idx] = sibling.records[0];

        for (int i = 1; i < sibling.numKeys; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
            sibling.records[i - 1] = sibling.records[i];
        }

        if (!sibling.isLeaf) {
            for (int i = 1; i <= sibling.numKeys; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        child.numKeys++;
        sibling.numKeys--;
    }

    private void merge(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];

        child.keys[degree - 1] = keys[idx];
        child.records[degree - 1] = records[idx];

        for (int i = 0; i < sibling.numKeys; i++) {
            child.keys[i + degree] = sibling.keys[i];
            child.records[i + degree] = sibling.records[i];
        }

        if (!child.isLeaf) {
            for (int i = 0; i <= sibling.numKeys; i++) {
                child.children[i + degree] = sibling.children[i];
            }
        }

        for (int i = idx + 1; i < numKeys; i++) {
            keys[i - 1] = keys[i];
            records[i - 1] = records[i];
            children[i] = children[i + 1];
        }

        child.numKeys += sibling.numKeys + 1;
        numKeys--;
    }


    public void collectRecords(Vector<Record> records) {
        for (int i = 0; i < numKeys; i++) {
            records.add(this.records[i]);
        }
        if (!isLeaf) {
            for (int i = 0; i <= numKeys; i++) {
                children[i].collectRecords(records);
            }
        }
    }
}