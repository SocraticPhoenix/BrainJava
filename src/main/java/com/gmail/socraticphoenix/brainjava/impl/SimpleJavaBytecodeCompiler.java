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
package com.gmail.socraticphoenix.brainjava.impl;

import com.gmail.socraticphoenix.brainjava.ast.Node;
import com.gmail.socraticphoenix.brainjava.ast.WhileNode;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckCompilationException;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Stack;

/**
 * An extremely simple {@link BrainfuckVisitor} that compiles to java bytecode.<lb />
 * Implementation details:
 * <ul>
 *     <li>Right-unbounded tape</li>
 *     <li>Min pointer: 0, Max pointer: theoretically infinite (2<sup>32-1</sup> in practice)</li>
 *     <li>Signed, 32 bit, wrapping integer cells</li>
 *     <li>EOF sets cell to 0</li>
 *     <li>Input through command line arguments</li>
 * </ul>
 */
public class SimpleJavaBytecodeCompiler implements BrainfuckVisitor, Opcodes {
    public static final int TAPE = 1;
    public static final int INPUT = 2;
    public static final int POINTER = 3;
    public static final int INPUT_POINTER = 4;

    private Stack<Loop> loopStack;
    private ClassWriter writer;
    private MethodVisitor main;
    private Label veryStart;
    private Label veryEnd;

    private String name;
    private Path dir;

    public SimpleJavaBytecodeCompiler(String name, Path dir) {
        this.loopStack = new Stack<>();
        this.name = name;
        this.dir = dir;
        this.veryStart = new Label();
        this.veryEnd = new Label();
    }

    @Override
    public void visitStart() {
        if(this.loopStack.isEmpty()) {
            this.writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            this.writer.visit(V1_8, ACC_PUBLIC | ACC_FINAL, this.name, null, "java/lang/Object", null);
            MethodVisitor constr = this.writer.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
            constr.visitCode();
            constr.visitVarInsn(ALOAD, 0);
            constr.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constr.visitInsn(RETURN);
            constr.visitMaxs(0, 0);
            constr.visitEnd();

            this.main = this.writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            main.visitParameter("args", ACC_FINAL);
            main.visitCode();
            main.visitLabel(veryStart);

            //init tape
            main.visitTypeInsn(NEW, "java/util/ArrayList");
            main.visitInsn(DUP);
            main.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            main.visitTypeInsn(CHECKCAST, "java/util/List");
            main.visitVarInsn(ASTORE, TAPE);
            main.visitVarInsn(ALOAD, TAPE);
            main.visitLdcInsn(0);
            main.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
            main.visitInsn(POP);
            main.visitLocalVariable("tape", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Integer;>;", veryStart, veryEnd, TAPE);

            //init input array
            main.visitLdcInsn(" ");
            main.visitTypeInsn(CHECKCAST, "Ljava/lang/CharSequence;");
            main.visitVarInsn(ALOAD, 0);
            main.visitTypeInsn(CHECKCAST, "[Ljava/lang/CharSequence;");
            main.visitMethodInsn(INVOKESTATIC, "java/lang/String", "join", "(Ljava/lang/CharSequence;[Ljava/lang/CharSequence;)Ljava/lang/String;", false);
            main.visitMethodInsn(INVOKEINTERFACE, "java/lang/CharSequence", "codePoints", "()Ljava/util/stream/IntStream;", true);
            main.visitMethodInsn(INVOKEINTERFACE, "java/util/stream/IntStream", "toArray", "()[I", true);
            main.visitVarInsn(ASTORE, INPUT);
            main.visitLocalVariable("input", "[I", null, veryStart, veryEnd, INPUT);

            //init tape pointer
            main.visitLdcInsn(0);
            main.visitVarInsn(ISTORE, POINTER);
            main.visitLocalVariable("pointer", "I", null, veryStart, veryEnd, POINTER);

            //init input pointer
            main.visitLdcInsn(0);
            main.visitVarInsn(ISTORE, INPUT_POINTER);
            main.visitLocalVariable("inputPointer", "I", null, veryStart, veryEnd, INPUT_POINTER);
        } else {
            Loop loop = this.loopStack.peek();
            Label start = loop.start;
            Label end = loop.end;

            main.visitLabel(start);
            main.visitVarInsn(ALOAD, TAPE);
            main.visitVarInsn(ILOAD, POINTER);
            main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
            main.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            main.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);

            main.visitJumpInsn(IFEQ, end);
        }
    }

    @Override
    public BrainfuckVisitor visitWhile(WhileNode node) {
        this.loopStack.push(new Loop(new Label(), new Label()));
        return this;
    }

    @Override
    public void visitAdvance(Node node) {
        main.visitIincInsn(POINTER, 1);

        main.visitVarInsn(ALOAD, TAPE);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
        main.visitVarInsn(ILOAD, POINTER);
        Label skip = new Label();
        main.visitJumpInsn(IF_ICMPGT, skip);

        main.visitVarInsn(ALOAD, TAPE);
        main.visitLdcInsn(0);
        main.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
        main.visitInsn(POP);

        main.visitLabel(skip);
    }

    @Override
    public void visitRetreat(Node node) {
        main.visitIincInsn(POINTER, -1);
    }

    @Override
    public void visitIncrement(Node node) {
        main.visitVarInsn(ALOAD, TAPE);
        main.visitVarInsn(ILOAD, POINTER);

        main.visitVarInsn(ALOAD, TAPE);
        main.visitVarInsn(ILOAD, POINTER);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
        main.visitTypeInsn(CHECKCAST, "java/lang/Integer");
        main.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        main.visitLdcInsn(1);
        main.visitInsn(IADD);

        main.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true);
        main.visitInsn(POP);
    }

    @Override
    public void visitDecrement(Node node) {
        main.visitVarInsn(ALOAD, TAPE);
        main.visitVarInsn(ILOAD, POINTER);

        main.visitVarInsn(ALOAD, TAPE);
        main.visitVarInsn(ILOAD, POINTER);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
        main.visitTypeInsn(CHECKCAST, "java/lang/Integer");
        main.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        main.visitLdcInsn(-1);
        main.visitInsn(IADD);

        main.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true);
        main.visitInsn(POP);
    }

    @Override
    public void visitInput(Node node) {
        main.visitVarInsn(ALOAD, TAPE);
        main.visitVarInsn(ILOAD, POINTER);

        main.visitVarInsn(ALOAD, INPUT);
        main.visitInsn(ARRAYLENGTH);
        main.visitVarInsn(ILOAD, INPUT_POINTER);
        Label fail = new Label();
        Label end = new Label();
        main.visitJumpInsn(IF_ICMPLE, fail);
        main.visitVarInsn(ALOAD, INPUT);
        main.visitVarInsn(ILOAD, INPUT_POINTER);
        main.visitInsn(IALOAD);
        main.visitJumpInsn(GOTO, end);
        main.visitLabel(fail);
        main.visitLdcInsn(0);
        main.visitLabel(end);

        main.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true);
        main.visitInsn(POP);

        main.visitIincInsn(INPUT_POINTER, 1);
    }

    @Override
    public void visitOutput(Node node) {
        main.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        main.visitTypeInsn(NEW, "java/lang/String");
        main.visitInsn(DUP);

        main.visitLdcInsn(1);
        main.visitIntInsn(NEWARRAY, T_INT);
        main.visitInsn(DUP);
        main.visitLdcInsn(0);

        main.visitVarInsn(ALOAD, TAPE);
        main.visitVarInsn(ILOAD, POINTER);
        main.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
        main.visitTypeInsn(CHECKCAST, "java/lang/Integer");
        main.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);

        main.visitInsn(IASTORE);

        main.visitLdcInsn(0);
        main.visitLdcInsn(1);
        main.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([III)V", false);

        main.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);

    }

    @Override
    public void visitEnd() throws BrainfuckCompilationException {
        if(this.loopStack.isEmpty()) {
            main.visitLabel(veryEnd);
            main.visitInsn(RETURN);
            main.visitMaxs(0, 0);
            main.visitEnd();
            writer.visitEnd();

            try {
                Files.createDirectories(this.dir);
                Files.write(this.dir.resolve(this.name + ".class"), writer.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new BrainfuckCompilationException("Failed to write file", e);
            }
        } else {
            Loop loop = loopStack.pop();
            main.visitJumpInsn(GOTO, loop.start);
            main.visitLabel(loop.end);
        }
    }

    private static class Loop {
        private Label start;
        private Label end;

        public Loop(Label start, Label end) {
            this.start = start;
            this.end = end;
        }

        public Label getStart() {
            return this.start;
        }

        public Label getEnd() {
            return this.end;
        }
    }

}
