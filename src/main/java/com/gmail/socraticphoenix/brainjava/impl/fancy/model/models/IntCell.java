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

/**
 * A {@link CellModel} for signed, 32-bit, wrapping integer cells.
 */
public class IntCell implements CellModel {

    @Override
    public String type() {
        return "java/lang/Integer";
    }

    @Override
    public void initial(ClassWriter writer, MethodVisitor clinit) {

    }

    @Override
    public void increment(GeneratorAdapter gen) {
        gen.unbox(Type.INT_TYPE);
        gen.push(1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        gen.box(Type.INT_TYPE);
    }

    @Override
    public void decrement(GeneratorAdapter gen) {
        gen.unbox(Type.INT_TYPE);
        gen.push(-1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        gen.box(Type.INT_TYPE);
    }

    @Override
    public void toCodepoint(GeneratorAdapter gen) {
        gen.unbox(Type.INT_TYPE);
    }

    @Override
    public void fromCodepoint(GeneratorAdapter gen) {
        gen.box(Type.INT_TYPE);
    }

    @Override
    public void defualtValue(GeneratorAdapter gen) {
        gen.push(0);
        gen.box(Type.INT_TYPE);
    }

    @Override
    public void isZero(GeneratorAdapter gen, Label ifZero) {
        gen.unbox(Type.INT_TYPE);
        gen.visitJumpInsn(Opcodes.IFEQ, ifZero);
    }

}
