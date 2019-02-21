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

import com.gmail.socraticphoenix.brainjava.impl.fancy.model.IOModel;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.PrintStream;
import java.io.Reader;
import java.util.function.Consumer;

/**
 * An {@link IOModel} for interactiveIO input, taken from {@link System#in}. Output is sent to {@link System#out}.
 */
public class SystemIO implements IOModel, Opcodes {
    private Type cls;
    private int eof;

    public SystemIO(int eof) {
        this.eof = eof;
    }

    @Override
    public void initial(String cls, ClassWriter writer, MethodVisitor clinit) {
        this.cls = Type.getObjectType(cls);
        writer.visitField(ACC_PRIVATE | ACC_STATIC, "in", "Ljava/io/Reader;", null, null).visitEnd();

        clinit.visitTypeInsn(NEW, "java/io/BufferedReader");
        clinit.visitInsn(DUP);

        clinit.visitTypeInsn(NEW, "java/io/InputStreamReader");
        clinit.visitInsn(DUP);

        clinit.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        clinit.visitMethodInsn(INVOKESPECIAL, "java/io/InputStreamReader", "<init>", "(Ljava/io/InputStream;)V", false);
        clinit.visitMethodInsn(INVOKESPECIAL, "java/io/BufferedReader", "<init>", "(Ljava/io/Reader;)V", false);

        clinit.visitFieldInsn(PUTSTATIC, cls, "in", "Ljava/io/Reader;");

        MethodVisitor mv;
        {
            mv = writer.visitMethod(ACC_PUBLIC + ACC_STATIC, "readPoint", "(Ljava/io/Reader;)I", null, new String[]{"java/io/IOException"});
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Reader", "read", "()I", false);
            mv.visitVarInsn(ISTORE, 1);
            mv.visitVarInsn(ILOAD, 1);
            Label l2 = new Label();
            mv.visitJumpInsn(IFLT, l2);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(I2C);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "isHighSurrogate", "(C)Z", false);
            Label l3 = new Label();
            mv.visitJumpInsn(IFNE, l3);
            mv.visitLabel(l2);
            mv.visitVarInsn(ILOAD, 1);
            Label l4 = new Label();
            mv.visitJumpInsn(IFGE, l4);
            mv.visitLdcInsn(this.eof);
            Label l5 = new Label();
            mv.visitJumpInsn(GOTO, l5);
            mv.visitLabel(l4);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitLabel(l5);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l3);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ICONST_1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Reader", "mark", "(I)V", false);;
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Reader", "read", "()I", false);
            mv.visitVarInsn(ISTORE, 2);
            mv.visitVarInsn(ILOAD, 2);
            Label l8 = new Label();
            mv.visitJumpInsn(IFGE, l8);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l8);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitInsn(I2C);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "isLowSurrogate", "(C)Z", false);
            Label l10 = new Label();
            mv.visitJumpInsn(IFNE, l10);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Reader", "reset", "()V", false);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l10);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitInsn(I2C);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitInsn(I2C);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "toCodePoint", "(CC)I", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = writer.visitMethod(ACC_PUBLIC + ACC_STATIC, "safeRead", "(Ljava/io/Reader;)I", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/io/IOException");
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, this.cls.getInternalName(), "readPoint", "(Ljava/io/Reader;)I", false);
            mv.visitLabel(l1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l2);
            mv.visitVarInsn(ASTORE, 1);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Failed to read input");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
            mv.visitInsn(ATHROW);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitMaxs(4, 2);
            mv.visitEnd();
        }
    }

    @Override
    public void initialMethod(GeneratorAdapter main) {

    }

    @Override
    public void input(GeneratorAdapter gen) {
        gen.getStatic(this.cls, "in", Type.getType(Reader.class));
        gen.invokeStatic(this.cls, new Method("safeRead", Type.INT_TYPE, new Type[]{Type.getType(Reader.class)}));
    }

    @Override
    public void output(GeneratorAdapter gen, Consumer<GeneratorAdapter> getVal) {
        gen.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));

        gen.newInstance(Type.getType(String.class));
        gen.dup();

        gen.push(1);
        gen.newArray(Type.INT_TYPE);
        gen.dup();
        gen.push(0);
        getVal.accept(gen);
        gen.arrayStore(Type.INT_TYPE);

        gen.push(0);
        gen.push(1);

        gen.invokeConstructor(Type.getType(String.class), new Method("<init>", Type.VOID_TYPE, new Type[]{Type.getType(int[].class), Type.INT_TYPE, Type.INT_TYPE}));

        gen.invokeVirtual(Type.getType(PrintStream.class), new Method("print", Type.VOID_TYPE, new Type[]{Type.getType(String.class)}));
    }

    @Override
    public void end(GeneratorAdapter main) {
        main.getStatic(this.cls, "in", Type.getType(Reader.class));
        main.invokeVirtual(Type.getType(Reader.class), new Method("close", Type.VOID_TYPE, new Type[0]));
    }

}
