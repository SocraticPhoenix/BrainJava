/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 socraticphoenix@gmail.com
 * Copyright (c) 2017 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gmail.socraticphoenix.brainjava.compiler;

import com.gmail.socraticphoenix.brainjava.ast.Loc;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class CharMatrix {
    private char[][] matrix;
    private int mlen = 0;

    public CharMatrix(String val) {
        String[] pieces = val.replace("\r", "").split("\n");
        this.matrix = new char[pieces.length][];
        for (int i = 0; i < pieces.length; i++) {
            this.matrix[i] = pieces[i].toCharArray();
            if(pieces[i].length() > mlen) {
                this.mlen = pieces[i].length();
            }
        }
    }

    public char get(int x, int y) {
        return this.matrix[y][x];
    }

    public void forEach(BiConsumer<Loc, Character> iter) {
        for (int y = 0; y < this.matrix.length; y++) {
            char[] row = this.matrix[y];
            for (int x = 0; x < row.length; x++) {
                iter.accept(new Loc(x, y), this.get(x, y));
            }
        }
    }

    public String insertPointers(Predicate<Loc> test) {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < this.matrix.length; y++) {
            char[] row = this.matrix[y];
            char[] pointers = new char[this.mlen];
            boolean p = false;
            for (int x = 0; x < row.length; x++) {
                if(test.test(new Loc(x, y))) {
                    pointers[x] = '^';
                    p = true;
                }
            }
            for (int x = 0; x < pointers.length; x++) {
                if(!test.test(new Loc(x, y))) {
                    pointers[x] = p ? '_' : ' ';
                }
            }

            builder.append(row).append(System.lineSeparator());
            if(p) {
                builder.append(pointers).append(System.lineSeparator());
            }
        }

        return builder.toString();
    }

}
