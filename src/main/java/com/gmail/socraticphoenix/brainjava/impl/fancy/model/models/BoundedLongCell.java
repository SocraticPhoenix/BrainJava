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
package com.gmail.socraticphoenix.brainjava.impl.fancy.model.models;

import com.gmail.socraticphoenix.brainjava.impl.fancy.model.CellModel;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class BoundedLongCell implements CellModel {
    private long max;
    private long min;

    public BoundedLongCell(long max, long min) {
        this.max = max;
        this.min = min;
    }

    @Override
    public String type() {
        return "java/lang/Long";
    }

    @Override
    public void initial(ClassWriter writer, MethodVisitor clinit) {

    }

    @Override
    public void increment(GeneratorAdapter gen) {
        gen.unbox(Type.LONG_TYPE);
        gen.push(1L);
        gen.math(GeneratorAdapter.ADD, Type.LONG_TYPE);

        Label fail = new Label();
        Label end = new Label();

        gen.dup2();
        gen.push(max);
        gen.ifCmp(Type.LONG_TYPE, GeneratorAdapter.GT, fail);
        gen.goTo(end);
        gen.mark(fail);
        gen.pop2();
        gen.push(min);
        gen.mark(end);
        gen.box(Type.LONG_TYPE);
    }

    @Override
    public void decrement(GeneratorAdapter gen) {
        gen.unbox(Type.LONG_TYPE);
        gen.push(-1L);
        gen.math(GeneratorAdapter.ADD, Type.LONG_TYPE);

        Label fail = new Label();
        Label end = new Label();

        gen.dup2();
        gen.push(min);
        gen.ifCmp(Type.LONG_TYPE, GeneratorAdapter.LT, fail);
        gen.goTo(end);
        gen.mark(fail);
        gen.pop2();
        gen.push(max);
        gen.mark(end);
        gen.box(Type.LONG_TYPE);
    }

    @Override
    public void toCodepoint(GeneratorAdapter gen) {
        gen.unbox(Type.LONG_TYPE);
        gen.cast(Type.LONG_TYPE, Type.INT_TYPE);
    }

    @Override
    public void fromCodepoint(GeneratorAdapter gen) {
        gen.cast(Type.INT_TYPE, Type.LONG_TYPE);

        Label start1 = new Label();
        Label start2 = new Label();
        Label end1 = new Label();
        Label end2 = new Label();
        gen.mark(start1);
        gen.dup2();
        gen.push(this.min);
        gen.ifCmp(Type.LONG_TYPE, GeneratorAdapter.GE, end1);
        gen.push(this.max - this.min + 1);
        gen.math(GeneratorAdapter.ADD, Type.LONG_TYPE);
        gen.goTo(start1);
        gen.mark(end1);


        gen.mark(start2);
        gen.dup2();
        gen.push(this.max);
        gen.ifCmp(Type.LONG_TYPE, GeneratorAdapter.LE, end2);
        gen.push(this.min - this.max - 1);
        gen.math(GeneratorAdapter.ADD, Type.LONG_TYPE);
        gen.goTo(start2);
        gen.mark(end2);

        gen.box(Type.LONG_TYPE);
    }

    @Override
    public void defualtValue(GeneratorAdapter gen) {
        gen.push(0L);
        gen.box(Type.LONG_TYPE);
    }

    @Override
    public void isZero(GeneratorAdapter gen, Label ifZero) {
        gen.unbox(Type.LONG_TYPE);
        gen.push(0L);
        gen.ifCmp(Type.LONG_TYPE, GeneratorAdapter.EQ, ifZero);
    }

}