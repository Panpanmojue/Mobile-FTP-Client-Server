package com.example.test2;


public class FileMeta {

    public enum Compressed {
        COMPRESSED,
        NOT_COMPRESSED
    }

    public long size;

    public String fileName;
    public Compressed compressed;

    public FileMeta (long size, String fileName, Compressed compressed) {
        this.size = size;
        this.fileName = fileName;
        this.compressed = compressed;
    }
}
