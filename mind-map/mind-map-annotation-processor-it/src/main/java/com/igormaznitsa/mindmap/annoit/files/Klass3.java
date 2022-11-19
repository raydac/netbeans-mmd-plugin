package com.igormaznitsa.mindmap.annoit.files;

import com.igormaznitsa.mindmap.annotations.MmdFile;
import com.igormaznitsa.mindmap.annotations.MmdFiles;

@MmdFiles(
    {@MmdFile(uid = "test1"),
        @MmdFile(uid = "test2", fileName = "Klass3_1")}
)
public class Klass3 extends Klass2 {
}
