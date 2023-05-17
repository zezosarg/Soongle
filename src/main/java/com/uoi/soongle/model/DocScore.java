package com.uoi.soongle.model;

public class DocScore {
    private int docId;
    private double score;

    public DocScore(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }

    public int getDocId() {
        return docId;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "[" + docId + ", " + score + "]";
    }
}
