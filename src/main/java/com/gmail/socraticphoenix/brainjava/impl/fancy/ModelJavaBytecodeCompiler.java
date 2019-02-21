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
package com.gmail.socraticphoenix.brainjava.impl.fancy;

import com.gmail.socraticphoenix.brainjava.ast.Node;
import com.gmail.socraticphoenix.brainjava.ast.WhileNode;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckCompilationException;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckVisitor;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.TapeModel;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.nio.file.Path;

/**
 * A powerful {@link BrainfuckVisitor} that uses a {@link TapeModel} to define its behavior.
 */
public class ModelJavaBytecodeCompiler implements BrainfuckVisitor, Opcodes {
    private int loopIndex = 0;
    private TapeModel model;
    private String name;
    private ClassWriter writer;
    private GeneratorAdapter main;

    public ModelJavaBytecodeCompiler(TapeModel model, String name) {
        this.model = model;
        this.name = name;
        this.writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        this.writer.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", null);

        MethodVisitor mv = this.writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        model.initial(this.writer);
        MethodVisitor main = this.writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        GeneratorAdapter adapter = new GeneratorAdapter(main, ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V");
        adapter.visitCode();
        this.main = adapter;
        this.model.initialMethod(this.main);
    }

    public int nextLoopIndex() {
        this.loopIndex++;
        return this.loopIndex;
    }

    @Override
    public void visitStart() throws BrainfuckCompilationException {

    }

    @Override
    public BrainfuckVisitor visitWhile(WhileNode node) throws BrainfuckCompilationException {
        int n = nextLoopIndex();
        this.main.invokeStatic(Type.getObjectType(this.name), new Method("loop" + n, Type.VOID_TYPE, new Type[0]));

        MethodVisitor nLoop = this.writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "loop" + n, "()V", null, null);
        GeneratorAdapter nLoopAd = new GeneratorAdapter(nLoop, ACC_PUBLIC | ACC_STATIC, "loop" + n, "()V");

        MethodVisitor nBody = this.writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "loop" + n + "$body", "()V", null, null);
        GeneratorAdapter nBodyAd = new GeneratorAdapter(nBody, ACC_PUBLIC | ACC_STATIC, "loop" + n + "$body", "()V");
        nLoopAd.visitCode();
        nBodyAd.visitCode();

        return new WhileLoopVisitor(this, this.model, this.name, this.writer, nBodyAd, nLoopAd, n);
    }

    @Override
    public void visitAdvance(Node node) throws BrainfuckCompilationException {
        this.model.advance(this.main);
    }

    @Override
    public void visitRetreat(Node node) throws BrainfuckCompilationException {
        this.model.retreat(this.main);
    }

    @Override
    public void visitIncrement(Node node) throws BrainfuckCompilationException {
        this.model.increment(this.main);
    }

    @Override
    public void visitDecrement(Node node) throws BrainfuckCompilationException {
        this.model.decrement(this.main);
    }

    @Override
    public void visitInput(Node node) throws BrainfuckCompilationException {
        this.model.input(this.main);
    }

    @Override
    public void visitOutput(Node node) throws BrainfuckCompilationException {
        this.model.output(this.main);
    }

    @Override
    public void visitEnd() throws BrainfuckCompilationException {
        this.model.end(this.main);
        this.main.returnValue();
        this.main.visitMaxs(0, 0);
        this.main.visitEnd();

        this.writer.visitEnd();
    }

    public byte[] dump() {
        return this.writer.toByteArray();
    }

}
