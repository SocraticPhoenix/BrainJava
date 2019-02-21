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
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.IOModel;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.TapeModel;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.List;
import java.util.function.Consumer;

/**
 * A {@link TapeModel} with a theoretically infinite tape. In practice, the tape can be at most 2<sup>32</sup> cells wide.
 * The tape is implemented as a two lists, one representing the positive side, and one representing the negative side.
 */
public class UnboundedTape implements TapeModel, Opcodes {
    private CellModel cell;
    private IOModel input;
    private String cls;

    private Type self;
    private Type list;

    public UnboundedTape(String cls, CellModel cell, IOModel input) {
        this.cell = cell;
        this.input = input;
        this.cls = cls;
        this.self = Type.getObjectType(cls);
        this.list = Type.getType(List.class);
    }

    @Override
    public CellModel cell() {
        return this.cell;
    }

    @Override
    public IOModel input() {
        return this.input;
    }

    @Override
    public void initial(ClassWriter writer) {
        writer.visitField(ACC_PRIVATE | ACC_STATIC, "pointer", "I", null, 0).visitEnd();
        writer.visitField(ACC_PRIVATE | ACC_STATIC, "leftTape", "Ljava/util/List;", "Ljava/util/List<L" + this.cell.type() + ";>;", null).visitEnd();
        writer.visitField(ACC_PRIVATE | ACC_STATIC, "rightTape", "Ljava/util/List;", "Ljava/util/List<L" + this.cell.type() + ";>;", null).visitEnd();

        MethodVisitor clinit = writer.visitMethod(ACC_STATIC | ACC_PUBLIC, "<clinit>", "()V", null, null);
        clinit.visitCode();
        clinit.visitTypeInsn(NEW, "java/util/ArrayList");
        clinit.visitInsn(DUP);
        clinit.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        clinit.visitFieldInsn(PUTSTATIC, this.cls, "leftTape", "Ljava/util/List;");

        clinit.visitTypeInsn(NEW, "java/util/ArrayList");
        clinit.visitInsn(DUP);
        clinit.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        clinit.visitFieldInsn(PUTSTATIC, this.cls, "rightTape", "Ljava/util/List;");

        this.cell.initial(writer, clinit);
        this.input.initial(cls, writer, clinit);

        clinit.visitInsn(RETURN);
        clinit.visitMaxs(0, 0);
        clinit.visitEnd();
    }

    @Override
    public void initialMethod(GeneratorAdapter gen) {
        gen.getStatic(this.self, "leftTape", this.list);
        this.cell.defualtValue(gen);
        gen.invokeInterface(this.list, new Method("add", Type.BOOLEAN_TYPE, new Type[]{Type.getType(Object.class)}));
        gen.pop();

        gen.getStatic(this.self, "rightTape", this.list);
        this.cell.defualtValue(gen);
        gen.invokeInterface(this.list, new Method("add", Type.BOOLEAN_TYPE, new Type[]{Type.getType(Object.class)}));
        gen.pop();

        this.input.initialMethod(gen);
    }

    @Override
    public void advance(GeneratorAdapter gen) {
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.push(1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        gen.putStatic(this.self, "pointer", Type.INT_TYPE);
        widen(gen);
    }

    @Override
    public void retreat(GeneratorAdapter gen) {
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.push(-1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        gen.putStatic(this.self, "pointer", Type.INT_TYPE);
        widen(gen);
    }

    @Override
    public void increment(GeneratorAdapter gen) {
        set(gen, g -> {
            get(g);
            this.cell.increment(g);
        });
    }

    @Override
    public void decrement(GeneratorAdapter gen) {
        set(gen, g -> {
            get(g);
            this.cell.decrement(g);
        });
    }

    @Override
    public void input(GeneratorAdapter gen) {
        set(gen, g -> {
            this.input.input(g);
            this.cell.fromCodepoint(g);
        });
    }

    @Override
    public void output(GeneratorAdapter gen) {
        this.input.output(gen, g -> {
            get(g);
            this.cell.toCodepoint(g);
        });
    }

    @Override
    public void end(GeneratorAdapter main) {
        this.input.end(main);
    }

    @Override
    public void get(GeneratorAdapter gen) {
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.push(0);

        Label left = gen.newLabel();
        Label end = gen.newLabel();
        gen.ifICmp(GeneratorAdapter.LT, left);

        gen.getStatic(this.self, "rightTape", this.list);
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.invokeInterface(this.list, new Method("get", Type.getType(Object.class), new Type[]{Type.INT_TYPE}));
        gen.checkCast(Type.getObjectType(this.cell.type()));
        gen.goTo(end);
        gen.mark(left);

        gen.getStatic(this.self, "leftTape", this.list);
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.math(GeneratorAdapter.NEG, Type.INT_TYPE);
        gen.push(-1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        gen.invokeInterface(this.list, new Method("get", Type.getType(Object.class), new Type[]{Type.INT_TYPE}));
        gen.checkCast(Type.getObjectType(this.cell.type()));
        gen.mark(end);
    }

    private void set(GeneratorAdapter gen, Consumer<GeneratorAdapter> getVal) {
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);

        Label left = gen.newLabel();
        Label end = gen.newLabel();
        gen.visitJumpInsn(IFLT, left);

        gen.getStatic(this.self, "rightTape", this.list);
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        getVal.accept(gen);
        gen.invokeInterface(this.list, new Method("set", Type.getType(Object.class), new Type[]{Type.INT_TYPE, Type.getType(Object.class)}));
        gen.pop();
        gen.goTo(end);

        gen.mark(left);
        gen.getStatic(this.self, "leftTape", this.list);
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.math(GeneratorAdapter.NEG, Type.INT_TYPE);
        gen.visitInsn(ICONST_M1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        getVal.accept(gen);
        gen.invokeInterface(this.list, new Method("set", Type.getType(Object.class), new Type[]{Type.INT_TYPE, Type.getType(Object.class)}));
        gen.pop();
        gen.mark(end);
    }

    private void widen(GeneratorAdapter gen) {
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.push(0);

        Label left = gen.newLabel();
        Label end = gen.newLabel();
        gen.ifICmp(GeneratorAdapter.LT, left);

        gen.getStatic(this.self, "rightTape", this.list);
        gen.invokeInterface(this.list, new Method("size", Type.INT_TYPE, new Type[]{}));
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        Label endTest1 = gen.newLabel();

        gen.ifICmp(GeneratorAdapter.GT, endTest1);
        gen.getStatic(this.self, "rightTape", this.list);
        this.cell.defualtValue(gen);
        gen.invokeInterface(this.list, new Method("add", Type.BOOLEAN_TYPE, new Type[]{Type.getType(Object.class)}));
        gen.pop();

        gen.mark(endTest1);
        gen.goTo(end);
        gen.mark(left);

        gen.getStatic(this.self, "leftTape", this.list);
        gen.invokeInterface(this.list, new Method("size", Type.INT_TYPE, new Type[]{}));
        gen.getStatic(this.self, "pointer", Type.INT_TYPE);
        gen.math(GeneratorAdapter.NEG, Type.INT_TYPE);
        gen.push(-1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        Label endTest2 = gen.newLabel();

        gen.ifICmp(GeneratorAdapter.GT, endTest1);
        gen.getStatic(this.self, "leftTape", this.list);
        this.cell.defualtValue(gen);
        gen.invokeInterface(this.list, new Method("add", Type.BOOLEAN_TYPE, new Type[]{Type.getType(Object.class)}));
        gen.pop();

        gen.mark(endTest2);
        gen.mark(end);
    }

}
