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
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class ArgumentIO implements IOModel, Opcodes {
    private Type self;
    private int eofVal;

    public ArgumentIO(int eofVal) {
        this.eofVal = eofVal;
    }

    @Override
    public void initial(String cls, ClassWriter writer, MethodVisitor clinit) {
        writer.visitField(ACC_STATIC | ACC_PRIVATE, "input", "[I", null, null);
        writer.visitField(ACC_STATIC | ACC_PRIVATE, "inputPointer", "I", null, 0);
        this.self = Type.getObjectType(cls);
    }

    @Override
    public void initialMethod(GeneratorAdapter main) {
        main.visitLdcInsn(" ");
        main.loadArg(0);
        main.invokeStatic(Type.getType(String.class), new Method("join", Type.getType(String.class), new Type[]{Type.getType(CharSequence.class), Type.getType(CharSequence[].class)}));

        main.invokeInterface(Type.getType(CharSequence.class), new Method("codePoints", Type.getType(IntStream.class), new Type[0]));
        main.invokeInterface(Type.getType(IntStream.class), new Method("toArray", Type.getType(int[].class), new Type[0]));
        main.putStatic(this.self, "input", Type.getType(int[].class));
    }

    @Override
    public void input(GeneratorAdapter gen) {
        gen.getStatic(this.self, "input", Type.getType(int[].class));
        gen.arrayLength();
        gen.getStatic(this.self, "inputPointer", Type.getType(int.class));

        Label eof = new Label();
        Label input = new Label();
        Label end = new Label();

        gen.ifICmp(GeneratorAdapter.GT, input);
        gen.goTo(eof);
        gen.mark(input);
        gen.getStatic(this.self, "input", Type.getType(int[].class));
        gen.getStatic(this.self, "inputPointer", Type.getType(int.class));
        gen.arrayLoad(Type.INT_TYPE);
        gen.getStatic(this.self, "inputPointer", Type.getType(int.class));
        gen.push(1);
        gen.math(GeneratorAdapter.ADD, Type.INT_TYPE);
        gen.putStatic(this.self, "inputPointer", Type.getType(int.class));
        gen.goTo(end);
        gen.mark(eof);
        gen.push(this.eofVal);
        gen.mark(end);
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

    }

}
